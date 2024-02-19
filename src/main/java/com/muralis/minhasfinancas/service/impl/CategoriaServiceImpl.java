package com.muralis.minhasfinancas.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.muralis.minhasfinancas.exception.RegraNegocioException;
import com.muralis.minhasfinancas.model.entity.Categoria;
import com.muralis.minhasfinancas.model.entity.Usuario;
import com.muralis.minhasfinancas.model.repository.CategoriaRepository;
import com.muralis.minhasfinancas.service.CategoriaService;

@Service
public class CategoriaServiceImpl implements CategoriaService{
	
	private CategoriaRepository repository;
	
	public CategoriaServiceImpl(CategoriaRepository repository) {
		this.repository = repository;
	}	

	@Override
	public Categoria salvar(Categoria categoria) {
		validar(categoria);
		return repository.save(categoria);
	}	
	
	@Override
	public List<Categoria> buscar() {
		return repository.findAll();
	}
	

	@Override
	public void validar(Categoria categoria) {
		if(categoria.getDescricao() == null || categoria.getDescricao().trim().equals("")){
			throw new RegraNegocioException("Preencha o campo de Descrição.");
		}
		
		if(categoria.getDescricao().length() >  255){
			throw new RegraNegocioException("O limite de caracteres desse campo é 255.");
		}
		
		if (!repository.findByDescricao(categoria.getDescricao()).isEmpty()) {
			throw new RegraNegocioException("Essa descrição já existe.");
		}
		
		
	}
	
	@Override
	public Optional<Categoria> obterPorId(Long id) {
		return repository.findById(id);
	}

	@Override
	public Optional<Categoria> obterPorDescricao(String descricao) {
		return repository.findFirstByDescricao(descricao);
	}

	

}
