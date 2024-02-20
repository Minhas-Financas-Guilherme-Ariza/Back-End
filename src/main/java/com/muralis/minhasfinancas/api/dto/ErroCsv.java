package com.muralis.minhasfinancas.api.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErroCsv {
	
	private List<String> errosPorlinha = new ArrayList<>();
	private Map<Integer, List<String>> linhaEErros = new HashMap<>();
	private List<Map<Integer, List<String>>> error = new ArrayList<>();
	
	private int lancamentosComErro = 0;
	private int lancamentosComSucesso = 0;
	private boolean valido = false;
}
