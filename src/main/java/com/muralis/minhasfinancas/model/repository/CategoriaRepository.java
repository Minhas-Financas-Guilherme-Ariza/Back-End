package com.muralis.minhasfinancas.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.muralis.minhasfinancas.model.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long>{
	
	List<Categoria> findByDescricao(String descricao);
	Optional<Categoria> findFirstByDescricao(String descricao);
	List<Categoria> findAllByAtivoTrue();
}
