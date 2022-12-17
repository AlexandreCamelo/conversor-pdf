package br.com.camelodev.conversoespdf.uteis;

import java.io.File;
import java.time.LocalDate;

public class ApagarPasta {
	
	public static void apagar(String pasta) {
		System.out.println("Entrei em excluir pasta!!!!!!!!!!");

		File filePasta = new File(pasta);
		
		if (pasta.isBlank()) {
			return;
		}

		if (filePasta.isDirectory() && filePasta.exists()) {
			File[] todos = filePasta.listFiles();

			for (File aApagar : todos) {
				boolean apagou = aApagar.delete();

				if (!apagou) {
					System.out.println("Não consegui apagar o arquivo '" + aApagar + "', na pasta '"
							+ filePasta.getAbsolutePath() + "' - " + LocalDate.now());
				}

			}

		}

		boolean apagouPasta = filePasta.delete();

		if (!apagouPasta) {
			System.out.println(
					"Não consegui apagar a pasta '" + filePasta.getAbsolutePath() + "' - " + LocalDate.now());
		}

	}
	
	
	
	
}
