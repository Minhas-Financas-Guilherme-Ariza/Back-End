package com.muralis.minhasfinancas.api.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SaldoDTO {
	
	private BigDecimal saldoTotal;
	private BigDecimal saldoMes;

}
