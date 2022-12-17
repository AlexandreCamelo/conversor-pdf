package br.com.camelodev.conversoespdf.controles.views;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.fit.pdfdom.PDFDomTree;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

import br.com.camelodev.conversoespdf.bean.ArquivoEnviadoDTO;
import br.com.camelodev.conversoespdf.bean.Ocr;
import br.com.camelodev.conversoespdf.bean.ProgressoBarra;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope("viewJSF")
@Data
@Slf4j
public class ConversoesGerais implements Serializable {
	private static final long serialVersionUID = -7482078232500407883L;
	@Autowired
	Ocr reconhecimento;
	@Autowired
	ProgressoBarra progresso;
	@Value("${conversoes.diretorio.arquivos.enviados}")
	private String pastaArquivosEnviados;

	public static String convertePDFemWord(String filename) {

		try {
			XWPFDocument doc = new XWPFDocument();
			String pdf = filename;
			PdfReader reader = new PdfReader(pdf);
			PdfReaderContentParser parser = new PdfReaderContentParser(reader);

			for (int i = 1; i <= reader.getNumberOfPages(); i++) {
				TextExtractionStrategy strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
				@SuppressWarnings("unused")
				TextRenderInfo informacaoTexto;
				String text = strategy.getResultantText();
				XWPFParagraph p = doc.createParagraph();
				XWPFRun run = p.createRun();
				run.setText(text);
				run.addBreak(BreakType.PAGE);
			}

			String caminhoSaida = filename + ".docx";
			FileOutputStream out = new FileOutputStream(caminhoSaida);
			doc.write(out);
			doc.close();
			return caminhoSaida;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

	}





	public void convertePDFEmImagem(String caminhoCompletoarquivoPDF, String nomeEExtensaoArquivoPDF,
			String extensaoDaImagem, String pastaOndeColocarImagens) {

		try {
			File pastaImagens = new File(pastaOndeColocarImagens);
			pastaImagens.mkdirs();
			PDDocument document = PDDocument.load(new File(caminhoCompletoarquivoPDF));
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			int totalDePaginas = document.getNumberOfPages();

			for (Integer pagina = 0; pagina < totalDePaginas; ++pagina) {
				BufferedImage bim = pdfRenderer.renderImageWithDPI(pagina, 300, ImageType.RGB);
				// É importante colocar essa parte da string: (page<10?"0":""), pois o
				// Arrays.sort
				// Só conseguirá ordenar os nomes dos arquivos, corretamente, com os ZEROS na
				// frente dos
				// números menores que 10.
				// Se ele não ordenar corretamente, o sistema gerará um arquivo texto sem uma
				// ordem compreensível
				String nomeArquivo = pastaOndeColocarImagens + "/" + nomeEExtensaoArquivoPDF + (pagina < 10 ? "0" : "")
						+ pagina + "." + extensaoDaImagem;
				ImageIOUtil.writeImage(bim, nomeArquivo, 300);
			}

			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}





	@SuppressWarnings("resource")
	public String converteTextoEmPDF(String caminhoCompletoArquivoComExtensao) {
		Document pdfDoc = new Document(PageSize.A4);

		try {
			PdfWriter.getInstance(pdfDoc, new FileOutputStream(caminhoCompletoArquivoComExtensao + ".pdf"))
					.setPdfVersion(PdfWriter.PDF_VERSION_1_7);
			pdfDoc.open();
			Font myfont = new Font();
			myfont.setStyle(Font.NORMAL);
			myfont.setSize(11);
			pdfDoc.add(new Paragraph("\n"));
			BufferedReader contar = new BufferedReader(new FileReader(caminhoCompletoArquivoComExtensao));
			Long linhas = contar.lines().count();
			Long linha = 0L;
			BufferedReader br = new BufferedReader(new FileReader(caminhoCompletoArquivoComExtensao));
			String strLine;

			while ((strLine = br.readLine()) != null) {

				if (ArquivoEnviadoDTO.pararProcesso == true) {
					return "erroDesconhecido";
				}

				int porcentEspacos = 0;
				int qtdeEspacos = StringUtils.countOccurrencesOf(strLine, " ");

				if (strLine.length() > 0) {
					porcentEspacos = qtdeEspacos / strLine.length() * 100;
				}

				// É **EXTREMAMENTE** NECESSÁRIO FAZER ESSE TESTE, AQUI. LEIA O TEXTO, NO FIM
				// DESSE ARQUIVO.
				if (strLine.length() >= 200 && strLine.indexOf(" ") <= 0) {
					ArquivoEnviadoDTO.palavraLongaTxtPdf = true;
					continue;
				} else if (strLine.length() >= 200 && porcentEspacos <= 1) {
					ArquivoEnviadoDTO.palavraLongaTxtPdf = true;
					continue;
				}

				linha++;
				Paragraph para = new Paragraph(strLine + "\n", myfont);
				para.setAlignment(Element.ALIGN_JUSTIFIED);

				if (!strLine.isBlank()) {
					pdfDoc.add(para);
				}

				Double dblPorcent = Double.valueOf(linha) / Double.valueOf(linhas) * 100;
				Integer porcentagem = (int) Math.round(dblPorcent);

				if (porcentagem <= 2) {
					progresso.setProgBarra(2);
				} else {
					progresso.setProgBarra(porcentagem);
				}

			}

			pdfDoc.close();
			br.close();
			return caminhoCompletoArquivoComExtensao + ".pdf";
		} catch (InvalidPdfException ePdf) {
			return "naoPDF";
		} catch (IOException ioe) {
			return "naoPDF";
		} catch (DocumentException de) {
			de.printStackTrace();
			return "erroDesconhecido";
		} catch (Exception e) {
			e.printStackTrace();
			return "erroDesconhecido";
		}

	}





	public int convertePDFemTexto(String caminhoCompletoArquivoPDF, String nomePuroArquivoSemExtensao,
			String localParaSalvarArquivoTexto) {
		String parsedText = "";

		try {
			File f = new File(caminhoCompletoArquivoPDF);
			f.deleteOnExit();
			String novaPastaParaArquivoTexto = localParaSalvarArquivoTexto;
			File pastaParaArquivoTexto = new File(novaPastaParaArquivoTexto);
			pastaParaArquivoTexto.mkdir();
			pastaParaArquivoTexto.setExecutable(true, false);
			pastaParaArquivoTexto.setWritable(true, false);
			pastaParaArquivoTexto.setReadable(true, false);
			pastaParaArquivoTexto.deleteOnExit();
			PDFParser parser = new PDFParser(new RandomAccessFile(f, "r"));
			parser.parse();
			COSDocument cosDoc = parser.getDocument();
			PDFTextStripper pdfStripper = new PDFTextStripper();
			PDDocument pdDoc = new PDDocument(cosDoc);
			parsedText = pdfStripper.getText(pdDoc);
			PrintWriter pw = new PrintWriter(novaPastaParaArquivoTexto + "/" + nomePuroArquivoSemExtensao + ".txt");
			pw.print(parsedText);
			pw.flush();
			pw.close();
			cosDoc.close();
			pdDoc.close();

			if (parsedText.isBlank()) {
				return 0;
			} else {
				return parsedText.length();
			}

		} catch (InvalidPdfException ePdf) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"PROBLEMAS", "Seu PDF está corrompido ou tem formato inválido."));
			return -1;
		} catch (IOException e) {

			if (e.getMessage().contains("End-of-File")) {
				log.error("ARQUIVO TEXTO GERADO EM BRANCO.");
			} else if (e.getMessage().contains("contain versioninfo")) {
				log.error("ARQUIVO NÃO É PDF.");
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"PROBLEMAS", "Seu PDF está corrompido ou tem formato inválido."));
				PrimeFaces.current().executeScript("escondeEscolherMostraConverterOutro();");
			} else {
				e.printStackTrace();
			}

			return -1;
		} catch (Exception ex) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"PROBLEMAS", "Seu PDF está corrompido ou tem formato inválido."));
			return -1;
		}

	}





	public void converteImagemEmPDF(String caminhoCompletoImagemComExtensao, double aumentar,
			double diminuir) {

		try {
			Document document = new Document();
			String input = caminhoCompletoImagemComExtensao;
			String output = caminhoCompletoImagemComExtensao + ".pdf";
			FileOutputStream fos = new FileOutputStream(output);
			PdfWriter writer = PdfWriter.getInstance(document, fos);
			Image imagem = Image.getInstance(input);
			float x = imagem.getScaledHeight();
			float y = imagem.getScaledWidth();

			if (aumentar > 0) {
				imagem.scaleAbsoluteHeight((float) (x * aumentar));
				imagem.scaleAbsoluteWidth((float) (y * aumentar));
			} else if (diminuir > 0) {
				imagem.scaleAbsoluteHeight((float) (x / diminuir));
				imagem.scaleAbsoluteWidth((float) (y / diminuir));
			}

			writer.open();
			document.open();
			document.add(imagem);
			document.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}





	public void converteVariasImagemEmPDF(List<String> fotos, double aumentar, double diminuir,
			boolean umaPaginaPorFoto, String caminhoArquivoDestinoComNomeEExtensao) {

		try {
			Document document = new Document();
			FileOutputStream fos = new FileOutputStream(caminhoArquivoDestinoComNomeEExtensao);
			PdfWriter writer = PdfWriter.getInstance(document, fos);
			writer.open();
			document.open();
			int pagina = 0;

			for (String foto : fotos) {
				pagina++;
				Image imagem = Image.getInstance(foto);
				float x = imagem.getScaledHeight();
				float y = imagem.getScaledWidth();

				if (aumentar > 0) {
					imagem.scaleAbsoluteHeight((float) (x * aumentar));
					imagem.scaleAbsoluteWidth((float) (y * aumentar));
				} else if (diminuir > 0) {
					imagem.scaleAbsoluteHeight((float) (x / diminuir));
					imagem.scaleAbsoluteWidth((float) (y / diminuir));
				}

				if (pagina > 1) {
					if(umaPaginaPorFoto) {
						document.newPage();
					}
				}

				document.add(imagem);
			}

			document.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}




	public static void convPDFparaHTML(String caminhoArquivoPDF) {

		try {
			PDDocument pdf = PDDocument.load(new File(caminhoArquivoPDF));
			Writer output = new PrintWriter(caminhoArquivoPDF + ".html", "utf-8");
			new PDFDomTree().writeText(pdf, output);
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}





	public void juntarPDFs(List<String> caminhosPDFs) {

		try {
			MemoryUsageSetting memoria = MemoryUsageSetting.setupTempFileOnly(50000000);
			String input = "/home/tudodev/enviadosUsuarios/contrato.pdf";
			String input2 = "/home/tudodev/enviadosUsuarios/scania.pdf";
			String output = "/home/tudodev/enviadosUsuarios/contratoScaniaSaida.pdf";
			File arquivosMemoria = new File("/home/tudodev/enviadosUsuarios/temp");
			arquivosMemoria.mkdirs();
			memoria.setTempDir(arquivosMemoria);
			PDFMergerUtility juntos = new PDFMergerUtility();
			juntos.addSource(input);
			juntos.addSource(input2);
			juntos.setDestinationFileName(output);
			juntos.mergeDocuments(memoria);
			log.info("ARQUIVOS MESCLADOS!");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
/*
 * SOBRE O MÉTODO 'converteTextoEmPDF' Renomeei um arquivo PDF, trocando sua
 * extensão para 'txt'. Ao tentar converter esse arquivo, o método
 * 'converteTextoEmPDF' ficava travado/parado em uma linha específica. Linha essa que
 * possuía MUITOS caracteres, sem NENHUM espaço entre eles. DETALHE: o método
 * ficava parado nessa linha e NÃO APRESENTAVA NENHUMA EXCEÇÃO (para que eu
 * pudesse tratá-la). A única solução viável foi testar o tamanho da string
 * gerada em cada linha e verificar se existe nela, um mínimo de caracteres
 * 'espaço'. Ou seja, se existir uma linha com mais de 200 caracteres e não
 * existir pouquíssimos ou nenhum espaço nela, o loop deve pular essa linha,
 * pois ela vai causar problemas.
 */
