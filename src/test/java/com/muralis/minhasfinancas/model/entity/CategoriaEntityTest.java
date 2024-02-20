package com.muralis.minhasfinancas.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CategoriaEntityTest {
	
	@Test
    public void testCategoriaBuilder() {
        // Cria um objeto Categoria usando o builder
        Categoria categoria = Categoria.builder()
            .id(1L)
            .descricao("Alimentação")
            .ativo(true)
            .build();

        // Verifica se os atributos foram configurados corretamente
        assertEquals(1L, categoria.getId());
        assertEquals("Alimentação", categoria.getDescricao());
        assertTrue(categoria.isAtivo());
    }

}
