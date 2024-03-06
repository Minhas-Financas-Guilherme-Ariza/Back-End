package com.muralis.minhasfinancas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.muralis.minhasfinancas.exception.RegraNegocioException;
import com.muralis.minhasfinancas.model.entity.Lancamento;
import com.muralis.minhasfinancas.model.entity.Usuario;
import com.muralis.minhasfinancas.model.enums.StatusLancamento;
import com.muralis.minhasfinancas.model.enums.TipoLancamento;
import com.muralis.minhasfinancas.model.repository.LancamentoRepository;
import com.muralis.minhasfinancas.model.repository.LancamentoRepositoryTest;
import com.muralis.minhasfinancas.service.impl.LancamentoServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LancamentoServiceTest {

	@SpyBean
	LancamentoServiceImpl service;
	
	@MockBean
	LancamentoRepository repository;
	
	@Test
	public void deveSalvarUmLancamento() {
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		Mockito.doNothing().when(service).validar(lancamentoASalvar);

		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1l);
		lancamentoSalvo.setStatus("PENDENTE");
		Mockito.when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);
		
		//execucao
		Lancamento lancamento = service.salvar(lancamentoASalvar);
		
		//verificacao
		assertThat(lancamento.getId()).isEqualTo(lancamentoSalvo.getId());
		assertThat(lancamento.getStatus()).isEqualTo(StatusLancamento.PENDENTE);
		
	}
	
	@Test
	public void naoDeveSalvarUmLancamentoQuandoHouverErroDeValidacao() {
		//cenario
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		Mockito.doThrow(RegraNegocioException.class).when(service).validar(lancamentoASalvar);
		
		//execucao e verificacao
		catchThrowableOfType(() -> service.salvar(lancamentoASalvar), RegraNegocioException.class);
		Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
	
	}
	
	@Test
	public void deveAtualizarUmLancamento() {
		

		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1l);
		lancamentoSalvo.setStatus("PENDENTE");
		
		Mockito.doNothing().when(service).validar(lancamentoSalvo);
		
		Mockito.when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);
		
		//execucao
		service.atualizar(lancamentoSalvo);
		
		//verificacao
		Mockito.verify(repository, Mockito.times(1)).save(lancamentoSalvo);
		
		
	}
	
	@Test
	public void deveLancarErroAoTentarAtualizarUmLancamentoQueAindaNaoFoiSalvo() {
		//cenario
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		
		//execucao e verificacao
		catchThrowableOfType( () -> service.atualizar(lancamento), NullPointerException.class);
		Mockito.verify(repository, Mockito.never()).save(lancamento);
	}
	
	@Test
	public void deveDeletarUmLancamento() {
		//cenario
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1l);
		
		//execucao
		service.deletar(lancamento);
		
		//verificacao
		Mockito.verify(repository).delete(lancamento);
	}
	
	@Test
	public void deveLancarErroAoTentarDeletarUmLancamentoQueAindaNaoFoiSalvo() {
		
		//cenario
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		
		//execucao
		catchThrowableOfType( () -> service.deletar(lancamento), NullPointerException.class);

		//verificacao
		Mockito.verify(repository, Mockito.never()).delete(lancamento);

	}
	
	@Test
	public void deveFiltrarLancamentos() {
		//cenario
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1l);
		
		
		List<Lancamento> lista = Arrays.asList(lancamento);
		Mockito.when(repository.findAll(Mockito.any(Example.class))).thenReturn(lista);
		
		//execucao
		List<Lancamento> resultado = service.buscar(lancamento);
		
		//verificacao
		Assertions
			.assertThat(resultado)
			.isNotEmpty()
			.hasSize(1)
			.contains(lancamento);
	}
	
	@Test
	public void deveAtualizarOsStatusDeUmLancamento() {
		//cenario
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1l);
		lancamento.setStatus("PENDENTE");
		
		StatusLancamento novoStatus = StatusLancamento.EFETIVADO;
		Mockito.doReturn(lancamento).when(service).atualizar(lancamento);
		
		//execucao
		service.atualizarStatus(lancamento, novoStatus);
		
		//verificacoes
		assertThat(lancamento.getStatus()).isEqualTo(novoStatus);
		Mockito.verify(service).atualizar(lancamento);
		
	}
	
	@Test
	public void deveRetornarVazioQuandoOLancamentoNaoExiste() {
		//cenario
		Long id = 1l;
		
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1l);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
		
		//execucao
		Optional<Lancamento> resultado = service.obterPorId(id);
		
		//verificacao
		assertThat(resultado.isPresent()).isFalse();
	}
	
	@Test
	public void deveLancarErroAoValidarUmLancamento() {
		Lancamento lancamento = new Lancamento();
		
		//descricao inicio
		Throwable erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma Descrição válida.");
		
		lancamento.setDescricao("");
		
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma Descrição válida.");
	
		lancamento.setDescricao("Salario");
		//descricao fim
		
		//mes inicio
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");
		
		lancamento.setAno(0);		
		
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");
		
		lancamento.setAno(13);
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");
		
		lancamento.setMes(1);
		//mes fim
				
		//ano inicio
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido.");
		
		lancamento.setAno(202);
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido.");
		
		lancamento.setAno(2020);
		//ano fim
		
		//usuario inicio
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Usuário.");
		
		lancamento.setUsuario(new Usuario());	
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Usuário.");
		
		lancamento.getUsuario().setId(1l);
		//usuario fim		
		
		//valor inicio
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Valor válido.");
		
		lancamento.setValor(BigDecimal.ZERO);
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Valor válido.");
		
		lancamento.setValor(BigDecimal.valueOf(1));
		//valor fim
		
		//tipo inicio
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Tipo de Lançamento.");
		//tipo fim
		
	}
	
	@Test
    public void deveSalvarComStatus() {
        List<Lancamento> lancamentos = new ArrayList<>();
        lancamentos.add(new Lancamento());
        lancamentos.add(new Lancamento());

        List<Lancamento> lancamentosSalvos = new ArrayList<>();
        lancamentosSalvos.add(new Lancamento());
        lancamentosSalvos.add(new Lancamento());

        when(repository.saveAll(lancamentos)).thenReturn(lancamentosSalvos);

        List<Lancamento> resultado = service.salvarComStatus(lancamentos);

        verify(repository).saveAll(lancamentos);

        assertSame(lancamentosSalvos, resultado);
        assertEquals(2, resultado.size()); 
    }
	
	 @Test
     public void deveObterSaldoPorUsuario() {
	        BigDecimal receitas = new BigDecimal("100.00");
	        BigDecimal despesas = new BigDecimal("50.00");

	        when(repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(anyLong(), eq(TipoLancamento.RECEITA), eq(StatusLancamento.EFETIVADO))).thenReturn(receitas);
	        when(repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(anyLong(), eq(TipoLancamento.DESPESA), eq(StatusLancamento.EFETIVADO))).thenReturn(despesas);

	        BigDecimal saldo = service.obterSaldoPorUsuario(1L);

	        assertEquals(new BigDecimal("50.00"), saldo);
	 }
	 
	 @Test
 	 public void deveObterSaldoPorUsuarioSemDespesas() {
		 
	        BigDecimal receitas = new BigDecimal("100.00");
	        BigDecimal despesas = null;

	        when(repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(anyLong(), eq(TipoLancamento.RECEITA), eq(StatusLancamento.EFETIVADO))).thenReturn(receitas);
	        when(repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(anyLong(), eq(TipoLancamento.DESPESA), eq(StatusLancamento.EFETIVADO))).thenReturn(despesas);

	        BigDecimal saldo = service.obterSaldoPorUsuario(1L);

	        assertEquals(new BigDecimal("100.00"), saldo);
	 }

    @Test
    public void deveObterSaldoPorUsuarioSemReceitasEDespesas() {

    	BigDecimal receitas = null;
        BigDecimal despesas = null;

        when(repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(anyLong(), eq(TipoLancamento.RECEITA), eq(StatusLancamento.EFETIVADO))).thenReturn(receitas);
        when(repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(anyLong(), eq(TipoLancamento.DESPESA), eq(StatusLancamento.EFETIVADO))).thenReturn(despesas);

        BigDecimal saldo = service.obterSaldoPorUsuario(1L);

        assertEquals(BigDecimal.ZERO, saldo);
    }
	
	
	
	
}
