package com.muralis.minhasfinancas.api.resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
import com.muralis.minhasfinancas.service.CsvService;
import com.muralis.minhasfinancas.service.LancamentoService;
import com.muralis.minhasfinancas.service.UsuarioService;
import org.springframework.web.multipart.MultipartFile;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = MinhasfinancasApplication.class, properties = "org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration")
@AutoConfigureMockMvc(addFilters = false)
public class CsvResourceTest {
	
	static final String API = "/arquivo";
    
    @Autowired
    MockMvc mvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private CategoriaService categoriaService;

    @MockBean
    private LancamentoService lancamentoService;
    
    @MockBean
    private CsvService csvService;

    @Autowired
    private CsvResource csvResource;


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
    public void deveVerificarSeConteudoDoArquivoValido() throws Exception {
        String csvContent = "DESC,VALOR_LANC,TIPO,STATUS,USUARIO,DATA_LANC,CATEGORIA,LAT,LONG\n"
                + "Teste,100.00,DESPESA,PENDENTE,1,2022-02-19,Alimentação,123.456,789.123";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                MediaType.MULTIPART_FORM_DATA_VALUE, csvContent.getBytes());

        Mockito.when(csvService.verificarConteudoArquivo(file)).thenReturn(true);

    }

    @Test 
    public void deveVerificarConteudoArquivoComArquivoVazio() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                MediaType.MULTIPART_FORM_DATA_VALUE, new byte[0]);

        Mockito.when(csvService.verificarConteudoArquivo(file)).thenReturn(false);

    }

}
