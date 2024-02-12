package com.muralis.minhasfinancas.api.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
	
	
	@PostMapping(value = "/upload" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<MultipartFile> uploadArquivo(@RequestParam MultipartFile file){
		
		
		List<CsvDTO> list = new ArrayList<CsvDTO>();
		
		int lancamentosComErro = 0;
		int lancamentosComSucesso = 0;
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
			
			String line = br.readLine();
			line = br.readLine();
			while (line != null ) {
				
				String[] vect = line.split(",");
				//Descricao vazia ou nula
				CsvDTO prod = new CsvDTO();

				prod.setDESC(vect[0]);
					
				
				//Verificar $valor.valor e nulo
				prod.setVALOR_LANC(vect[1]);
				
				//verificar tipo é valido dentre as opcoes RECEITA e DESPESA e nulo
				prod.setTIPO(vect[2]);
				
				//verificar  STATUS é valido dentre as opcoes PENDENTE, CANCELADO e EFETIVADO e nulo
				prod.setSTATUS(vect[3]);
				
				//usuario deve ser numero
				//Verificar id do usuario se existe, caso nao exista deleta linha e nulo
				prod.setUSUARIO(vect[4]);
				
				
				//Verificar padrão dd/mm/yyyy e nulo
				prod.setDATA_LANC(vect[5]);
				
				//Verificar se categoria é existente e nulo
				prod.setCATEGORIA(vect[6]);
				
				prod.setLAT(vect[7]);
				prod.setLONG(vect[8]);
				System.out.println(prod.getCATEGORIA());
				if (!prod.getUSUARIO().isEmpty() && !prod.getSTATUS().isEmpty() && !prod.getTIPO().isEmpty() && !prod.getVALOR_LANC().isEmpty() && !prod.getDESC().isEmpty() && !prod.getDATA_LANC().isEmpty()){
					list.add(prod);
					
				}
				line = br.readLine();
				
				
				
				
				
			}	
			
			
		}
		catch (IOException e) {
			ResponseEntity.badRequest().body(e.getMessage());
		}
		
		
		return new ResponseEntity(list, HttpStatus.OK) ; 
	}
	
	

}
