package com.muralis.minhasfinancas.api.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Validation;
import javax.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muralis.minhasfinancas.api.dto.UploadFeatureLayerDTO;
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
import com.muralis.minhasfinancas.api.dto.CsvDTO;
import com.muralis.minhasfinancas.api.dto.RespostaUploadDTO;
import com.muralis.minhasfinancas.model.entity.Categoria;
import com.muralis.minhasfinancas.model.entity.Lancamento;
import com.muralis.minhasfinancas.model.enums.TipoLancamento;
import com.muralis.minhasfinancas.service.CategoriaService;
import com.muralis.minhasfinancas.service.CsvService;
import com.muralis.minhasfinancas.service.LancamentoService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/arquivo")
@RequiredArgsConstructor
public class CsvResource {
	
	private final CategoriaService categoriaService;
	private final LancamentoService lancamentoService;
	private final CsvService csvService;
	
	@PostMapping(value = "/upload" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> uploadArquivo( @RequestParam MultipartFile file){
		
		if (!csvService.verificarConteudoArquivo(file)) {
			return new ResponseEntity("O arquivo está vazio.", HttpStatus.BAD_REQUEST);
        } 
		
		int lancamentosComErro = 0;
		int lancamentosComSucesso = 0;
		List<CsvDTO> list = new ArrayList<CsvDTO>();
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
			String line = br.readLine();
			line = br.readLine();
			Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
			int linhas = 1;
			
			while (line != null ) {
				CsvDTO linhaLancamento = new CsvDTO();
				String[] vect = line.split(",");
				linhaLancamento.setDescricao(vect[0]);
				linhaLancamento.setValorLancamento(vect[1].replace("$", ""));
				linhaLancamento.setTipo(vect[2]);
				linhaLancamento.setStatus(vect[3]);
				linhaLancamento.setUsuario(vect[4]);
				linhaLancamento.setDataLancamento(vect[5]);
				linhaLancamento.setCategoria(vect[6]);
				linhaLancamento.setLatitude(vect[7]);
				linhaLancamento.setLongitude(vect[8]);
				
	            if (validator.validate(linhaLancamento).isEmpty()) {
	            	list.add(linhaLancamento);
	                lancamentosComSucesso++;
	                line = br.readLine();
	            } else {
	                lancamentosComErro++;
	                line = br.readLine();
	            }
	            linhas++;
			}	
			RespostaUploadDTO response = new RespostaUploadDTO();
			if(lancamentosComSucesso == 0) {
				return ResponseEntity.badRequest().body("Todas as linhas do arquivo são inválidas, total de linhas com erro: " +lancamentosComErro);
			}else {
				response.setLancamentosComErro(lancamentosComErro);
				response.setLancamentosComSucesso(lancamentosComSucesso);
				response.setLancamentosTotais(lancamentosComSucesso+lancamentosComErro);
			}
			List<Lancamento> listaConvertidaParaLancamento = csvService.converterCsvDtoEMLancamento(list);
			lancamentoService.salvarComStatus(listaConvertidaParaLancamento);

			List<UploadFeatureLayerDTO> listaConvertidaParaLancamentoDTO = csvService.converterLancamentoEmLancamentoDTO(listaConvertidaParaLancamento);
			response.setLancamentosValidados(listaConvertidaParaLancamentoDTO);


			return ResponseEntity.ok(response);
			
		}
		catch (IOException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@GetMapping(value = "/download")
	public ResponseEntity downloadArquivo(
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
		if(csvService.filtroVazio(lancamentoFiltro)) {
			lancamentoFiltro.setAno(LocalDate.now().getYear());
		}
		List<Lancamento> lancamentosResultado = new ArrayList();
		lancamentosResultado = lancamentoService.buscar(lancamentoFiltro);
		String json = csvService.criarArquivo(lancamentosResultado);
	    
	    return new ResponseEntity(json, HttpStatus.OK);
	}
	
	
}
