package com.muralis.minhasfinancas.api.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvDTO {
	
	private String DESC;
	private String VALOR_LANC;
	private String TIPO;
	private String STATUS;
	private String USUARIO;
	private String DATA_LANC;
	private String CATEGORIA;
	private String LAT;
	private String LONG;
	

}
