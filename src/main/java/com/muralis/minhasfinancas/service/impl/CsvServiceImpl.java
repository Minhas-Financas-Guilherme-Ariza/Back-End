package com.muralis.minhasfinancas.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.muralis.minhasfinancas.api.dto.CsvDTO;
import com.muralis.minhasfinancas.model.entity.Categoria;
import com.muralis.minhasfinancas.model.entity.Lancamento;
import com.muralis.minhasfinancas.model.entity.Usuario;
import com.muralis.minhasfinancas.model.enums.StatusLancamento;
import com.muralis.minhasfinancas.model.enums.TipoLancamento;
import com.muralis.minhasfinancas.service.CategoriaService;
import com.muralis.minhasfinancas.service.CsvService;
import com.muralis.minhasfinancas.service.UsuarioService;

@Service
public class CsvServiceImpl implements CsvService{
	
	@Autowired
	UsuarioService usuarioService;
	
	@Autowired
	CategoriaService categoriaService;

	@Override
	public List<Lancamento> converterCsvDtoEMLancamento(List<CsvDTO> listaCsvDTO){
			
			List<Lancamento> listaLancamentosConvertidos = new ArrayList<Lancamento>();
			
			DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	        DateTimeFormatter formatoDataCriacao = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	        
			for (CsvDTO csvDTO : listaCsvDTO) {
				
				//Item foreach Lancamento
				Lancamento lancamento = new Lancamento();
				
				//Data criação
				lancamento.setDataCadastro(LocalDate.parse(LocalDate.now().format(formatoDataCriacao)));
				
				//Descrição
				lancamento.setDescricao(csvDTO.getDescricao());
				
				//Usuário
				Optional<Usuario> obterPorId = usuarioService.obterPorId(Long.parseLong(csvDTO.getUsuario()));
				Usuario usuario = obterPorId.get();
				lancamento.setUsuario(usuario);
	
				//Mês e Ano
				LocalDate data = LocalDate.parse(csvDTO.getDataLancamento(), formatoEntrada);
				int mes = data.getMonthValue();
				lancamento.setMes(mes);
		        int ano = data.getYear();
	            lancamento.setAno(ano);
	
				//Valor
				lancamento.setValor(new BigDecimal(csvDTO.getValorLancamento()));
				
				//Latitude
				lancamento.setLatitude(csvDTO.getLatitude());
				
				//Longitude
				lancamento.setLongitude(csvDTO.getLongitude());
				
				//Categoria
				if(csvDTO.getCategoria() == null || csvDTO.getCategoria().isEmpty()) {
					lancamento.setCategoria(null);
				}else {
					Optional<Categoria> categoriaBuscada = categoriaService.obterPorDescricao(csvDTO.getCategoria());
					if(categoriaBuscada == null) {
						Categoria novaCategoria = new Categoria();
						novaCategoria.setDescricao(csvDTO.getCategoria());
						lancamento.setCategoria(novaCategoria);
						categoriaService.salvar(novaCategoria);
					}else {
						lancamento.setCategoria(categoriaBuscada.get());
					}
				}
				
				//Tipo
				lancamento.setTipo(TipoLancamento.valueOf(csvDTO.getTipo()));
				
				//Status
				lancamento.setStatus(StatusLancamento.valueOf(csvDTO.getStatus()));
	
				//Adiciona item convertido
				listaLancamentosConvertidos.add(lancamento);
				
			}
			
			return listaLancamentosConvertidos;
		}

	@Override
	public boolean validarLinha(CsvDTO linhaLancamento) {
			
		if (linhaLancamento.getUsuario().isEmpty() || !linhaLancamento.getUsuario().matches("\\d+")) {
			return false;
		}
		
		if (!linhaLancamento.getStatus().equals("PENDENTE") && !linhaLancamento.getStatus().equals("CANCELADO") && !linhaLancamento.getStatus().equals("EFETIVADO")) {
			return false;
		}

		if (!linhaLancamento.getTipo().equals("DESPESA") && !linhaLancamento.getTipo().equals("RECEITA")) {
			return false;
		}
		
		if(!usuarioService.obterPorId(Long.parseLong(linhaLancamento.getUsuario())).isPresent()) {
			return false;
		}
		
		if (linhaLancamento.getValorLancamento().isEmpty() || Double.parseDouble(linhaLancamento.getValorLancamento()) <= 0) {
			return false;
		}

		if (linhaLancamento.getDescricao().isEmpty() || linhaLancamento.getDescricao().codePointCount(0, linhaLancamento.getDescricao().length())  > 100 || linhaLancamento.getDescricao().trim().isEmpty()) {
			return false;
		}
		
		if (linhaLancamento.getLatitude().isEmpty() || linhaLancamento.getLatitude().length() > 12 || Double.parseDouble(linhaLancamento.getLatitude()) > 90 || Double.parseDouble(linhaLancamento.getLatitude()) < -90) {
			return false;
		}
		
		if (linhaLancamento.getLongitude().isEmpty() || linhaLancamento.getLongitude().length() > 13 || Double.parseDouble(linhaLancamento.getLongitude()) > 180 || Double.parseDouble(linhaLancamento.getLongitude()) < -180) {
			return false;
		}
		
		return true;
	}

	@Override
	public boolean verificarConteudoArquivo(MultipartFile multipartFile) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream()))) {
            return reader.readLine() != null; 
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

	@Override
	public File criarArquivo(File arquivo, List<Lancamento> lancamentos) {
	    ObjectMapper objectMapper = new ObjectMapper();
	    objectMapper.registerModule(new JavaTimeModule());
	    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

	    try {
	        String json = objectMapper.writeValueAsString(lancamentos);

	        FileWriter escritor = new FileWriter(arquivo);
	        escritor.write(json);
	        escritor.close();

	        return arquivo;
	    } catch (JsonProcessingException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    return null;
	}

	@Override
	public File escreverNomeArquivo() {
		LocalDateTime localDateTimeAtual = LocalDateTime.now();
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String carimbo = localDateTimeAtual.format(formatador);
        String nomeArquivo = "lancamento_" + carimbo + ".json";
        String home = System.getProperty("user.home");
        File file = new File(home+ "/Downloads/"+ nomeArquivo);
        return file;
	}

	@Override
	public boolean filtroVazio(Lancamento lancamentoFiltro) {
		return Stream.of(
				lancamentoFiltro.getAno() == null,
				lancamentoFiltro.getCategoria() == null,
				lancamentoFiltro.getDescricao() == null,
				lancamentoFiltro.getId() == null,
				lancamentoFiltro.getLatitude() == null,
				lancamentoFiltro.getLongitude() == null,
				lancamentoFiltro.getMes() == null,
				lancamentoFiltro.getStatus() == null,
				lancamentoFiltro.getTipo() == null,
				lancamentoFiltro.getUsuario() == null,
				lancamentoFiltro.getValor() == null
				).allMatch(elemento -> elemento);
		
	}

}