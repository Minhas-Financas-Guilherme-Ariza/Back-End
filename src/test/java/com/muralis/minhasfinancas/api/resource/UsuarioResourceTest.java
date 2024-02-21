package com.muralis.minhasfinancas.api.resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muralis.minhasfinancas.MinhasfinancasApplication;
import com.muralis.minhasfinancas.api.dto.UsuarioDTO;
import com.muralis.minhasfinancas.exception.ErroAutenticacao;
import com.muralis.minhasfinancas.exception.RegraNegocioException;
import com.muralis.minhasfinancas.model.entity.Usuario;
import com.muralis.minhasfinancas.service.UsuarioService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = MinhasfinancasApplication.class)
@AutoConfigureMockMvc(addFilters = false)
public class UsuarioResourceTest {
	
	static final String API = "/api/usuarios";
	static final MediaType JSON = MediaType.APPLICATION_JSON;
			
	@Autowired
	MockMvc mvc;
	
	@MockBean
	UsuarioService service;
	
	@Test
	public void deveAutenticarUmUsuario() throws Exception{
		//cenario
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String email = "usuario@email.com";
		String senha = encoder.encode("senha");
		
		UsuarioDTO dto = UsuarioDTO.builder().email(email).senha(senha).build();
		
		Usuario usuario = Usuario.builder().id(1l).email(email).senha(senha).build();
		
		Mockito.when(service.autenticar(email, senha)).thenReturn(usuario);
		
		String json = new ObjectMapper().writeValueAsString(dto);
		
		//execucao e verificacao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
													.post(API.concat("/autenticar"))
													.accept(JSON)
													.contentType(JSON)
													.content(json);
		
		mvc
			.perform(request)
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("token").exists())
			.andExpect(MockMvcResultMatchers.jsonPath("nome").value(usuario.getNome()))
		;
	}
	
	
	@Test
	public void deveRetornarBadRequestAoObterErroDeAutenticacao() throws Exception{
		//cenario
		String email = "usuario@email.com";
		String senha = "123";
		
		UsuarioDTO dto = UsuarioDTO.builder().email(email).senha(senha).build();
				
		Mockito.when(service.autenticar(email, senha)).thenThrow(ErroAutenticacao.class);
		
		String json = new ObjectMapper().writeValueAsString(dto);
		
		//execucao e verificacao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
													.post(API.concat("/autenticar"))
													.accept(JSON)
													.contentType(JSON)
													.content(json);
		
		mvc
			.perform(request)
			.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void deveCriarUmNovoUsuario() throws Exception{
		
		Long id = 1l;
		String email = "usuario@email.com";
		String senha = "123";
				
		UsuarioDTO dto = UsuarioDTO.builder().email(email).senha(senha).build();
		Usuario usuario = Usuario.builder().id(id).email(email).senha(senha).build();
		
		Mockito.when(service.salvarUsuario(Mockito.any(Usuario.class))).thenReturn(usuario);
		String json = new ObjectMapper().writeValueAsString(dto);
		
		//execucao e verificacao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
													.post(API)
													.accept(JSON)
													.contentType(JSON)
													.content(json);
		
		mvc
			.perform(request)
			.andExpect(MockMvcResultMatchers.status().isCreated())
			.andExpect(MockMvcResultMatchers.jsonPath("id").value(usuario.getId()))
			.andExpect(MockMvcResultMatchers.jsonPath("nome").value(usuario.getNome()))
		;
	}
	
	@Test
	public void deveRetornarBadRequestAoTentarCriarUmUsuarioInvalido() throws Exception{
		String email = "usuario@email.com";
		String senha = "123";
				
		UsuarioDTO dto = UsuarioDTO.builder().email(email).senha(senha).build();
		
		Mockito.when(service.salvarUsuario(Mockito.any(Usuario.class))).thenThrow(RegraNegocioException.class);
		String json = new ObjectMapper().writeValueAsString(dto);
		
		//execucao e verificacao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
													.post(API)
													.accept(JSON)
													.contentType(JSON)
													.content(json);
		
		mvc
			.perform(request)
			.andExpect(MockMvcResultMatchers.status().isBadRequest())			
		;
	}
	
	

}
