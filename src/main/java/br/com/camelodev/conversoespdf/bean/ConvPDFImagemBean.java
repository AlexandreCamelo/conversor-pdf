package br.com.camelodev.conversoespdf.bean;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.zip.ZipOutputStream;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.omnifaces.util.Faces;
import org.primefaces.PrimeFaces;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.itextpdf.text.exceptions.InvalidPdfException;

import br.com.camelodev.conversoespdf.controles.views.ConversoesGerais;
import br.com.camelodev.conversoespdf.uteis.IdUnicoApp;
import br.com.camelodev.conversoespdf.uteis.ZiparArquivos;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope("viewJSF")
@Data
@Slf4j
public class ConvPDFImagemBean implements Serializable {
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
	private boolean pararProcesso = false;
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

	public ConvPDFImagemBean(@Value("${conversoes.diretorio.arquivos.enviados}") String caminhoEnviados,
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





	public void converterPDFParaImagem() {
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

				if (!tipoArquivo.equals("application/pdf")) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"SÓ ARQUIVOS PDF", "Seu arquivo não é do tipo PDF."));
					return;
				} else if (tamanhoArquivo > tamanhoMaxArquivo) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"ARQUIVO MUITO GRANDE", "Somente arquivos até " + arquivoEmMB + " mb."));
					return;
				}

				criarPastaArquivos();
				String caminhoCompletoPDF = pastaArquivos + "/" + arquivoEnviado.getFileName();
				ArquivoEnviadoDTO.caminhoCompletoArquivoEnviado = caminhoCompletoPDF;
				File caminho = new File(caminhoCompletoPDF);
				String strPastaParaImagens = pastaArquivos + "/" + "imagens";
				InputStream recebido = arquivoEnviado.getInputStream();
				OutputStream arquivo = new FileOutputStream(caminho, false);
				recebido.transferTo(arquivo);
				caminho.deleteOnExit();
				recebido.close();
				arquivo.close();
				String nomeEExtensaoArquivoZIPParaBaixar = PDFEmImagem(caminhoCompletoPDF, arquivoEnviado.getFileName(),
						"jpeg", strPastaParaImagens);

				if (nomeEExtensaoArquivoZIPParaBaixar.equals("naoPDF")) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"PROBLEMAS", "Seu PDF está corrompido ou tem formato inválido."));
					PrimeFaces.current().executeScript("escondeEscolherMostraConverterOutro();");
					return;
				}

				caminhoCompletoArquivoParaBaixar = new File(nomeEExtensaoArquivoZIPParaBaixar);
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
					"PROBLEMAS", "Ocorreu um problema desconhecido. Volte para a página inicial e tente novamente."));
			e.printStackTrace();
		}

	}





	private String PDFEmImagem(String caminhoCompletoarquivoPDF, String nomeEExtensaoArquivoPDF,
			String extensaoDaImagem, String pastaOndeColocarImagens) {
		String paginaAtual = "";
		ArquivoEnviadoDTO.pararProcesso = false; // não tirar daqui

		try {
			File pastaImagens = new File(pastaOndeColocarImagens);
			pastaImagens.mkdirs();
			pastaImagens.deleteOnExit();
			PDDocument document = PDDocument.load(new File(caminhoCompletoarquivoPDF));
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			int totalDePaginas = document.getNumberOfPages();
			FileOutputStream fileOutputStream = new FileOutputStream(
					pastaOndeColocarImagens + "/" + nomeEExtensaoArquivoPDF + ".zip");
			ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

			if (totalDePaginas > 1) {
				ArquivoEnviadoDTO.nomeArquivoEnviado = arquivoEnviado.getFileName() + ".zip";
				nomeArquivoEnviadoJpegOuZip = ArquivoEnviadoDTO.nomeArquivoEnviado;
			} else if (totalDePaginas == 1) {
				progresso.setProgBarra(90);
				ArquivoEnviadoDTO.nomeArquivoEnviado = arquivoEnviado.getFileName() + "." + extensaoDaImagem;
				nomeArquivoEnviadoJpegOuZip = ArquivoEnviadoDTO.nomeArquivoEnviado;
				BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
				String caminhoCompletoArquivo = pastaOndeColocarImagens + "/" + nomeEExtensaoArquivoPDF + "."
						+ extensaoDaImagem;
				ImageIOUtil.writeImage(bim, caminhoCompletoArquivo, 300);
				document.close();
				progresso.setProgBarra(100);
				zipOutputStream.close();
				fileOutputStream.close();
				return caminhoCompletoArquivo;
			} else {
				zipOutputStream.close();
				fileOutputStream.close();
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "NADA", "Não existe conteúdo no seu arquivo."));
				return "";
			}

			for (Integer pagina = 0; pagina < totalDePaginas; ++pagina) {

				if (ArquivoEnviadoDTO.pararProcesso) {
					document.close();
					apagarPastaArquivos();
					ArquivoEnviadoDTO.pararProcesso = false;
					return "";
				}

				if (totalDePaginas <= 2) {
					progresso.setProgBarra(80);
				}

				paginaAtual = pagina.toString();
				BufferedImage bim = pdfRenderer.renderImageWithDPI(pagina, 300, ImageType.RGB);
				// É importante colocar essa parte da string: (page<10?"0":""), pois o
				// Arrays.sort Só conseguirá ordenar os nomes dos arquivos, corretamente, com os
				// ZEROS na
				// frente dos números menores que 10.
				// Se ele não ordenar corretamente, o sistema gerará um arquivo texto sem uma
				// ordem compreensível
				String nomeArquivo = pastaOndeColocarImagens + "/" + nomeEExtensaoArquivoPDF + (pagina < 10 ? "0" : "")
						+ pagina + "." + extensaoDaImagem;
				ImageIOUtil.writeImage(bim, nomeArquivo, 300);
				File imagemAApagar = new File(nomeArquivo);

				if (pagina == (totalDePaginas - 1)) {
					ZiparArquivos.ziparArquivoUmAUm(imagemAApagar, true, fileOutputStream, zipOutputStream);
				} else {
					ZiparArquivos.ziparArquivoUmAUm(imagemAApagar, false, fileOutputStream, zipOutputStream);
				}

				imagemAApagar.delete();
				imagemAApagar.deleteOnExit();
				Double dblPorcent = Double.valueOf(pagina) / Double.valueOf(totalDePaginas) * 100;
				Integer porcentagem = (int) Math.round(dblPorcent);

				if (porcentagem <= 2) {
					progresso.setProgBarra(2);
				} else {
					progresso.setProgBarra(porcentagem);
				}

			}

			document.close();
			return pastaArquivos + "/imagens/" + nomeEExtensaoArquivoPDF + ".zip";
		} catch (InvalidPdfException ePdf) {
			return "naoPDF";
		} catch (IOException ioe) {
			return "naoPDF";
		} catch (Exception e) {
			e.printStackTrace();
			return "Não conseguimos identificar texto na página: " + paginaAtual;
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
