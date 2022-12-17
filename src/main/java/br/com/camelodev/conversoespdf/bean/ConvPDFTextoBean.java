package br.com.camelodev.conversoespdf.bean;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;

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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope("viewJSF")
@Data
@Slf4j
public class ConvPDFTextoBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private UploadedFile arquivoEnviado;
	private StreamedContent arquivoParaBaixarVindoDeFora;
	private String diretorioEnviados;
	private String caminhoCompletoTXT;
	private String estiloLinkDownload;
	private boolean fazerOCR = false;
	private String pastaArquivosEnviados;
	private String pastaArquivos;
	private File filePastaArquivos;
	private File filePastaArquivosCONVERTIDO;
	private String idCliente;
	private String arquivoMarcadorDeExclusaoDePasta = "excluir.txt";
	private File caminhoCompletoArquivoParaBaixar;
	private String estiloBotaoBaixar;
	private String estNomeArquivoABaixar;
	private String estVisivelNomeArquivoABaixar;
	private final long _10MB = 10000000;
	private final long _15MB = 15000000;
	private final long _100MB = 100000000;
	private final long _200MB = 200000000;
	private int progressoConversao = 0;
	private String linguagemOCR = "";
	private Long tamanhoMaxArquivo = 0L;
	private boolean pararProcesso = false;
	private boolean ehPraExcluir = false;
	private String nomeArquivoEnviadoTxt = "";
	@Autowired
	ConversoesGerais conversao;
	@Autowired
	Ocr reconhecimento;
	@Autowired
	ProgressoBarra progresso;
	@Autowired
	ArquivoParaBaixarDTO paraBaixar;
	@Autowired
	CamelCaptchaBean cameloCaptcha;

	public ConvPDFTextoBean(@Value("${conversoes.diretorio.arquivos.enviados}") String caminhoEnviados,
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
			caminhoCompletoTXT = pastaArquivos + "CONVERTIDO/convertidoParaTexto.txt";
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
			nomeArquivoEnviadoTxt = ArquivoEnviadoDTO.nomeArquivoEnviado + ".txt";
		} catch (Exception e) {
			e.printStackTrace();
		}

	}





	public void converterPDFParaTexto() {
		ArquivoEnviadoDTO.pararProcesso = false; // não tirar daqui
		int caracteresTexto = 0;

		if (!cameloCaptcha.quebraCabecasResolvido()) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRADO", "Favor resolver o quebra cabeças."));
			PrimeFaces.current().executeScript("rejeicaoCaptcha();");
			return;
		}

		try {
			String nomeArquivo = arquivoEnviado.getFileName();
			ArquivoEnviadoDTO.uplArquivoEnviado = arquivoEnviado;
			String nomeArquivoTextoComExt = arquivoEnviado.getFileName() + ".txt";
			String caminhoENomeArqBaixar = "";
			ArquivoEnviadoDTO.nomeArquivoEnviado = nomeArquivo;
			nomeArquivoEnviadoTxt = nomeArquivo + ".txt";
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
				InputStream recebido = arquivoEnviado.getInputStream();
				OutputStream arquivo = new FileOutputStream(caminho, false);
				recebido.transferTo(arquivo);
				caracteresTexto = conversao.convertePDFemTexto(caminhoCompletoPDF, nomeArquivo,
						pastaArquivos + "CONVERTIDO");
				ArquivoEnviadoDTO.totalCaracteresArquivoGerado = caracteresTexto;
				filePastaArquivosCONVERTIDO = new File(pastaArquivos + "CONVERTIDO");
				caminho.deleteOnExit();
				filePastaArquivosCONVERTIDO.deleteOnExit();
				recebido.close();
				arquivo.close();

				if (caracteresTexto > 0 && caracteresTexto < 50) {
					PrimeFaces.current().executeScript("PF('dlgOCR').show();");
					return;
				} else if (caracteresTexto < 0) {
					return;
				}

				caminhoENomeArqBaixar = pastaArquivos + "CONVERTIDO/" + nomeArquivoTextoComExt;
				caminhoCompletoArquivoParaBaixar = new File(caminhoENomeArqBaixar);
				caminhoCompletoArquivoParaBaixar.deleteOnExit();
				PrimeFaces.current().executeScript("PF('dlgBarraEterna').hide();");
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
		} catch (InvalidPdfException ePdf) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"PROBLEMAS", "Seu PDF está corrompido ou tem formato inválido."));
			PrimeFaces.current().executeScript("rejeicaoCaptcha();");
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"PROBLEMAS", "Ocorreu um problema desconhecido. Volte para a página inicial e tente novamente."));
			e.printStackTrace();
		}

	}









	public void converterPDFParaTextoSemOCR() {
		ArquivoEnviadoDTO.pararProcesso = false; // não tirar daqui

		try {
			String caminhoCompletoPDF = ArquivoEnviadoDTO.caminhoCompletoArquivoEnviado;
			File filePDF = new File(caminhoCompletoPDF);
			String nomeArquivo = filePDF.getName();
			String nomeArquivoTextoComExt = nomeArquivo + ".txt";
			String caminhoENomeArqBaixar = "";
			nomeArquivoEnviadoTxt = nomeArquivo + ".txt";
			long tamanhoArquivo = ArquivoEnviadoDTO.uplArquivoEnviado.getSize();

			if (!nomeArquivo.isBlank()) {
				String tipoArquivo = ArquivoEnviadoDTO.uplArquivoEnviado.getContentType();

				if (!tipoArquivo.equals("application/pdf")) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"SÓ ARQUIVOS PDF", "Seu arquivo não é do tipo PDF."));
					return;
				} else if (tamanhoArquivo > tamanhoMaxArquivo) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"ARQUIVO MUITO GRANDE", "Somente arquivos até 100 mb."));
					return;
				}

				conversao.convertePDFemTexto(caminhoCompletoPDF, nomeArquivo, pastaArquivos + "CONVERTIDO");
				filePastaArquivosCONVERTIDO = new File(pastaArquivos + "CONVERTIDO");
				filePDF.deleteOnExit();
				filePastaArquivosCONVERTIDO.deleteOnExit();
				caminhoENomeArqBaixar = pastaArquivos + "CONVERTIDO/" + nomeArquivoTextoComExt;
				caminhoCompletoArquivoParaBaixar = new File(caminhoENomeArqBaixar);
				caminhoCompletoArquivoParaBaixar.deleteOnExit();
				PrimeFaces.current().executeScript("PF('dlgBarraEterna').hide();");
				PrimeFaces.current().executeScript(
						"window.document.getElementById('formPrincipal:escolher_label').style.display = 'none';");
				PrimeFaces.current().executeScript(
						"window.document.getElementById('formPrincipal:outroArquivoAoBaixar').style.display = 'flex';");
				estiloLinkDownload = estiloBotaoBaixar;
				estNomeArquivoABaixar = estVisivelNomeArquivoABaixar;
				PrimeFaces.current().ajax().update("painelCasoDownload");

				if (ArquivoEnviadoDTO.totalCaracteresArquivoGerado <= 0) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
							"SEM TEXTO", "Seu arquivo texto foi gerado, mas está em branco."));
				} else {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
							"OK", "Seu arquivo foi convertido. Confirme se todo o texto foi reconhecido."));
				}

			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"SELECIONAR ARQUIVO", "Nenhum arquivo foi selecionado."));
			}

		} catch (NullPointerException npE) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"SELECIONAR ARQUIVO", "Nenhum arquivo foi selecionado."));
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"ERRO NO SERVIDOR", "Favor fechar a aba e abrir uma outra."));
			e.printStackTrace();
		}

	}





	public void fazOCRArquivo() {
		ArquivoEnviadoDTO.pararProcesso = false; // não tirar daqui

		try {

			if (linguagemOCR.isBlank()) {
				PrimeFaces.current().executeScript("refazTelaOCR();");
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "IDIOMA", "Informe o idioma do texto."));
				return;
			}

			PrimeFaces.current().executeScript("PF('dlgBarraProgresso').show();");
			progresso.setProgBarra(2);
			String nomeArquivo = ArquivoEnviadoDTO.nomeArquivoEnviado;
			String nomeArqTextoCExt = nomeArquivo + ".txt";
			String caminhoCompletoPDF = pastaArquivos + "/" + nomeArquivo;
			String caminhoTXT = pastaArquivos + "CONVERTIDO/" + nomeArqTextoCExt;
			String strPastaParaImagens = pastaArquivos + "/" + "imagens";
			String textoGeral = PDFEmImagemEfazOCR(caminhoCompletoPDF, nomeArquivo, "jpeg", strPastaParaImagens);

			if (textoGeral.isBlank()) {
				progresso.setProgBarra(null);
				PrimeFaces.current().executeScript("PF('dlgBarraProgresso').hide();");
				PrimeFaces.current().executeScript("redirecionarParaAMesmaPagina();");
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "SEM TEXTO", "Nenhum texto foi reconhecido."));
				// Zera variável, para que combobox apareça em branco, na próxima conversão
				linguagemOCR = "";
				return;
			}

			PrintWriter pw;
			pw = new PrintWriter(caminhoTXT);
			pw.print(textoGeral);
			pw.close();
			String caminhoENomeArqBaixar = pastaArquivos + "CONVERTIDO/" + nomeArqTextoCExt;
			caminhoCompletoArquivoParaBaixar = new File(caminhoENomeArqBaixar);
			caminhoCompletoArquivoParaBaixar.deleteOnExit();
			progresso.setProgBarra(null);
			PrimeFaces.current().executeScript("PF('dlgBarraProgresso').hide();");
			PrimeFaces.current().executeScript(
					"window.document.getElementById('formPrincipal:escolher_label').style.display = 'none';");
			PrimeFaces.current().executeScript(
					"window.document.getElementById('formPrincipal:outroArquivoAoBaixar').style.display = 'flex';");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Concluído!", "Seu texto foi reconhecido com sucesso."));
			estiloLinkDownload = estiloBotaoBaixar;
			estNomeArquivoABaixar = estVisivelNomeArquivoABaixar;
			PrimeFaces.current().ajax().update("painelCasoDownload");
			PrimeFaces.current()
					.executeScript("document.getElementById('formPrincipal:escolher_label').style.display = 'none'");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}





	private String PDFEmImagemEfazOCR(String caminhoCompletoarquivoPDF, String nomeEExtensaoArquivoPDF,
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
			String textoDaImagem = "";

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
				// Então, após a conversão em imagem, pega-se o texto, através de OCR e apaga-se
				// a imagem
				String textoExtraido = reconhecimento.OCRdeImagem(nomeArquivo, linguagemOCR, pagina.toString());
				textoDaImagem = textoDaImagem + "\n\n" + textoExtraido;
				File imagemAApagar = new File(nomeArquivo);
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
			return textoDaImagem;
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
			FileUtils.deleteDirectory(filePastaArquivosCONVERTIDO);
		} catch (Exception e) {
			log.error("Não foi possível apagar uma das pastas de arquivos");

			try {
				FileUtils.deleteDirectory(filePastaArquivosCONVERTIDO);
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



	public void mostrarBotaoOCR() {

		if (linguagemOCR.isBlank()) {
			PrimeFaces.current().executeScript(
					"window.document.getElementById('formPrincipal:btnFazOCR2').style.display = 'none';");
		} else {
			PrimeFaces.current().executeScript(
					"window.document.getElementById('formPrincipal:btnFazOCR2').style.display = 'flex';");
		}

	}
}
