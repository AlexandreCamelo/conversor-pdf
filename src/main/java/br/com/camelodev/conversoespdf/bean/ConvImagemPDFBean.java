package br.com.camelodev.conversoespdf.bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.omnifaces.util.Faces;
import org.primefaces.PrimeFaces;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;

import br.com.camelodev.conversoespdf.controles.views.ConversoesGerais;
import br.com.camelodev.conversoespdf.uteis.IdUnicoApp;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Scope("viewJSF")
@Named
@Data
@Slf4j
public class ConvImagemPDFBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private UploadedFiles arquivosEnviados;
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
	private Double aumentar = 0.0;
	private Double diminuir = 0.0;
	private boolean umaImagemPorPagina;
	String valorQuebraCabecas = "";
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

	// Construtor
	public ConvImagemPDFBean(@Value("${conversoes.diretorio.arquivos.enviados}") String caminhoEnviados,
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
			nomeArquivoEnviadoJpegOuZip = ArquivoEnviadoDTO.nomeArquivoEnviado;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}





	public void aumentarImagemMais() {
		if (diminuir != 0)
			diminuir = 0.0;
		aumentar = aumentar + 0.1;
	}





	public void aumentarImagemMenos() {
		if (diminuir != 0)
			diminuir = 0.0;
		aumentar = aumentar - 0.1;
		if (aumentar < 0)
			aumentar = 0.0;
	}





	public void diminuirImagemMais() {
		if (aumentar != 0)
			aumentar = 0.0;
		diminuir = diminuir + 0.1;
	}





	public void diminuirImagemMenos() {
		if (aumentar != 0)
			aumentar = 0.0;
		diminuir = diminuir - 0.1;
		if (diminuir < 0)
			diminuir = 0.0;
	}





	public void converterFotosParaPDF() {
		ArquivoEnviadoDTO.pararProcesso = false; // não tirar daqui

		if (!captcha.quebraCabecasResolvido()) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRADO", "Favor resolver o quebra cabeças."));
			PrimeFaces.current().executeScript("rejeicaoCaptcha();");
			return;
		}

		try {
			List<String> lstCaminhosArquivos = new ArrayList<>();
			long tamanhoTotal = arquivosEnviados.getSize();

			for (UploadedFile foto : arquivosEnviados.getFiles()) {
				String nomeArquivo = foto.getFileName();
				ArquivoEnviadoDTO.uplArquivoEnviado = foto;
				long arquivoEmMB = tamanhoMaxArquivo / 1000000;

				if (foto != null && !nomeArquivo.isBlank()) {
					String tipoArquivo = foto.getContentType();

					if (tamanhoTotal > tamanhoMaxArquivo) {
						FacesContext.getCurrentInstance().addMessage(null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, "MAIOR QUE O PERMITIDO",
										"A soma das fotos não pode ultrapassar " + arquivoEmMB + " mb."));
						return;
					} else if (!arquivoEDoTipoPermitido(tipoArquivo)) {
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
								"TIPOS NÃO PERMITIDOS",
								"Existem arquivos não permitidos na sua lista. Tipos de arquivo permitidos: JPG, JPEG, BMP, GIF e TIFF"));
						PrimeFaces.current().executeScript("rejeicaoCaptcha();");
						return;
					}

				} else {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"SELECIONAR ARQUIVO", "Nenhum arquivo foi selecionado."));
					PrimeFaces.current().executeScript("rejeicaoCaptcha();");
					return;
				}

			}

			criarPastaArquivos();

			for (UploadedFile foto2 : arquivosEnviados.getFiles()) {
				ArquivoEnviadoDTO.uplArquivoEnviado = foto2;
				String caminhoCompletoFoto2 = pastaArquivos + "/" + foto2.getFileName();
				ArquivoEnviadoDTO.caminhoCompletoArquivoEnviado = caminhoCompletoFoto2;
				File caminho2 = new File(caminhoCompletoFoto2);
				InputStream recebido2 = foto2.getInputStream();
				OutputStream arquivo2 = new FileOutputStream(caminho2, false);
				recebido2.transferTo(arquivo2);
				caminho2.deleteOnExit();
				recebido2.close();
				arquivo2.close();
				lstCaminhosArquivos.add(caminhoCompletoFoto2);
			}

			nomeArquivoEnviadoJpegOuZip = "fotos.pdf";
			conversao.converteVariasImagemEmPDF(lstCaminhosArquivos, aumentar, diminuir, umaImagemPorPagina,
					pastaArquivos + "/" + "fotos.pdf");
			caminhoCompletoArquivoParaBaixar = new File(pastaArquivos + "/" + "fotos.pdf");
			caminhoCompletoArquivoParaBaixar.deleteOnExit();
			PrimeFaces.current().executeScript(
					"window.document.getElementById('formPrincipal:escolher_label').style.display = 'none';");
			PrimeFaces.current().executeScript(
					"window.document.getElementById('formPrincipal:outroArquivoAoBaixar').style.display = 'flex';");
			estiloLinkDownload = estiloBotaoBaixar;
			estNomeArquivoABaixar = estVisivelNomeArquivoABaixar;
			PrimeFaces.current().ajax().update("painelCasoDownload");
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", "Seu arquivo foi convertido!"));
		} catch (NullPointerException npe) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"ESCOLHA UM ARQUIVO", "Nenhum arquivo foi selecionado."));
			log.warn("Tentativa de enviar, sem escolher o arquivo");
			PrimeFaces.current().executeScript("rejeicaoCaptcha();");
		} catch (IOException e) {

			if (e.getMessage().contains("not a recognized imageformat")) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"NÃO RECONHECIDO", "O formato do seu arquivo não parece ser de uma imagem."));
				PrimeFaces.current().executeScript("rejeicaoCaptcha();");
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"PROBLEMAS",
						"Ocorreu um problema desconhecido em sua navegação. Volte para a página inicial e tente novamente."));
			}

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"PROBLEMAS",
					"Ocorreu um problema desconhecido em sua navegação. Volte para a página inicial e tente novamente."));
			e.printStackTrace();
		}

	}





	private boolean arquivoEDoTipoPermitido(String tipo) {
		if (!tipo.equals("image/jpeg") && !tipo.equals("image/bmp") && !tipo.equals("image/gif")
				&& !tipo.equals("image/tiff")) {
			return false;
		} else {
			return true;
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
		pasta.deleteOnExit();
		filePastaArquivos = pasta;
		filePastaArquivos.deleteOnExit();
	}





	public void apagarPastaArquivos() {
		try {
			FileUtils.deleteDirectory(filePastaArquivos);
		} catch (Exception e) {
			log.warn("Não foi possível apagar uma das pastas de arquivos");

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
