package com.muralis.minhasfinancas.api.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
	public ResponseEntity<MultipartFile> uploadArquivo(@RequestParam MultipartFile file){
		
		
		List<CsvDTO> list = new ArrayList<CsvDTO>();
		int lancamentosComErro = 0;
		int lancamentosComSucesso = 0;
		
		
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
			List<Lancamento> listaConvertida = converterCsvDtoEMLancamento(list);
			
			lancamentoService.salvarComStatus(listaConvertida);
			
			
			
			
		}
		catch (IOException e) {
			ResponseEntity.badRequest().body(e.getMessage());
		}

		
		
		
		return new ResponseEntity(lancamentosComErro, HttpStatus.OK) ; 
	}
	
	
	@GetMapping(value = "/download")
	public ResponseEntity<MultipartFile> downloadArquivo(){
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

		if (linhaLancamento.getVALOR_LANC().isEmpty()) {
			return false;
		}

		if (linhaLancamento.getDESC().isEmpty() || linhaLancamento.getDESC().length() > 100) {
			return false;
		}
		
		if (linhaLancamento.getLAT().isEmpty() || linhaLancamento.getLAT().length() > 12) {
			return false;
		}
		
		if (linhaLancamento.getLONG().isEmpty() || linhaLancamento.getLONG().length() > 13) {
			return false;
		}
		
		return true;
	}
	
	
	private List<Lancamento> converterCsvDtoEMLancamento(List<CsvDTO> listaCsvDTO){
		
		List<Lancamento> listaLancamentosConvertidos = new ArrayList();
		SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");

		for (CsvDTO csvDTO : listaCsvDTO) {
			Lancamento lancamento = new Lancamento();
			lancamento.setDescricao(csvDTO.getDESC());
			
			
			Optional<Usuario> obterPorId = usuarioService.obterPorId(Long.parseLong(csvDTO.getUSUARIO()));
			
			try {
				Usuario usuario = obterPorId.get();
				lancamento.setUsuario(usuario);

			}catch (RegraNegocioException e) {
				e.getMessage();
			}
			

			try {
				Date data = formatoData.parse(csvDTO.getDATA_LANC());
	            int mes = Integer.parseInt(new SimpleDateFormat("MM").format(data));
	            int ano = Integer.parseInt(new SimpleDateFormat("yyyy").format(data));
	            lancamento.setMes(mes);
	            lancamento.setAno(ano);

			} catch (ParseException e) {
				System.out.println("Erro ao fazer o parsing da data: " + e.getMessage());
	            e.printStackTrace();
			}

			
			lancamento.setValor(new BigDecimal(csvDTO.getVALOR_LANC()));
			lancamento.setLatitude(new BigDecimal(csvDTO.getLAT()));
			lancamento.setLongitude(new BigDecimal(csvDTO.getLONG()));
			
			Categoria categoriaBuscada = categoriaService.obterPorDescricao(csvDTO.getCATEGORIA());
			
			if(categoriaBuscada == null) {
				Categoria novaCategoria = new Categoria();
				novaCategoria.setDescricao(csvDTO.getCATEGORIA());
				
				lancamento.setCategoria(categoriaBuscada);
				
				categoriaService.salvar(novaCategoria);
			}else {
				lancamento.setCategoria(categoriaBuscada);
			}
			lancamento.setTipo(TipoLancamento.valueOf(csvDTO.getTIPO()));
			lancamento.setStatus(StatusLancamento.valueOf(csvDTO.getSTATUS()));

			
			listaLancamentosConvertidos.add(lancamento);
			
			
		}
		
		return listaLancamentosConvertidos;
		
	}
	
	

}
