package com.muralis.minhasfinancas.api.resource;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.muralis.minhasfinancas.api.dto.AtualizaStatusDTO;
import com.muralis.minhasfinancas.api.dto.LancamentoDTO;
import com.muralis.minhasfinancas.exception.RegraNegocioException;
import com.muralis.minhasfinancas.model.entity.Categoria;
import com.muralis.minhasfinancas.model.entity.Lancamento;
import com.muralis.minhasfinancas.model.entity.Usuario;
import com.muralis.minhasfinancas.model.enums.StatusLancamento;
import com.muralis.minhasfinancas.model.enums.TipoLancamento;
import com.muralis.minhasfinancas.service.CategoriaService;
import com.muralis.minhasfinancas.service.LancamentoService;
import com.muralis.minhasfinancas.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor
public class LancamentoResource {

	private final LancamentoService service;
	private final UsuarioService usuarioService;
	private final CategoriaService categoriaService;

	@GetMapping
	public ResponseEntity buscar(@RequestParam(value = "descricao", required = false) String descricao,
			@RequestParam(value = "mes", required = false) Integer mes,
			@RequestParam(value = "ano", required = false) Integer ano,
			@RequestParam(value = "tipo", required = false) TipoLancamento tipo,
			@RequestParam(value = "status", required = false) StatusLancamento status,
			@RequestParam(value = "usuario") Long idUsuario,
			@RequestParam(value = "id_categoria", required = false) Long idCategoria,
			@RequestParam(value = "latitude", required = false) String latitude,
			@RequestParam(value = "longitude", required = false) String longitude) {
		Lancamento lancamentoFiltro = new Lancamento();
		lancamentoFiltro.setDescricao(descricao);
		lancamentoFiltro.setMes(mes);
		lancamentoFiltro.setAno(ano);
		lancamentoFiltro.setTipo(tipo);
		lancamentoFiltro.setStatus(status);
		lancamentoFiltro.setLatitude(latitude);
		lancamentoFiltro.setLongitude(longitude);

		Optional<Usuario> usuario = usuarioService.obterPorId(idUsuario);
		lancamentoFiltro.setUsuario(usuario.get());

		if (idCategoria != null) {
			Optional<Categoria> categoria = categoriaService.obterPorId(idCategoria);
			if (categoria.isPresent()) {
				lancamentoFiltro.setCategoria(categoria.get());

			}
		}
		List<Lancamento> lancamentos = new ArrayList();

		lancamentos = service.buscar(lancamentoFiltro);

		return ResponseEntity.ok(lancamentos);

	}

	@GetMapping("{id}")
	public ResponseEntity obterLancamento(@PathVariable("id") Long id) {
		return service.obterPorId(id).map(lancamento -> new ResponseEntity(converter(lancamento), HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity(HttpStatus.NOT_FOUND));
	}

	@PostMapping
	public ResponseEntity salvar(@RequestBody LancamentoDTO dto) {
		try {
			Lancamento entidade = converter(dto);
			entidade = service.salvar(entidade);
			return new ResponseEntity(entidade, HttpStatus.CREATED);
		} catch (RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PutMapping("{id}")
	public ResponseEntity atualizar(@PathVariable Long id, @RequestBody LancamentoDTO dto) {
		return service.obterPorId(id).map(entity -> {
			try {
				Lancamento lancamento = converter(dto);
				lancamento.setId(entity.getId());
				service.atualizar(lancamento);
				return ResponseEntity.ok(lancamento);
			} catch (RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}).orElseGet(() -> new ResponseEntity("Lançamento não encontrado na base de dados", HttpStatus.NOT_FOUND));
	}

	@PutMapping("{id}/atualiza-status")
	public ResponseEntity atualizarStatus(@PathVariable Long id, @RequestBody AtualizaStatusDTO dto) {
		return service.obterPorId(id).map(entity -> {
			StatusLancamento statusSelecionado = StatusLancamento.valueOf(dto.getStatus());

			if (statusSelecionado == null) {
				return ResponseEntity.badRequest().body("não foi possível atualizar o status do lançamento");
			}

			try {
				entity.setStatus(statusSelecionado);
				service.atualizar(entity);
				return ResponseEntity.ok(entity);
			} catch (RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}).orElseGet(() -> new ResponseEntity("Lançamento não encontrado na base de dados.", HttpStatus.BAD_REQUEST));

	}

	@DeleteMapping("{id}")
	public ResponseEntity deletar(@PathVariable("id") Long id) {
		return service.obterPorId(id).map(entity -> {
			service.deletar(entity);
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}).orElseGet(() -> new ResponseEntity("Lançamento não encontrado na base de dados", HttpStatus.BAD_REQUEST));

	}

	public LancamentoDTO converter(Lancamento lancamento) {
		return LancamentoDTO.builder().id(lancamento.getId()).descricao(lancamento.getDescricao())
				.valor(lancamento.getValor()).mes(lancamento.getMes()).ano(lancamento.getAno())
				.status(lancamento.getStatus().name()).tipo(lancamento.getTipo().name())
				.usuario(lancamento.getUsuario().getId()).categoria(lancamento.getCategoria().getId())
				.latitude(lancamento.getLatitude()).longitude(lancamento.getLongitude())
				.dataCadastro(lancamento.getDataCadastro()).build();

	}

	public Lancamento converter(LancamentoDTO dto) {
		Lancamento lancamento = new Lancamento();
		lancamento.setId(dto.getId());
		lancamento.setDescricao(dto.getDescricao());
		lancamento.setAno(dto.getAno());
		lancamento.setMes(dto.getMes());
		lancamento.setValor(dto.getValor());
		lancamento.setLatitude(dto.getLatitude());
		lancamento.setLongitude(dto.getLongitude());
		lancamento.setDataCadastro(dto.getDataCadastro());

		Usuario usuario = usuarioService.obterPorId(dto.getUsuario())
				.orElseThrow(() -> new RegraNegocioException("Usuário não encontrado para o Id Informado."));

		lancamento.setUsuario(usuario);

		if (dto.getCategoria() != null) {
			Categoria categoria = categoriaService.obterPorId(dto.getCategoria())
					.orElseThrow(() -> new RegraNegocioException("Categoria não encontrada para o Id Informado."));
			lancamento.setCategoria(categoria);

		}

		if (dto.getTipo() != null) {
			lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));
		}
		if (dto.getStatus() != null) {
			lancamento.setStatus(StatusLancamento.valueOf(dto.getStatus()));
		}

		return lancamento;

	}

}
