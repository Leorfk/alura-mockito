package br.com.alura.leilao.service;

import br.com.alura.leilao.dao.LeilaoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FinalizarLelilaoServiceTest {

    private FinalizarLeilaoService service;

    @Mock
    private LeilaoDao leilaoDao;

    @Mock
    private EnviadorDeEmails enviadorDeEmails;

    @BeforeEach
    public void beforeEach(){
        MockitoAnnotations.initMocks(this);
        this.service = new FinalizarLeilaoService(leilaoDao, enviadorDeEmails);
    }
    @Test
    public void deveriaFinalizarUmLeilao() {
        List<Leilao> leiloes = leiloes();
        Mockito.when(leilaoDao.buscarLeiloesExpirados()).thenReturn(leiloes);

        service.finalizarLeiloesExpirados();

        Leilao leilao = leiloes.get(0);
        assertTrue(leilao.isFechado());
        assertEquals(new BigDecimal("800"), leilao.getLanceVencedor().getValor());

        Mockito.verify(leilaoDao).salvar(leilao);
    }

    @Test
    public void deveriaEnviarEmailParaVencedorDoLeilao() {
        List<Leilao> leiloes = leiloes();
        Mockito.when(leilaoDao.buscarLeiloesExpirados()).thenReturn(leiloes);
        service.finalizarLeiloesExpirados();
        Leilao leilao = leiloes.get(0);
        Lance lanceVencedor = leilao.getLanceVencedor();
        Mockito.verify(enviadorDeEmails).enviarEmailVencedorLeilao(lanceVencedor);
    }

    @Test
    public void naoDeveriaEnviarEmailParaVencedorDoLeilaoCasoOcorraErroAoFinalizarLeilao() {
        List<Leilao> leiloes = leiloes();

        Mockito.when(leilaoDao.buscarLeiloesExpirados()).thenReturn(leiloes);

        Mockito.when(leilaoDao.salvar(Mockito.any()))
                .thenThrow(RuntimeException.class);
        try {
            service.finalizarLeiloesExpirados();
            Mockito.verifyNoMoreInteractions(enviadorDeEmails);
        } catch (Exception e){}
    }

    private List<Leilao> leiloes(){
        List<Leilao> lista = new ArrayList<>();
        Leilao leilao = new Leilao("Fone", new BigDecimal("500"), new Usuario("xap"));
        Lance primeiro = new Lance(new Usuario("toma"), new BigDecimal("600"));
        Lance segundo = new Lance(new Usuario("rapaz"), new BigDecimal("800"));
        leilao.propoe(primeiro);
        leilao.propoe(segundo);
        lista.add(leilao);
        return lista;
    }
}
