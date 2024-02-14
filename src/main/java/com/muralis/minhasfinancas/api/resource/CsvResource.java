package com.muralis.minhasfinancas.api.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.muralis.minhasfinancas.api.dto.CsvDTO;
import com.muralis.minhasfinancas.model.entity.Usuario;
import com.muralis.minhasfinancas.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/arquivo")
@RequiredArgsConstructor
public class CsvResource {
	
	UsuarioService usuarioService;
	
	
	
	//Uma lista de Chave(numero da linha) Valor(Uma Lista de erros por linha)
	List<Map<Integer, String>> erros = new ArrayList<>();
	Map<Integer, String> linhaEErro = new HashMap<>();
    
    
	
	
	@PostMapping(value = "/upload" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<MultipartFile> uploadArquivo(@RequestParam MultipartFile file){
		
		
		List<CsvDTO> list = new ArrayList<CsvDTO>();
		int lancamentosComErro = 0;
		int lancamentosComSucesso = 0;
		int numeroLinha = 1;
		
		
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
				
				if(validarLinha(linhaLancamento, numeroLinha)) {
					list.add(linhaLancamento);
					lancamentosComSucesso++;
					line = br.readLine();

				}else {
					lancamentosComErro++;
					line = br.readLine();
				}
				//Map<Integer, String> linhaEErro = new HashMap<>();
				erros.add(linhaEErro);
				numeroLinha++;
			}	
			
			
		}
		catch (IOException e) {
			ResponseEntity.badRequest().body(e.getMessage());
		}
		System.out.println(erros);

		System.out.println("Lançamentos com erro: " + lancamentosComErro);
		System.out.println("Lançamentos com sucesso: " + lancamentosComSucesso);
		System.out.printf("Lançamentos analisados: ");
		System.out.println(lancamentosComSucesso + lancamentosComErro);


		
		return new ResponseEntity(list, HttpStatus.OK) ; 
	}
	
	public boolean validarLinha(CsvDTO linhaLancamento, int numeroLinha) {
		
		if (linhaLancamento.getUSUARIO().isEmpty() || !linhaLancamento.getUSUARIO().matches("\\d+")) {
			linhaEErro.put(numeroLinha, "O campo usuário está inválido.");
			return false;
		}
		
		if (linhaLancamento.getSTATUS().equals("PENDENTE") && linhaLancamento.getSTATUS().equals("CANCELADO") && linhaLancamento.getSTATUS().equals("EFETIVADO")) {
			linhaEErro.put(numeroLinha, "O campo status está inválido.");
			return false;
		}

		if (!linhaLancamento.getTIPO().equals("DESPESA") && !linhaLancamento.getTIPO().equals("RECEITA")) {
			linhaEErro.put(numeroLinha, "O campo tipo está inválido.");
			return false;
		}

		if (linhaLancamento.getVALOR_LANC().isEmpty()) {
			linhaEErro.put(numeroLinha, "O campo valor está inválido.");
			return false;
		}

		if (linhaLancamento.getDESC().isEmpty() || linhaLancamento.getDESC().length() > 100) {
			linhaEErro.put(numeroLinha, "O campo descrição está inválido.");
			return false;
		}
		
		if (linhaLancamento.getLAT().isEmpty() || linhaLancamento.getLAT().length() > 12) {
			linhaEErro.put(numeroLinha, "O campo latitude está inválido.");
			return false;
		}
		
		if (linhaLancamento.getLONG().isEmpty() || linhaLancamento.getLONG().length() > 13) {
			linhaEErro.put(numeroLinha, "O campo longitude está inválido.");
			return false;
		}

		
		//Descricao vazia ou nula
		//Verificar $valor.valor e nulo
		//verificar tipo é valido dentre as opcoes RECEITA e DESPESA e nulo
		//verificar  STATUS é valido dentre as opcoes PENDENTE, CANCELADO e EFETIVADO e nulo
		//usuario deve ser numero
		//Verificar id do usuario se existe, caso nao exista deleta linha e nulo
		//Verificar padrão dd/mm/yyyy e nulo
		//Verificar se categoria é existente e nulo
		
		return true;
	}
	
	

}
