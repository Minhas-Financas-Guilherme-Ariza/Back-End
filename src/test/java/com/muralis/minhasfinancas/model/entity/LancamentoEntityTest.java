package com.muralis.minhasfinancas.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.muralis.minhasfinancas.model.enums.StatusLancamento;
import com.muralis.minhasfinancas.model.enums.TipoLancamento;

public class LancamentoEntityTest {
	
	@Test
    public void testLancamentoBuilder() {
        Lancamento lancamento = Lancamento.builder()
            .id(1L)
            .descricao("Despesa de Aluguel")
            .mes(2)
            .ano(2024)
            .usuario(new Usuario())
            .valor(new BigDecimal("1000.00"))
            .dataCadastro(LocalDate.now())
            .tipo(TipoLancamento.DESPESA)
            .status(StatusLancamento.PENDENTE)
            .categoria(new Categoria())
            .latitude("40.7128° N")
            .longitude("74.0060° W")
            .build();

        // Verifique se os atributos foram configurados corretamente
        assertEquals(1L, lancamento.getId());
        assertEquals("Despesa de Aluguel", lancamento.getDescricao());
        assertEquals(2, lancamento.getMes());
        assertEquals(2024, lancamento.getAno());
        assertNotNull(lancamento.getUsuario());
        assertEquals(new BigDecimal("1000.00"), lancamento.getValor());
        assertNotNull(lancamento.getDataCadastro());
        assertEquals(TipoLancamento.DESPESA, lancamento.getTipo());
        assertEquals(StatusLancamento.PENDENTE, lancamento.getStatus());
        assertNotNull(lancamento.getCategoria());
        assertEquals("40.7128° N", lancamento.getLatitude());
        assertEquals("74.0060° W", lancamento.getLongitude());
    }

}
