package com.muralis.minhasfinancas.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.muralis.minhasfinancas.api.dto.CsvDTO;
import com.muralis.minhasfinancas.model.entity.Lancamento;

public interface CsvService {

	List<Lancamento> converterCsvDtoEMLancamento(List<CsvDTO> listaCsvDTO);
	boolean verificarConteudoArquivo(MultipartFile multipartFile);
	String criarArquivo( List<Lancamento> lancamentos);
	boolean filtroVazio(Lancamento lancamentoFiltro);
}
