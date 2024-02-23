package com.muralis.minhasfinancas.api.dto;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import com.muralis.minhasfinancas.api.validation.MaxCodePoints;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvDTO {
	
	@MaxCodePoints(value = 100)
	@NotEmpty
	private String descricao;
	
	@DecimalMin(value = "0", inclusive = false)
	@NotEmpty
	private String valorLancamento;
	
	@Pattern(regexp = "DESPESA|RECEITA")
	@NotEmpty
	private String tipo;
	
	private String status;
	
	@Pattern(regexp = "\\d+")
	@NotEmpty
	private String usuario;
	
	private String dataLancamento;
	private String categoria;
	
	@DecimalMax(value = "90")
	@DecimalMin(value = "-90")
	@MaxCodePoints(value = 12)
	@NotEmpty
	private String latitude;
	
	@DecimalMax(value = "180")
	@DecimalMin(value = "-180")
	@MaxCodePoints(value = 13)
	@NotEmpty
	private String longitude;
	

}
