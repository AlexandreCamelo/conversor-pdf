package br.com.camelodev.conversoespdf.bean;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.omnifaces.util.Faces;
import org.primefaces.PrimeFaces;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import br.com.camelodev.conversoespdf.controles.views.ConversoesGerais;
import br.com.camelodev.conversoespdf.uteis.IdUnicoApp;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope("viewJSF")
@Data
@Slf4j
public class ConvPDFDocxBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private UploadedFile arquivoEnviado;
	private String diretorioEnviados;
	private String estiloLinkDownload;
	private String pastaArquivos;
	private File filePastaArquivos;
	private String idCliente;
	private File caminhoCompletoArquivoParaBaixar;
	private String estiloBotaoBaixar;
	private String estNomeArquivoABaixar;
	private String estVisivelNomeArquivoABaixar;
	private int progressoConversao = 0;
	private String linguagemOCR = "";
	private Long tamanhoMaxArquivo = 0L;
	private boolean pararProcesso = false;
	private String nomeArquivoEnviadoDocx = "";
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

	public ConvPDFDocxBean(@Value("${conversoes.diretorio.arquivos.enviados}") String caminhoEnviados,
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
			nomeArquivoEnviadoDocx = ArquivoEnviadoDTO.nomeArquivoEnviadoParaBaixar;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}





	public void converterPDFParaDocx() {
		ArquivoEnviadoDTO.pararProcesso = false; // não tirar daqui
		int caracteresTexto = 0;

		if (!captcha.quebraCabecasResolvido()) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRADO", "Favor resolver o quebra cabeças."));
			PrimeFaces.current().executeScript("rejeicaoCaptcha();");
			return;
		}
		
		
		try {
			String nomeArquivo = arquivoEnviado.getFileName();
			ArquivoEnviadoDTO.uplArquivoEnviado = arquivoEnviado;
			String nomeArquivoTextoComExt = arquivoEnviado.getFileName() + ".docx";
			String caminhoENomeArqBaixar = "";
			ArquivoEnviadoDTO.nomeArquivoEnviado = nomeArquivo;
			ArquivoEnviadoDTO.nomeArquivoEnviadoParaBaixar = nomeArquivo + ".docx";
			nomeArquivoEnviadoDocx = ArquivoEnviadoDTO.nomeArquivoEnviadoParaBaixar;
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
				String strPastaParaImagens = pastaArquivos + "/" + "imagens";
				ArquivoEnviadoDTO.caminhoCompletoArquivoEnviado = caminhoCompletoPDF;
				File caminho = new File(caminhoCompletoPDF);
				InputStream recebido = arquivoEnviado.getInputStream();
				OutputStream arquivo = new FileOutputStream(caminho, false);
				recebido.transferTo(arquivo);
				caracteresTexto = PDFEmWord(caminhoCompletoPDF, arquivoEnviado.getFileName(), "jpeg",
						strPastaParaImagens);
				ArquivoEnviadoDTO.totalCaracteresArquivoGerado = caracteresTexto;
				caminho.deleteOnExit();
				recebido.close();
				arquivo.close();

				if (caracteresTexto > 0 && caracteresTexto < 50 ) {
					PrimeFaces.current().executeScript("PF('dlgOCR').show();");
					return;
				}else if (caracteresTexto <0) {
					return;
				}

				caminhoENomeArqBaixar = pastaArquivos + "/" + nomeArquivoTextoComExt;
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
			}

		} catch (NullPointerException npe) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"ESCOLHA UM ARQUIVO", "Nenhum arquivo foi selecionado."));
			PrimeFaces.current().executeScript("rejeicaoCaptcha();");
		} 
		catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"PROBLEMAS", "Ocorreu um problema desconhecido. Volte para a página inicial e tente novamente."));
			e.printStackTrace();
		}

	}





	private int PDFEmWord(String caminhoCompletoarquivoPDF, String nomeEExtensaoArquivoPDF, String extensaoDaImagem,
			String pastaOndeColocarImagens) {
		ArquivoEnviadoDTO.pararProcesso = false; // não tirar daqui

		try {
			@SuppressWarnings("resource")
			XWPFDocument doc = new XWPFDocument();
			String pdf = caminhoCompletoarquivoPDF;
			PdfReader reader = new PdfReader(pdf);
			PdfReaderContentParser parser = new PdfReaderContentParser(reader);
			int tamanhoTexto = 0;

			for (int i = 1; i <= reader.getNumberOfPages(); i++) {
				TextExtractionStrategy strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
				String text = strategy.getResultantText();
				tamanhoTexto += text.length();
				XWPFParagraph p = doc.createParagraph();
				XWPFRun run = p.createRun();
				run.setText(text);
				run.addBreak(BreakType.PAGE);
			}

			String caminhoSaida = caminhoCompletoarquivoPDF + ".docx";
			FileOutputStream out = new FileOutputStream(caminhoSaida);
			doc.write(out);
			doc.close();
			return tamanhoTexto;
		} catch (InvalidPdfException ePdf) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"PROBLEMAS", "Seu PDF está corrompido ou tem formato inválido."));
			PrimeFaces.current().executeScript("escondeEscolherMostraConverterOutro();");
			return -1;
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"PROBLEMAS", "Houve algum erro desconhecido em sua navegação. Favor retornar à página inicial e tentar novamente."));
			e.printStackTrace();
			return -1;
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
			@SuppressWarnings("resource")
			XWPFDocument doc = new XWPFDocument();

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
				XWPFParagraph p = doc.createParagraph();
				XWPFRun run = p.createRun();
				run.setText(textoExtraido);
				run.addBreak(BreakType.PAGE);
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

			String caminhoSaida = caminhoCompletoarquivoPDF + ".docx";
			FileOutputStream out = new FileOutputStream(caminhoSaida);
			doc.write(out);
			doc.close();
			document.close();
			doc.close();
			return "Ok";
		} catch (Exception e) {
			e.printStackTrace();
			return "Não conseguimos identificar texto na página: " + paginaAtual;
		}

	}


	public void converterPDFParaDocxSemOCR() {
		ArquivoEnviadoDTO.pararProcesso = false; // não tirar daqui

		try {
			String caminhoCompletoPDF = ArquivoEnviadoDTO.caminhoCompletoArquivoEnviado;
			File filePDF = new File(caminhoCompletoPDF);
			String nomeArquivo = filePDF.getName();
			String nomeArquivoTextoComExt = nomeArquivo + ".docx";
			String caminhoENomeArqBaixar = "";
			ArquivoEnviadoDTO.nomeArquivoEnviadoParaBaixar = nomeArquivoTextoComExt;
			nomeArquivoEnviadoDocx = ArquivoEnviadoDTO.nomeArquivoEnviadoParaBaixar;
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

				String strPastaParaImagens = pastaArquivos + "/" + "imagens";
				PDFEmWord(caminhoCompletoPDF, arquivoEnviado.getFileName(), "jpeg", strPastaParaImagens);
				caminhoENomeArqBaixar = pastaArquivos + "/" + nomeArquivoTextoComExt;
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
			String nomeArqTextoCExt = nomeArquivo + ".docx";
			String caminhoCompletoPDF = pastaArquivos + "/" + nomeArquivo;
			String strPastaParaImagens = pastaArquivos + "/" + "imagens";
			String textoGeral = PDFEmImagemEfazOCR(caminhoCompletoPDF, nomeArquivo, "jpeg", strPastaParaImagens);

			if (!textoGeral.equals("Ok")) {
				progresso.setProgBarra(null);
				PrimeFaces.current().executeScript("PF('dlgBarraProgresso').hide();");
				PrimeFaces.current().executeScript("redirecionarParaAMesmaPagina();");
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "SEM TEXTO", "Nenhum texto foi reconhecido."));
				// Zera variável, para que combobox apareça em branco, na próxima conversão
				linguagemOCR = "";
				return;
			}

			String caminhoENomeArqBaixar = pastaArquivos + "/" + nomeArqTextoCExt;
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
		} catch (Exception e) {

			if (e.getMessage().contains("contain versioninfo")) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"PROBLEMAS", "Seu PDF está corrompido ou tem formato inválido."));
			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "PROBLEMAS",
								"Ocorreu um problema desconhecido. Volte para a página inicial e tente novamente."));
				e.printStackTrace();
			}

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
