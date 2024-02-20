package com.muralis.minhasfinancas.api.resource;

import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.muralis.minhasfinancas.MinhasfinancasApplication;
import com.muralis.minhasfinancas.model.entity.Categoria;
import com.muralis.minhasfinancas.model.entity.Lancamento;
import com.muralis.minhasfinancas.model.entity.Usuario;
import com.muralis.minhasfinancas.model.enums.StatusLancamento;
import com.muralis.minhasfinancas.model.enums.TipoLancamento;
import com.muralis.minhasfinancas.service.CategoriaService;
import com.muralis.minhasfinancas.service.LancamentoService;
import com.muralis.minhasfinancas.service.UsuarioService;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = MinhasfinancasApplication.class)
@AutoConfigureMockMvc(addFilters = false)
public class CsvResourceTest {
	
	static final String API = "/api/arquivo";
    
    @Autowired
    MockMvc mvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private CategoriaService categoriaService;

    @MockBean
    private LancamentoService lancamentoService;

    @Autowired
    private CsvResource csvResource;

    @Test
    public void deveFazerUploadDeArquivoValido() throws Exception {
        String csvContent = "DESC,VALOR_LANC,TIPO,STATUS,USUARIO,DATA_LANC,CATEGORIA,LAT,LONG\n"
                + "Teste,100.00,DESPESA,PENDENTE,1,2022-02-19,Alimentação,123.456,789.123";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                MediaType.MULTIPART_FORM_DATA_VALUE, csvContent.getBytes());

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Categoria categoria = new Categoria();
        categoria.setId(1L);

        ResponseEntity<?> responseEntity = csvResource.uploadArquivo(file);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        Mockito.verify(lancamentoService).salvarComStatus(Mockito.any());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                                    .multipart(API.concat("/upload"))
                                                    .file(file);

        mvc
            .perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test 
    public void deveRetornarBadRequestAoFazerUploadDeArquivoVazio() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                MediaType.MULTIPART_FORM_DATA_VALUE, new byte[0]);

        ResponseEntity<?> responseEntity = csvResource.uploadArquivo(file);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                                    .multipart(API.concat("/upload"))
                                                    .file(file);

        mvc
            .perform(request)
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void deveFazerDownloadComSucesso() throws Exception {
        Lancamento lancamento = new Lancamento();
        lancamento.setId(1L);
        lancamento.setDescricao("Teste");
        lancamento.setValor(BigDecimal.valueOf(100.00));
        lancamento.setTipo(TipoLancamento.DESPESA);
        lancamento.setStatus(StatusLancamento.PENDENTE);
        lancamento.setUsuario(new Usuario());
        lancamento.setDataCadastro(LocalDate.now());
        lancamento.setCategoria(new Categoria());
        lancamento.setLatitude("123.456");
        lancamento.setLongitude("789.123");

        List<Lancamento> lancamentos = new ArrayList<>();
        lancamentos.add(lancamento);

        Mockito.when(lancamentoService.buscar(Mockito.any(Lancamento.class))).thenReturn(lancamentos);

        ResponseEntity<?> responseEntity = csvResource.downloadArquivo("Teste", LocalDate.now().getMonthValue(),
                LocalDate.now().getYear(), TipoLancamento.DESPESA, 1L);
        Assertions.assertNotNull(responseEntity);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                                    .get(API.concat("/download"))
                                                    .param("descricao", "Teste")
                                                    .param("mes", String.valueOf(LocalDate.now().getMonthValue()))
                                                    .param("ano", String.valueOf(LocalDate.now().getYear()))
                                                    .param("tipo", TipoLancamento.DESPESA.toString())
                                                    .param("usuario", "1");

        mvc
            .perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void deveReceberUmFiltroDeDownloadVazio() throws Exception {
        ResponseEntity<?> responseEntity = csvResource.downloadArquivo(null, null, null, null, null);
        Assertions.assertNotNull(responseEntity);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                                    .get(API.concat("/download"));

        mvc
            .perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test 
    public void deveVerificarSeConteudoDoArquivoValido() throws Exception {
        String csvContent = "DESC,VALOR_LANC,TIPO,STATUS,USUARIO,DATA_LANC,CATEGORIA,LAT,LONG\n"
                + "Teste,100.00,DESPESA,PENDENTE,1,2022-02-19,Alimentação,123.456,789.123";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                MediaType.MULTIPART_FORM_DATA_VALUE, csvContent.getBytes());

        boolean result = csvResource.verificarConteudoArquivo(file);
        Assertions.assertTrue(result);

    }

    @Test 
    public void deveVerificarConteudoArquivoComArquivoVazio() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                MediaType.MULTIPART_FORM_DATA_VALUE, new byte[0]);

        boolean result = csvResource.verificarConteudoArquivo(file);
        Assertions.assertFalse(result);

    }

    @Test 
    public void deveCriarArquivoComSucesso() throws Exception {
        List<Lancamento> lancamentos = new ArrayList<>();
        Lancamento lancamento = new Lancamento();
        lancamento.setId(1L);
        lancamentos.add(lancamento);
        
        LocalDateTime localDateTimeAtual = LocalDateTime.now();
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String carimbo = localDateTimeAtual.format(formatador);
        String nomeArquivo = "lancamento_" + carimbo + ".json";
        File file = new File("C:\\Users\\MURALIS\\Downloads\\" + nomeArquivo);
        file = csvResource.criarArquivo(file, lancamentos);

        Assertions.assertTrue(file.exists());
        Assertions.assertTrue(file.isFile());

        file.delete();

    }
}
