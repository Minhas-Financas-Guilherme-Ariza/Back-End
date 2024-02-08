package com.muralis.minhasfinancas.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.muralis.minhasfinancas.model.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long>{

	List<Categoria> findByDescricao(String descricao);

	
}
