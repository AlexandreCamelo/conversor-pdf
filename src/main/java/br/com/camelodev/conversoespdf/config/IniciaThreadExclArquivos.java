package br.com.camelodev.conversoespdf.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import br.com.camelodev.conversoespdf.uteis.ThreadExcluirArquivos;

@Configuration
public class IniciaThreadExclArquivos {
	private ThreadExcluirArquivos exclusoes;

	IniciaThreadExclArquivos(@Value("${conversoes.diretorio.arquivos.enviados}") String pastaGeralArquivos) {
		exclusoes = new ThreadExcluirArquivos(pastaGeralArquivos);
	}


	@PostConstruct
	public void Iniciar() throws Exception {
		exclusoes.start();
	}
}
