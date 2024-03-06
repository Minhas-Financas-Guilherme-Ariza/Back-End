package com.muralis.minhasfinancas.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.muralis.minhasfinancas.api.dto.SaldoDTO;
import com.muralis.minhasfinancas.exception.RegraNegocioException;
import com.muralis.minhasfinancas.model.entity.Lancamento;
import com.muralis.minhasfinancas.model.enums.StatusLancamento;
import com.muralis.minhasfinancas.model.enums.TipoLancamento;
import com.muralis.minhasfinancas.model.repository.LancamentoRepository;
import com.muralis.minhasfinancas.service.LancamentoService;


@Service
public class LancamentoServiceImpl implements LancamentoService{
	
	
	private LancamentoRepository repository;
	
	private LocalDate localDate = LocalDate.now();
	
	public LancamentoServiceImpl(LancamentoRepository repository) {
		this.repository = repository;
	}
	

	@Override
	@Transactional
	public Lancamento salvar(Lancamento lancamento) {
		lancamento.setDataCadastro(localDate);
		validar(lancamento);
		lancamento.setStatus("PENDENTE");
		return repository.save(lancamento);
	}
	
	@Override
	@Transactional
	public List<Lancamento> salvarComStatus(List<Lancamento> lancamentos) {
		return repository.saveAll(lancamentos);
	}

	@Override
	@Transactional
	public Lancamento atualizar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		validar(lancamento);
		return repository.save(lancamento);
	}

	@Override
	@Transactional
	public void deletar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		repository.delete(lancamento);
		
	}

	@Override
	@Transactional(readOnly = true)
	public List<Lancamento> buscar(Lancamento lancamentoFiltro) {	
		Example<Lancamento> example = Example.of(lancamentoFiltro, 
				ExampleMatcher.matching()
					.withIgnoreCase()
					.withStringMatcher(StringMatcher.CONTAINING));
		return repository.findAll(example);
	}

	@Override
	public void atualizarStatus(Lancamento lancamento, StatusLancamento status) {
		lancamento.setStatus(status.name());
		atualizar(lancamento);
	}


	@Override
	public void validar(Lancamento lancamento) {
		
		if(lancamento.getDescricao() == null || lancamento.getDescricao().trim().equals("")){
			throw new RegraNegocioException("Informe uma Descrição válida.");
		}
		
		if(lancamento.getMes() == null || lancamento.getMes() < 1 || lancamento.getMes() > 12){
			throw new RegraNegocioException("Informe um Mês válido.");
		}
		
		if(lancamento.getAno() == null || lancamento.getAno().toString().length() != 4){
			throw new RegraNegocioException("Informe um Ano válido.");
		}
		
		if(lancamento.getUsuario() == null || lancamento.getUsuario().getId() == null){
			throw new RegraNegocioException("Informe um Usuário.");
		}
		
		if(lancamento.getValor() == null || lancamento.getValor().compareTo(BigDecimal.ZERO) < 1){
			throw new RegraNegocioException("Informe um Valor válido.");
		}
		
		if(lancamento.getTipo() == null){
			throw new RegraNegocioException("Informe um Tipo de Lançamento.");
		}
		
	}


	@Override
	public Optional<Lancamento> obterPorId(long id) {
		return repository.findById(id);
	}


	@Override
	@Transactional(readOnly = true)
	public SaldoDTO obterSaldoPorUsuario(Long id) {
		
		SaldoDTO saldoDTO = new SaldoDTO();
		
		BigDecimal receitasTotal = repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(id, TipoLancamento.RECEITA, StatusLancamento.EFETIVADO);
		BigDecimal despesasTotal = repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(id, TipoLancamento.DESPESA, StatusLancamento.EFETIVADO);
		
		if (receitasTotal == null) {
			receitasTotal = BigDecimal.ZERO;
		}
		if (despesasTotal == null) {
			despesasTotal = BigDecimal.ZERO;
		}
		saldoDTO.setSaldoTotal(receitasTotal.subtract(despesasTotal));
		
		int mesAtual = localDate.getMonthValue();
		
		BigDecimal receitasMensaisAtuais = repository.obterSaldoDoMesPorTipoLancamentoEUsuarioEStatus(id, TipoLancamento.RECEITA, mesAtual, StatusLancamento.EFETIVADO);
		BigDecimal despesasMensaisAtuais = repository.obterSaldoDoMesPorTipoLancamentoEUsuarioEStatus(id, TipoLancamento.DESPESA, mesAtual, StatusLancamento.EFETIVADO);

		if (receitasMensaisAtuais == null) {
			receitasMensaisAtuais = BigDecimal.ZERO;
		}
		if (despesasMensaisAtuais == null) {
			despesasMensaisAtuais = BigDecimal.ZERO;
		}
		
		saldoDTO.setSaldoMes(receitasMensaisAtuais.subtract(despesasMensaisAtuais));
		
		return saldoDTO;
	}

}
