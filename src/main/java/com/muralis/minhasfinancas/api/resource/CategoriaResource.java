package com.muralis.minhasfinancas.api.resource;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.muralis.minhasfinancas.model.entity.Categoria;
import com.muralis.minhasfinancas.model.repository.CategoriaRepository;
import com.muralis.minhasfinancas.service.CategoriaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaResource {
	
	private final CategoriaService categoriaService;
	
	@PostMapping
	public ResponseEntity salvar(@RequestBody Categoria categoria) {
		categoriaService.salvar(categoria);
		return new ResponseEntity(categoria, HttpStatus.CREATED); 
		
	}
	
	@GetMapping
	public ResponseEntity buscar() {
		
		List<Categoria> resultadoCategorias = categoriaService.buscar();
		
		return new ResponseEntity(resultadoCategorias, HttpStatus.FOUND);
	}

}
