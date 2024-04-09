package com.muralis.minhasfinancas.api.resource;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muralis.minhasfinancas.MinhasfinancasApplication;
import com.muralis.minhasfinancas.api.dto.AtualizaStatusDTO;
import com.muralis.minhasfinancas.api.dto.LancamentoDTO;
import com.muralis.minhasfinancas.exception.RegraNegocioException;
import com.muralis.minhasfinancas.model.entity.Categoria;
import com.muralis.minhasfinancas.model.entity.Lancamento;
import com.muralis.minhasfinancas.model.entity.Usuario;
import com.muralis.minhasfinancas.model.enums.StatusLancamento;
import com.muralis.minhasfinancas.model.enums.TipoLancamento;
import com.muralis.minhasfinancas.model.repository.LancamentoRepository;
import com.muralis.minhasfinancas.model.repository.UsuarioRepository;
import com.muralis.minhasfinancas.service.CategoriaService;
import com.muralis.minhasfinancas.service.LancamentoService;
import com.muralis.minhasfinancas.service.UsuarioService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = MinhasfinancasApplication.class, properties = "org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration")
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LancamentoResourceTest {

    static final String API = "/lancamentos";
    static final MediaType JSON = MediaType.APPLICATION_JSON;

	@Autowired
	MockMvc mvc;

	@Autowired
	LancamentoRepository repository;
	
	@MockBean
	LancamentoService service;
	
	@Autowired
	UsuarioRepository usuarioRepository;
	
	@MockBean
	UsuarioService usuarioService;
	
	@MockBean
	CategoriaService categoriaService;
    
    @Test
    public void deveSalvarLancamento() throws Exception {
    	Usuario usuario = Usuario
    			.builder()
    			.id(1l)
    			.nome("guilherme")
    			.email("guilherme@email.com")
    			.senha("senha")
    			.build();
    	
    	usuarioService.salvarUsuario(usuario);
    	Mockito.when(usuarioService.obterPorId(usuario.getId())).thenReturn(Optional.of(usuario));
    	
    	Categoria categoria = Categoria
    			.builder()
    			.id(1l)
    			.descricao("descricao")
    			.ativo(true)
    			.build();
    	
    	categoriaService.salvar(categoria);
    	Mockito.when(categoriaService.obterPorId(categoria.getId())).thenReturn(Optional.of(categoria));
    	LancamentoDTO dto = criarLancamentoDTO();

        Lancamento lancamento = converter(dto);

        Mockito.when(service.salvar(Mockito.any(Lancamento.class))).thenReturn(lancamento);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(API)
                .accept(JSON)
                .contentType(JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated());
        
    }

    @Test
    public void deveObterLancamentoComFiltro() throws Exception {
        Long id = 1l;
        Lancamento lancamento = criarLancamento(id);
        String descricao = "Descricao";
        Integer mes = 1;
        Integer ano = 2022;

        Mockito.when(usuarioService.obterPorId(lancamento.getUsuario().getId())).thenReturn(Optional.of(new Usuario()));
        Mockito.when(service.buscar(Mockito.any(Lancamento.class))).thenReturn(Collections.singletonList(lancamento));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(API)
                .param("usuario", String.valueOf(id))
                .param("descricao", descricao) 
                .param("mes", String.valueOf(mes))
                .param("ano", String.valueOf(ano));

        System.out.println((lancamento));
        
        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].descricao").value(lancamento.getDescricao()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].valor").value(lancamento.getValor()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].mes").value(lancamento.getMes()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].ano").value(lancamento.getAno()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].tipo").value(String.valueOf(lancamento.getTipo())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value(String.valueOf(lancamento.getStatus())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].usuario").value(lancamento.getUsuario()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].categoria").value(lancamento.getCategoria()));
    }

    
    @Test
    public void deveRetornarNotFoundQuandoObterLancamentoInexistente() throws Exception {
        Long id = 1l;
        Mockito.when(service.obterPorId(id)).thenReturn(Optional.empty());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(API.concat("/" + id));

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    @Test
    public void deveAtualizarLancamento() throws Exception {
    	Usuario usuario = Usuario
    			.builder()
    			.id(1l)
    			.nome("guilherme")
    			.email("guilherme@email.com")
    			.senha("senha")
    			.build();
    	
    	usuarioService.salvarUsuario(usuario);
    	Mockito.when(usuarioService.obterPorId(usuario.getId())).thenReturn(Optional.of(usuario));
    	
    	Categoria categoria = Categoria
    			.builder()
    			.id(1l)
    			.descricao("descricao")
    			.ativo(true)
    			.build();
    	
    	categoriaService.salvar(categoria);
    	Mockito.when(categoriaService.obterPorId(categoria.getId())).thenReturn(Optional.of(categoria));
        Long id = 1l;
        Lancamento lancamento = criarLancamento(id);
        LancamentoDTO dto = converter(lancamento);
        
        Mockito.when(service.obterPorId(id)).thenReturn(Optional.of(lancamento));
        Mockito.when(service.atualizar(lancamento)).thenReturn(lancamento);

        String json = new ObjectMapper().writeValueAsString(dto);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(API.concat("/" + 1))
                .accept(JSON)
                .contentType(JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(lancamento.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.descricao").value(lancamento.getDescricao()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.valor").value(lancamento.getValor()));
    }
    
    @Test
    public void deveAtualizarStatusLancamento() throws Exception {
        Long id = 1L;
        AtualizaStatusDTO dto = new AtualizaStatusDTO();
        dto.setStatus(StatusLancamento.CANCELADO.toString());
        Lancamento lancamento = criarLancamento(id);

        Mockito.when(service.obterPorId(id)).thenReturn(Optional.of(lancamento));
        Mockito.when(service.atualizar(Mockito.any(Lancamento.class))).thenReturn(lancamento);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(API.concat("/" + id + "/atualiza-status"))
                .accept(JSON)
                .contentType(JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(lancamento.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.descricao").value(lancamento.getDescricao()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.valor").value(lancamento.getValor()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(dto.getStatus()));
    }
    
    @Test
    public void deveDeletarLancamento() throws Exception {
        Long id = 1L;
        Lancamento lancamento = criarLancamento(id);

        Mockito.when(service.obterPorId(id)).thenReturn(Optional.of(lancamento));
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(API.concat("/" + id))
                .accept(JSON)
                .contentType(JSON);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    private LancamentoDTO criarLancamentoDTO() {
        return LancamentoDTO.builder()
                .descricao("Descricao")
                .valor(BigDecimal.TEN)
                .mes(1)
                .ano(2022)
                .tipo(TipoLancamento.RECEITA.toString())
                .status(StatusLancamento.PENDENTE.toString())
                .usuario(1l)
                .categoria(1l)
                .build();
    }

    private Lancamento criarLancamento(Long id) {
    	Usuario usuario = new Usuario();
        usuario.setId(1l);
    	
        return Lancamento.builder()
                .id(id)
                .descricao("Descricao")
                .valor(BigDecimal.TEN)
                .mes(1)
                .ano(2022)
                .tipo(TipoLancamento.RECEITA)
                .status(StatusLancamento.PENDENTE)
                .usuario(usuario)
                .categoria(Categoria.builder().id(1l).build())
                .build();
    }
    
    public LancamentoDTO converter(Lancamento lancamento) {
		return LancamentoDTO.builder()
				.id(lancamento.getId())
				.descricao(lancamento.getDescricao())
				.valor(lancamento.getValor())
				.mes(lancamento.getMes())
				.ano(lancamento.getAno())
				.status(lancamento.getStatus().name())
				.tipo(lancamento.getTipo().name())
				.usuario(lancamento.getUsuario().getId())
				.categoria(lancamento.getCategoria().getId())
				.latitude(lancamento.getLatitude())
				.longitude(lancamento.getLongitude())
				.dataCadastro(lancamento.getDataCadastro())
				.build();
	}
    
	public Lancamento converter(LancamentoDTO dto) {
		Lancamento lancamento = new Lancamento();
		lancamento.setId(dto.getId());
		lancamento.setDescricao(dto.getDescricao());
		lancamento.setAno(dto.getAno());
		lancamento.setMes(dto.getMes());
		lancamento.setValor(dto.getValor());
		lancamento.setLatitude(dto.getLatitude());
		lancamento.setLongitude(dto.getLongitude());
		lancamento.setDataCadastro(dto.getDataCadastro());
		
		Usuario usuario = usuarioService
			.obterPorId(dto.getUsuario())
			.orElseThrow(  () -> new RegraNegocioException("Usuário não encontrado para o Id Informado."));

		lancamento.setUsuario(usuario);
		Categoria categoria = categoriaService
				.obterPorId(dto.getCategoria())
				.orElseThrow( () -> new RegraNegocioException("Categoria não encontrada para o Id Informado."));
		lancamento.setCategoria(categoria);
		
		if (dto.getTipo() != null) {
			lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));
		}
		if (dto.getStatus() != null) {
			lancamento.setStatus(dto.getStatus());
		}
		
		return lancamento;
	}
}

