package com.muralis.minhasfinancas.service;

import java.util.List;
import java.util.Optional;

import com.muralis.minhasfinancas.api.dto.SaldoDTO;
import com.muralis.minhasfinancas.model.entity.Lancamento;
import com.muralis.minhasfinancas.model.enums.StatusLancamento;

public interface LancamentoService {
	
	Lancamento salvar(Lancamento lancamento);
	
	Lancamento atualizar(Lancamento lancamento);
	
	void deletar(Lancamento lancamento);
	
	List<Lancamento> buscar(Lancamento lancamentoFiltro);
	
	void atualizarStatus(Lancamento lancamento, StatusLancamento status);
	
	void validar(Lancamento lancamento);
	
	Optional<Lancamento> obterPorId(long id);
	
	SaldoDTO obterSaldoPorUsuario(Long id);

	List<Lancamento> salvarComStatus(List<Lancamento> lancamentos);

}
