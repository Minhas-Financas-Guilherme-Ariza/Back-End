package com.muralis.minhasfinancas.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.muralis.minhasfinancas.exception.RegraNegocioException;
import com.muralis.minhasfinancas.model.entity.Categoria;
import com.muralis.minhasfinancas.model.repository.CategoriaRepository;
import com.muralis.minhasfinancas.service.impl.CategoriaServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class CategoriaServiceTest {
	
	@SpyBean
	CategoriaServiceImpl service;
	
	@MockBean
	CategoriaRepository repository;
	
	@Test
	public void deveSalvarUmaCategoria() {
		
		Mockito.doNothing().when(service).validar(Mockito.any(Categoria.class));
		Categoria categoria = Categoria
					.builder()
					.id(1l)
					.descricao("descrição teste")
					.ativo(true)
					.build();
		
		Mockito.when(repository.save(Mockito.any(Categoria.class))).thenReturn(categoria);
		
		//acao
		Categoria categoriaSalva = service.salvar(categoria);
			
		
		//verificacao
		Assertions.assertThat(categoriaSalva).isNotNull();
		Assertions.assertThat(categoriaSalva.getId()).isEqualTo(1l);
		Assertions.assertThat(categoriaSalva.getDescricao()).isEqualTo("descrição teste");
		Assertions.assertThat(categoriaSalva.isAtivo()).isEqualTo(true);	
		
	}
	@Test
    public void deveValidarCategoriaComDescricaoValida() {
        Categoria categoria = new Categoria();
        categoria.setDescricao("Teste");

        assertDoesNotThrow(() -> service.validar(categoria));
    }

    @Test
    public void deveValidarCategoriaComDescricaoNula() {
        Categoria categoria = new Categoria();

        assertThrows(RegraNegocioException.class, () -> service.validar(categoria));
    }

    @Test
    public void deveValidarCategoriaComDescricaoVazia() {
        Categoria categoria = new Categoria();
        categoria.setDescricao("");

        assertThrows(RegraNegocioException.class, () -> service.validar(categoria));
    }

    @Test
    public void deveValidarCategoriaComDescricaoExcendendoLimiteDeCaracteres() {
        Categoria categoria = new Categoria();
        categoria.setDescricao("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequatLorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.");

        assertThrows(RegraNegocioException.class, () -> service.validar(categoria));
    }

    @Test
    public void deveValidarCategoriaComDescricaoExistente() {
        Categoria categoria = new Categoria();
        categoria.setDescricao("Teste");

        List<Categoria> categorias = new ArrayList<>();
        categorias.add(new Categoria());

        when(repository.findByDescricao(categoria.getDescricao())).thenReturn(categorias);

        assertThrows(RegraNegocioException.class, () -> service.validar(categoria));
    }

    @Test
    public void deveObterPorId() {
        Long id = 1L;
        Categoria categoria = new Categoria();
        categoria.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(categoria));

        Optional<Categoria> result = service.obterPorId(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    public void deveObterPorDescricao() {
        String descricao = "Teste";
        Categoria categoria = new Categoria();
        categoria.setDescricao(descricao);

        when(repository.findFirstByDescricao(descricao)).thenReturn(Optional.of(categoria));

        Optional<Categoria> result = service.obterPorDescricao(descricao);

        assertTrue(result.isPresent());
        assertEquals(descricao, result.get().getDescricao());
    }
    
    @Test
    public void testBuscarCategorias() {
        List<Categoria> categorias = new ArrayList<>();
        categorias.add(new Categoria());
        categorias.add(new Categoria());

        when(repository.findAll()).thenReturn(categorias);

        List<Categoria> result = service.buscar();

        assertSame(categorias, result);
        assertEquals(2, result.size()); // Verificando se a lista possui o tamanho esperado
    }
	
	

}
