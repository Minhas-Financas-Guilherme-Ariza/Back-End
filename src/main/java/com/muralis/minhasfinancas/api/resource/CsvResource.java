package com.muralis.minhasfinancas.api.resource;

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

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.muralis.minhasfinancas.api.dto.CsvDTO;
import com.muralis.minhasfinancas.exception.RegraNegocioException;
import com.muralis.minhasfinancas.model.entity.Categoria;
import com.muralis.minhasfinancas.model.entity.Lancamento;
import com.muralis.minhasfinancas.model.entity.Usuario;
import com.muralis.minhasfinancas.model.enums.StatusLancamento;
import com.muralis.minhasfinancas.model.enums.TipoLancamento;
import com.muralis.minhasfinancas.service.CategoriaService;
import com.muralis.minhasfinancas.service.LancamentoService;
import com.muralis.minhasfinancas.service.UsuarioService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/arquivo")
@RequiredArgsConstructor
public class CsvResource {
	
	private final UsuarioService usuarioService;
	private final CategoriaService categoriaService;
	private final LancamentoService lancamentoService;
	
	
	
	@PostMapping(value = "/upload" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> uploadArquivo(@RequestParam MultipartFile file){
		
		if (!verificarConteudoArquivo(file)) {
			return new ResponseEntity("O arquivo está vazio.", HttpStatus.BAD_REQUEST);
        } 
		
		int lancamentosComErro = 0;
		int lancamentosComSucesso = 0;
		List<CsvDTO> list = new ArrayList<CsvDTO>();
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
			
			String line = br.readLine();
			line = br.readLine();
			
			

			while (line != null ) {
				CsvDTO linhaLancamento = new CsvDTO();

				String[] vect = line.split(",");
				linhaLancamento.setDESC(vect[0]);
				linhaLancamento.setVALOR_LANC(vect[1].replace("$", ""));
				linhaLancamento.setTIPO(vect[2]);
				linhaLancamento.setSTATUS(vect[3]);
				linhaLancamento.setUSUARIO(vect[4]);
				linhaLancamento.setDATA_LANC(vect[5]);
				linhaLancamento.setCATEGORIA(vect[6]);
				linhaLancamento.setLAT(vect[7]);
				linhaLancamento.setLONG(vect[8]);
				
				if(validarLinha(linhaLancamento)) {
					list.add(linhaLancamento);
					lancamentosComSucesso++;
					line = br.readLine();

				}else {
					lancamentosComErro++;
					line = br.readLine();
				}
			}	
			
			//Verificação de todas linhas inválidas
			String response;
			if(lancamentosComSucesso == 0) {
				response = "Todas as linhas do arquivo são inválidas, total de linhas com erro: " +lancamentosComErro;
			}else {
				response = "Linhas com sucesso: " +lancamentosComSucesso+ "\nLinhas com erro: " +lancamentosComErro;
			}
			
			//Converte a lista de csvDTO para Lista Lançamento e salva no banco de dados
			List<Lancamento> listaConvertida = converterCsvDtoEMLancamento(list);
			lancamentoService.salvarComStatus(listaConvertida);
			
			return ResponseEntity.ok(response);
			
			
		}
		catch (IOException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
		
	}
	
	
	@GetMapping(value = "/download")
	public ResponseEntity<?> downloadArquivo(
	        @RequestParam(value = "descricao", required = false) String descricao,
	        @RequestParam(value = "mes", required = false) Integer mes,
	        @RequestParam(value = "ano", required = false) Integer ano,
	        @RequestParam(value = "tipo", required = false) TipoLancamento tipo,
	        @RequestParam(value = "id_categoria", required = false) Long idCategoria) throws JsonProcessingException {

		Lancamento lancamentoFiltro = new Lancamento();
		lancamentoFiltro.setDescricao(descricao);
		lancamentoFiltro.setMes(mes);
		lancamentoFiltro.setAno(ano);
		lancamentoFiltro.setTipo(tipo);
		
		if(idCategoria != null) {
			Optional<Categoria> categoria = categoriaService.obterPorId(idCategoria);
			if(categoria.isPresent()) {
				lancamentoFiltro.setCategoria(categoria.get());

			}
		}
		
		//Verifica se o filtro está vazio, caso esteja, seta o ano do filtro como o ano atual
		if(filtroVazio(lancamentoFiltro)) {
			lancamentoFiltro.setAno(LocalDate.now().getYear());
		}
		
		List<Lancamento> lancamentosResultado = new ArrayList();
		lancamentosResultado = lancamentoService.buscar(lancamentoFiltro);

		
		//Escrevendo o nome do arquivo
		LocalDateTime localDateTimeAtual = LocalDateTime.now();
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String carimbo = localDateTimeAtual.format(formatador);
        String nomeArquivo = "lancamento_" + carimbo + ".json";
        File file = new File("C:\\Users\\MURALIS\\Downloads\\" + nomeArquivo);
        
        //Criando arquivo
		criarArquivo(file, lancamentosResultado);
	    
	    return new ResponseEntity(file, HttpStatus.OK);
	}
	
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
	
	
	//Verifica se o conteúdo do arquivo é nulo
	public boolean verificarConteudoArquivo(MultipartFile multipartFile) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream()))) {
            return reader.readLine() != null; 
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
	
	//Criar arquivo e escrever em .JSON
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
	
	
	
	public boolean validarLinha(CsvDTO linhaLancamento) {
		
		if (linhaLancamento.getUSUARIO().isEmpty() || !linhaLancamento.getUSUARIO().matches("\\d+")) {
			return false;
		}
		
		if (!linhaLancamento.getSTATUS().equals("PENDENTE") && !linhaLancamento.getSTATUS().equals("CANCELADO") && !linhaLancamento.getSTATUS().equals("EFETIVADO")) {
			return false;
		}

		if (!linhaLancamento.getTIPO().equals("DESPESA") && !linhaLancamento.getTIPO().equals("RECEITA")) {
			return false;
		}
		
		if (!linhaLancamento.getTIPO().equals("DESPESA") && !linhaLancamento.getTIPO().equals("RECEITA")) {
			return false;
		}
		
		if (linhaLancamento.getVALOR_LANC().isEmpty() || Double.parseDouble(linhaLancamento.getVALOR_LANC()) <= 0) {
			return false;
		}

		if (linhaLancamento.getDESC().isEmpty() || linhaLancamento.getDESC().length() > 100 || linhaLancamento.getDESC().trim().isEmpty()) {
			return false;
		}
		
		if (linhaLancamento.getLAT().isEmpty() || linhaLancamento.getLAT().length() > 12 || Double.parseDouble(linhaLancamento.getLAT()) > 90 || Double.parseDouble(linhaLancamento.getLAT()) < -90) {
			return false;
		}
		
		if (linhaLancamento.getLONG().isEmpty() || linhaLancamento.getLONG().length() > 13 || Double.parseDouble(linhaLancamento.getLONG()) > 180 || Double.parseDouble(linhaLancamento.getLONG()) < -180) {
			return false;
		}
		
		return true;
	}
	
	
	private List<Lancamento> converterCsvDtoEMLancamento(List<CsvDTO> listaCsvDTO){
		
		List<Lancamento> listaLancamentosConvertidos = new ArrayList();
		
		DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatoDataCriacao = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        
		for (CsvDTO csvDTO : listaCsvDTO) {
			
			//Item foreach Lancamento
			Lancamento lancamento = new Lancamento();
			
			//Data criação
			lancamento.setDataCadastro(LocalDate.parse(LocalDate.now().format(formatoDataCriacao)));
			
			//Descrição
			lancamento.setDescricao(csvDTO.getDESC());
			
			//Usuário
			Optional<Usuario> obterPorId = usuarioService.obterPorId(Long.parseLong(csvDTO.getUSUARIO()));
			try {
				Usuario usuario = obterPorId.get();
				lancamento.setUsuario(usuario);

			}catch (RegraNegocioException e) {
				e.getMessage();
			}
			

			//Mês e Ano
			try {
				LocalDate data = LocalDate.parse(csvDTO.getDATA_LANC(), formatoEntrada);
				
				int mes = data.getMonthValue();
				lancamento.setMes(mes);
				
		        int ano = data.getYear();
	            lancamento.setAno(ano);

			} catch (Exception e) {
				e.getMessage();
			}

			//Valor
			lancamento.setValor(new BigDecimal(csvDTO.getVALOR_LANC()));
			
			//Latitude
			lancamento.setLatitude(csvDTO.getLAT());
			
			//Longitude
			lancamento.setLongitude(csvDTO.getLONG());
			
			//Categoria
			
			if(csvDTO.getCATEGORIA() == null || csvDTO.getCATEGORIA().isEmpty()) {
				lancamento.setCategoria(null);
			}else {
				Optional<Categoria> categoriaBuscada = categoriaService.obterPorDescricao(csvDTO.getCATEGORIA());
				if(categoriaBuscada == null) {
					Categoria novaCategoria = new Categoria();
					novaCategoria.setDescricao(csvDTO.getCATEGORIA());
					lancamento.setCategoria(categoriaBuscada.get());
					categoriaService.salvar(novaCategoria);
				}else {
					lancamento.setCategoria(categoriaBuscada.get());
				}
			}
			
			
			
			//Tipo
			lancamento.setTipo(TipoLancamento.valueOf(csvDTO.getTIPO()));
			
			//Status
			lancamento.setStatus(StatusLancamento.valueOf(csvDTO.getSTATUS()));

			//Adiciona item convertido
			listaLancamentosConvertidos.add(lancamento);
			
			
		}
		
		return listaLancamentosConvertidos;
		
	}
	
	

}
