package com.muralis.minhasfinancas.api.resource;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muralis.minhasfinancas.MinhasfinancasApplication;
import com.muralis.minhasfinancas.model.entity.Categoria;
import com.muralis.minhasfinancas.service.CategoriaService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = MinhasfinancasApplication.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoriaResourceTest {
	
	static final String API = "/api/categorias";
	static final MediaType JSON = MediaType.APPLICATION_JSON;
	
	@Autowired
	MockMvc mvc;
	
	@MockBean
	CategoriaService service;
	
	@Test
	public void deveCadastrarUmaCategoria() throws Exception{
		String descricao = "Descrição Teste";
		boolean ativo = true;
		
		Categoria categoria = Categoria.builder().id(1l).descricao(descricao).ativo(ativo).build();

		Mockito.when(service.salvar(Mockito.any(Categoria.class))).thenReturn(categoria);
		String json = new ObjectMapper().writeValueAsString(categoria);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
													.post(API)
													.accept(JSON)
													.contentType(JSON)
													.content(json);
		
		mvc
			.perform(request)
			.andExpect(MockMvcResultMatchers.status().isCreated())
			.andExpect(MockMvcResultMatchers.jsonPath("id").value(categoria.getId()))
			.andExpect(MockMvcResultMatchers.jsonPath("descricao").value(categoria.getDescricao()))
			.andExpect(MockMvcResultMatchers.jsonPath("ativo").value(categoria.isAtivo()))
		;
		
		
	}
	
	@Test
	public void deveRetornarTodasCategorias() throws Exception {
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(API);

		mvc
		.perform(request)
		.andExpect(MockMvcResultMatchers.status().isFound())
		;

	}

}
