package com.muralis.minhasfinancas.api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UsuarioDTO {
	//teste2 git flow
	private String email;
	private String nome;
	private String senha;

}
