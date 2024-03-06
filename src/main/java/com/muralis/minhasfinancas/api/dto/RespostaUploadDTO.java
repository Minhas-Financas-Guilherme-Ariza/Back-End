package com.muralis.minhasfinancas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RespostaUploadDTO {
	
	private int lancamentosTotais;
	private int lancamentosComSucesso;
	private int lancamentosComErro;

}
