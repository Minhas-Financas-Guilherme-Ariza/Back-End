package com.muralis.minhasfinancas.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvDTO {
	
	private String descricao;
	private String valorLancamento;
	private String tipo;
	private String status;
	private String usuario;
	private String dataLancamento;
	private String categoria;
	private String latitude;
	private String longitude;
	

}
