package com.muralis.minhasfinancas.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UsuarioEntityTest {
	@Test
    public void testUsuarioBuilder() {
        // Crie um objeto Usuario usando o builder
        Usuario usuario = Usuario.builder()
            .id(1L)
            .nome("João")
            .email("joao@example.com")
            .senha("senha123")
            .build();

        // Verifique se os atributos foram configurados corretamente
        assertEquals(1L, usuario.getId());
        assertEquals("João", usuario.getNome());
        assertEquals("joao@example.com", usuario.getEmail());
        assertEquals("senha123", usuario.getSenha());
    }

}
