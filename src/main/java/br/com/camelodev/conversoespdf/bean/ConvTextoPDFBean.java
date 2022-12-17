package br.com.camelodev.conversoespdf.bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FileUtils;
import org.omnifaces.util.Faces;
import org.primefaces.PrimeFaces;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.camelodev.conversoespdf.controles.views.ConversoesGerais;
import br.com.camelodev.conversoespdf.uteis.IdUnicoApp;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope("viewJSF")
@Data
@Slf4j
public class ConvTextoPDFBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private UploadedFile arquivoEnviado;
	private StreamedContent arquivoParaBaixarVindoDeFora;
	private String diretorioEnviados;
	private String estiloLinkDownload;
	private String pastaArquivos;
	private File filePastaArquivos;
	private String idCliente;
	private File caminhoCompletoArquivoParaBaixar;
	private String estiloBotaoBaixar;
	private String estNomeArquivoABaixar;
	private String estVisivelNomeArquivoABaixar;
	private Long tamanhoMaxArquivo = 0L;
	private String nomeArquivoEnviadoJpegOuZip = "";
	@Autowired
	ConversoesGerais conversao;
	@Autowired
	Ocr reconhecimento;
	@Autowired
	ProgressoBarra progresso;
	@Autowired
	ArquivoParaBaixarDTO paraBaixar;
	@Autowired
	CamelCaptchaBean captcha;

	public ConvTextoPDFBean(@Value("${conversoes.diretorio.arquivos.enviados}") String caminhoEnviados,
			@Value("${conversoes.diretorio.tesseract.tessdata}") String diretorio,
			@Value("${conversoes.tamanho.maximo.arquivo}") Long tamanhoMaximoArquivo) {

		try {
			File caminhoInicial = new File(caminhoEnviados);

			if (!caminhoInicial.exists()) {
				caminhoInicial.mkdirs();
			}

			caminhoInicial.deleteOnExit();
			idCliente = IdUnicoApp.gerarIdUUID();
			pastaArquivos = caminhoEnviados + "/temp" + idCliente;
			estiloLinkDownload = "display: none; color:#1673B1; font-family: alex; text-align: center;";
			estNomeArquivoABaixar = "display: none; color:#1673B1; font-family: alex; text-align: center;";
			estVisivelNomeArquivoABaixar = "display: flex; justify-content: center !important; align-items: center; "
					+ "color:#1673B1; font-family: alex; text-align: center !important;"
					+ "flex-wrap: wrap; border: none !important; width: 100% !important;";
			estiloBotaoBaixar = "" + "display: flex !important; " + "margin-top: 5px !important; "
					+ "align-items: center !important; " + "justify-content: space-between !important; "
					+ "padding: 5px !important; " + "width: 280px !important; " + "text-align: right !important; "
					+ "border: 1px solid #1673B1 !important; " + "border-radius: 10px !important; "
					+ "font-family: alex !important; " + "font-size: 50px !important; " + "color:#1673B1; ";
			tamanhoMaxArquivo = Long.valueOf(tamanhoMaximoArquivo);
			arquivoParaBaixarVindoDeFora = ArquivoEnviadoDTO.arquivoParaBaixar;
			nomeArquivoEnviadoJpegOuZip = ArquivoEnviadoDTO.nomeArquivoEnviado;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}





	public void converterTxtParaPDF() {
		ArquivoEnviadoDTO.pararProcesso = false; // não tirar daqui
		
		if (!captcha.quebraCabecasResolvido()) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRADO", "Favor resolver o quebra cabeças."));
			PrimeFaces.current().executeScript("rejeicaoCaptcha();");
			return;
		}
		
		try {
			String nomeArquivo = arquivoEnviado.getFileName();
			ArquivoEnviadoDTO.uplArquivoEnviado = arquivoEnviado;
			long tamanhoArquivo = arquivoEnviado.getSize();
			long arquivoEmMB = tamanhoArquivo / 1000000;

			if (arquivoEnviado != null && !nomeArquivo.isBlank()) {
				String tipoArquivo = arquivoEnviado.getContentType();

				if (!tipoArquivo.equals("text/plain")) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"SÓ ARQUIVOS TXT", "Seu arquivo não é do tipo txt."));
					return;
				} else if (tamanhoArquivo > tamanhoMaxArquivo) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"ARQUIVO MUITO GRANDE", "Somente arquivos até " + arquivoEmMB + " mb."));
					return;
				}

				criarPastaArquivos();
				String caminhoCompletoTxt = pastaArquivos + "/" + arquivoEnviado.getFileName();
				ArquivoEnviadoDTO.caminhoCompletoArquivoEnviado = caminhoCompletoTxt;
				File caminho = new File(caminhoCompletoTxt);
				InputStream recebido = arquivoEnviado.getInputStream();
				OutputStream arquivo = new FileOutputStream(caminho, false);
				recebido.transferTo(arquivo);
				caminho.deleteOnExit();
				recebido.close();
				arquivo.close();
				String nomeEExtensaoArquivoTxtParaBaixar = conversao.converteTextoEmPDF(caminhoCompletoTxt);

				if (nomeEExtensaoArquivoTxtParaBaixar.equals("naoPDF")) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"PROBLEMAS", "Seu PDF está corrompido ou tem formato inválido."));
					PrimeFaces.current().executeScript("escondeEscolherMostraConverterOutro();");
					return;
				} else if (nomeEExtensaoArquivoTxtParaBaixar.equals("erroDesconhecido")) {
					log.error("Erro desconhecido.");
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"PROBLEMAS",
							"Ocorreu um problema desconhecido em sua navegação. Volte para a página inicial e tente novamente."));
					PrimeFaces.current().executeScript("escondeEscolherMostraConverterOutro();");
					return;
				}

				caminhoCompletoArquivoParaBaixar = new File(nomeEExtensaoArquivoTxtParaBaixar);
				caminhoCompletoArquivoParaBaixar.deleteOnExit();
				PrimeFaces.current().executeScript(
						"window.document.getElementById('formPrincipal:escolher_label').style.display = 'none';");
				PrimeFaces.current().executeScript(
						"window.document.getElementById('formPrincipal:outroArquivoAoBaixar').style.display = 'flex';");
				estiloLinkDownload = estiloBotaoBaixar;
				estNomeArquivoABaixar = estVisivelNomeArquivoABaixar;
				PrimeFaces.current().ajax().update("painelCasoDownload");

				if (ArquivoEnviadoDTO.palavraLongaTxtPdf) {
					log.warn(
							"Convertido, mas existem palavras muito longas no arquivo. Não parece ser um arquivo TXT útil.");
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
							"OK",
							"Seu arquivo foi convertido, mas existem algumas linhas fora do padrão que não foram convertidas."));
					ArquivoEnviadoDTO.palavraLongaTxtPdf = false;
				} else {
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", "Seu arquivo foi convertido!"));
				}

			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"SELECIONAR ARQUIVO", "Nenhum arquivo foi selecionado."));
				PrimeFaces.current().executeScript("rejeicaoCaptcha();");
			}

		} catch (NullPointerException npe) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"ESCOLHA UM ARQUIVO", "Nenhum arquivo foi selecionado."));
			PrimeFaces.current().executeScript("rejeicaoCaptcha();");
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"PROBLEMAS",
					"Ocorreu um problema desconhecido em sua navegação. Volte para a página inicial e tente novamente."));
			e.printStackTrace();
		}

	}




	private void criarPastaArquivos() {
		File pasta = new File(pastaArquivos);

		if (pasta.exists()) {

			do {
				log.warn("A Pasta " + pastaArquivos + " já existe. Tentando criar outra.");
				pastaArquivos = diretorioEnviados + "/temp" + idCliente;
				pasta = new File(pastaArquivos);
				idCliente = IdUnicoApp.gerarIdUUID();
				pastaArquivos = diretorioEnviados + "/temp" + idCliente;
			} while (pasta.exists());

		}

		pasta.mkdir();
		pasta.setWritable(true);
		pasta.setExecutable(true);
		pasta.deleteOnExit(); // Marca arquivo para ser apagado, sempre que reiniciar a m
		filePastaArquivos = pasta;
		filePastaArquivos.deleteOnExit();
	}





	public void apagarPastaArquivos() {
		try {
			FileUtils.deleteDirectory(filePastaArquivos);
		} catch (Exception e) {
			log.error("Não foi possível apagar uma das pastas de arquivos");

			try {
				FileUtils.deleteDirectory(filePastaArquivos);
			} catch (Exception ex) {
				log.error("Não foi possível apagar uma das pastas de arquivos");
			}

		}

	}





	public void baixarArquivoConvertido() throws IOException {
		File paraBaixar = caminhoCompletoArquivoParaBaixar;
		Faces.sendFile(paraBaixar, true);
		apagarPastaArquivos();
	}





	public void pararProcessos() {
		ArquivoEnviadoDTO.pararProcesso = true;
	}
}
