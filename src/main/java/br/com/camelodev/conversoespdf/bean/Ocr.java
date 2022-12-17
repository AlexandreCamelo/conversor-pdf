package br.com.camelodev.conversoespdf.bean;

import java.io.File;

import javax.enterprise.context.RequestScoped;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;
import net.sourceforge.tess4j.Tesseract;

@Component
@RequestScoped
@Data
public class Ocr {

	private String diretTess;

	public Ocr(@Value("${conversoes.diretorio.tesseract.tessdata}") String diretorio) {
		diretTess = diretorio;
	}

	public String OCRdeImagem(String caminhoENomeImagem, String idiomaTexto, String pagina) {
		
		try {
			File imagem = new File(caminhoENomeImagem);
			
			
			imagem.deleteOnExit();
			
			Tesseract tesseract = new Tesseract();
			tesseract.setDatapath(diretTess);
			tesseract.setLanguage(idiomaTexto);
			tesseract.setPageSegMode(2); 
			tesseract.setOcrEngineMode(0);
			
			if(idiomaTexto.equals("hin")) {
				tesseract.setOcrEngineMode(1);  //Instrução do desenvolvedor, lida no github
			}
			
			
			String result = tesseract.doOCR(imagem);

			if (result.isBlank() || result.isEmpty()) {
				String paginaVazia = "A leitura da página " + pagina + " não obteve resultados";
				return paginaVazia;
			}
			
			return result;
			
		} catch (Exception e) {
			String paginaVazia = "A leitura da página " + pagina + " não obteve resultados";
			e.printStackTrace();
			return paginaVazia;
			
		}
		
	}
	

	
		

}
