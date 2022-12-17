package br.com.camelodev.conversoespdf.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class configPastasIniciais {
	public configPastasIniciais(@Value("${conversoes.diretorio.arquivos.enviados}") String caminhoEnviados) {

		try {
			File caminhoInicial = new File(caminhoEnviados);

			if (!caminhoInicial.exists()) {
				caminhoInicial.mkdirs();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
