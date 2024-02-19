package com.muralis.minhasfinancas.api.resource;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.muralis.minhasfinancas.MinhasfinancasApplication;
import com.muralis.minhasfinancas.model.entity.Lancamento;
import com.muralis.minhasfinancas.model.entity.Usuario;
import com.muralis.minhasfinancas.model.enums.StatusLancamento;
import com.muralis.minhasfinancas.model.enums.TipoLancamento;
import com.muralis.minhasfinancas.service.LancamentoService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = MinhasfinancasApplication.class)
@AutoConfigureMockMvc(addFilters = false)
public class LancamentoResourceTest {

	static final String API = "/api/lancamentos";
	static final MediaType JSON = MediaType.APPLICATION_JSON;
	
	@Autowired
	MockMvc mvc;
	
	@MockBean
	LancamentoService service;
	
	 /*@Test
	    public void testBuscar() throws Exception {
	        // Mock do serviço
	        Lancamento lancamentoMock = new Lancamento();
	        lancamentoMock.setId(1L);
	        lancamentoMock.setDescricao("Lançamento de teste");
	        lancamentoMock.setTipo(TipoLancamento.RECEITA);
	        lancamentoMock.setStatus(StatusLancamento.PENDENTE);
	        lancamentoMock.setUsuario(new Usuario());
	        List<Lancamento> listaLancamentos = new ArrayList<>();
	        listaLancamentos.add(lancamentoMock);
	        when(service.buscar(org.mockito.ArgumentMatchers.any())).thenReturn(listaLancamentos);

	        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
					.post(API)
					.accept(JSON)
					.contentType(JSON);

	        
	        // Requisição GET para buscar lançamentos
	        mvc.perform(					.content(json);
)
	                .param("usuario", "1")
	                .contentType(JSON))
	                .andExpect(status().isOk())
	                .andExpect(jsonPath("$", hasSize(1)))
	                .andExpect(jsonPath("$[0].id", is(1)))
	                .andExpect(jsonPath("$[0].descricao", is("Lançamento de teste")))
	                .andExpect(jsonPath("$[0].tipo", is("RECEITA")))
	                .andExpect(jsonPath("$[0].status", is("PENDENTE")));
	    }
	*/

}
