package com.muralis.minhasfinancas.service;

import java.util.List;
import java.util.Optional;

import com.muralis.minhasfinancas.model.entity.Categoria;

public interface CategoriaService {
	
	Categoria salvar(Categoria categoria);
	
	void validar(Categoria categoria);
	
	List<Categoria> buscar();

	Optional<Categoria> obterPorId(Long id);
	
	Categoria obterPorDescricao(String descricao);

}
