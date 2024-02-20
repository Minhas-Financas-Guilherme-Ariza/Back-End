package com.muralis.minhasfinancas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.muralis.minhasfinancas.exception.ErroAutenticacao;
import com.muralis.minhasfinancas.exception.RegraNegocioException;
import com.muralis.minhasfinancas.model.entity.Usuario;
import com.muralis.minhasfinancas.model.repository.UsuarioRepository;
import com.muralis.minhasfinancas.service.impl.UsuarioServiceImpl;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {
	
	@SpyBean
	UsuarioServiceImpl service;
	
	@MockBean
	UsuarioRepository repository;
	
	@Test
	public void deveSalvarUmUsuario() {
		
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		
		//cenario
		Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
		Usuario usuario = Usuario
					.builder()
					.id(1l)
					.nome("nome")
					.email("email@email.com")
					.senha("senha")
					.build();
		
		Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);
		
		//acao
		Usuario usuarioSalvo = service.salvarUsuario(usuario);
		
		//verificacao
		assertThat(usuarioSalvo).isNotNull();
		assertThat(usuarioSalvo.getId()).isEqualTo(1l);
		assertThat(usuarioSalvo.getNome()).isEqualTo("nome");
		assertThat(usuarioSalvo.getEmail()).isEqualTo("email@email.com");	
		boolean senhaDevidamenteCriptografada = encoder.matches("senha", usuario.getSenha());
		assertThat(senhaDevidamenteCriptografada).isEqualTo(true);

	}
	
	@Test
	public void naoDeveSalvarUmUsuarioCOmEmailJaCadastrado() {
		//cenario
		String email = "email@email.com";
		Usuario usuario = Usuario
					.builder()
					.email(email)
					.build();
		Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);
		
		//acao
		assertThrows(RegraNegocioException.class, () -> service.salvarUsuario(usuario));
		
		//verificacao
		Mockito.verify(repository, Mockito.never()).save(usuario);
		
		
	}
	
	@Test
	public void deveAutenticarUmUsuarioComSucesso() {
		//CENARIO
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		
		String email = "email@email.com";
		String senha = encoder.encode("senha");
		
		Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
		Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));
		
		//acao
		Usuario result = service.autenticar(email, "senha");
		
		//verificacao
		assertThat(result).isNotNull();
		
	}
	
	@Test
	public void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComOEmailInformado() {
		//cenario
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
		
		//acao
		Throwable exception = catchThrowable(() -> service.autenticar("email@email.com", "senha"));
		//verificacao
		assertThat(exception)
				.isInstanceOf(ErroAutenticacao.class)
				.hasMessage("Usuário não encontrado para o e-mail informado.");
		
	}
	
	@Test
	public void deveLancarErroQuandoSenhaNaoBater() {
		//cenario
		String senha = "senha";
		Usuario usuario = Usuario
				.builder()
				.email("email@email.com")
				.senha(senha)
				.build();
		
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));
		
		//acao
		Throwable exception = catchThrowable(() -> service.autenticar("email@email.com", "123"));
		assertThat(exception)
				.isInstanceOf(ErroAutenticacao.class)
				.hasMessage("Senha inválida.");
		
	
	}
	
	@Test
	public void deveValidarEmail() {
		
		//cenario
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);
		
		//acao
		service.validarEmail("email@email.com");
		
		
	}
	
	@Test
	public void deveLancarErroAoValidarEmailQuandoExistirEmailCadastrado() {
		
		//cenario
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);
		
		//acao
		assertThrows(RegraNegocioException.class, () -> service.validarEmail("email@email.com"));
		
		
	}

}
