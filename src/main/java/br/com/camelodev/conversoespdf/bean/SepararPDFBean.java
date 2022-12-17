package br.com.camelodev.conversoespdf.bean;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipOutputStream;

import javax.faces.application.FacesMessage;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.omnifaces.util.Faces;
import org.primefaces.PrimeFaces;
import org.primefaces.event.DragDropEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.itextpdf.text.exceptions.InvalidPdfException;

import br.com.camelodev.conversoespdf.controles.views.ConversoesGerais;
import br.com.camelodev.conversoespdf.entidades.Fotos;
import br.com.camelodev.conversoespdf.entidades.Intervalo;
import br.com.camelodev.conversoespdf.uteis.IdUnicoApp;
import br.com.camelodev.conversoespdf.uteis.ZiparArquivos;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope("viewJSF")
@Named()
@Data
@Slf4j
public class SepararPDFBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private UploadedFile arquivoEnviado;
	private String diretorioEnviados;
	private String estiloLinkDownload;
	private String pastaArquivos;
	private File filePastaArquivos;
	private String idCliente;
	private File caminhoCompletoArquivoParaBaixar;
	private Long tamanhoMaxArquivo = 0L;
	private boolean pararProcesso = false;
	private String etiquetaArquivoABaixar = "";
	private List<Fotos> fotos = new ArrayList<>();
	private List<Fotos> fotosSelecionadas = new ArrayList<>();
	private Fotos fotoSelecionada;
	private int intervInicio = 0;
	private int intervFim = 0;
	private List<Intervalo> intervalosSelecionados = new ArrayList<>();
	private String strIntervalosSelecionados = "";
	private int totalPaginasPDF = 0;
	private boolean existeFotoMarcada;
	private String nomeArqEnviado;
	private String caminhoFotoAltaResolucaoDialog = "";
	private boolean umArquivoPorFoto;
	@Autowired
	ConversoesGerais conversao;
	@Autowired
	Ocr reconhecimento;
	@Autowired
	ProgressoBarra progresso;
	@Autowired
	ArquivoParaBaixarDTO paraBaixar;
	@Autowired
	SepararPDFBeanDTO fotosDTO;
	@Autowired
	CamelCaptchaBean captcha;


	public SepararPDFBean(@Value("${conversoes.diretorio.arquivos.enviados}") String caminhoEnviados,
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
			tamanhoMaxArquivo = Long.valueOf(tamanhoMaximoArquivo);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}




	public void converterPDFParaImagem() {
		if (!captcha.quebraCabecasResolvido()) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRADO", "Favor resolver o quebra cabeças."));
			PrimeFaces.current().executeScript("rejeicaoCaptcha();");
			return;
		}
		
		try {
			String nomeArquivo = arquivoEnviado.getFileName();
			ArquivoEnviadoDTO.uplArquivoEnviado = arquivoEnviado;
			nomeArqEnviado = nomeArquivo;
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
				ArquivoEnviadoDTO.nomeArquivoEnviado = nomeArquivo;
				PDFEmImagem(caminhoCompletoPDF, arquivoEnviado.getFileName(), "jpeg", strPastaParaImagens);
				PrimeFaces.current().executeScript("escondeDivEscolha();");
				PrimeFaces.current()
						.executeScript("window.document.getElementById('divEscolhaModo').style.display = 'flex';");
				PrimeFaces.current()
						.executeScript("window.document.getElementById('divReProcModo').style.display = 'flex';");
				PrimeFaces.current().ajax().update("formPrincipal:nomeArquivoEnviado");
				PrimeFaces.current().ajax().update("nomeArquivoEnviado");
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
		@SuppressWarnings("unused")
		String listaFotos = "[";
		ArquivoEnviadoDTO.pararProcesso = false; // não tirar daqui

		try {
			File pastaImagens = new File(pastaOndeColocarImagens);
			pastaImagens.mkdirs();
			pastaImagens.deleteOnExit();
			ArquivoEnviadoDTO.pastaOndeColocarImagens = pastaOndeColocarImagens;
			PDDocument document = PDDocument.load(new File(caminhoCompletoarquivoPDF));
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			int totalDePaginas = document.getNumberOfPages();
			totalPaginasPDF = totalDePaginas;

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
				BufferedImage bim = pdfRenderer.renderImageWithDPI(pagina, 30, ImageType.RGB);
				// É importante colocar essa parte da string: (page<10?"0":""), pois o
				// Arrays.sort Só conseguirá ordenar os nomes dos arquivos, corretamente, com os
				// ZEROS na
				// frente dos números menores que 10.
				// Se ele não ordenar corretamente, o sistema gerará um arquivo texto sem uma
				// ordem compreensível
				String nomeArquivo = pastaOndeColocarImagens + "/" + nomeEExtensaoArquivoPDF + (pagina < 10 ? "0" : "")
						+ pagina + "." + extensaoDaImagem;
				ImageIOUtil.writeImage(bim, nomeArquivo, 300);

				if (pagina == (totalDePaginas - 1)) {
					listaFotos += "\"/Conversoes/" + nomeArquivo + "\"";
				} else {
					listaFotos += "\"/Conversoes/" + nomeArquivo + "\", ";
				}

				fotos.add(new Fotos(pagina + 1, nomeArquivo, false));
				fotosDTO.fotos = fotos;
				Double dblPorcent = Double.valueOf(pagina) / Double.valueOf(totalDePaginas) * 100;
				Integer porcentagem = (int) Math.round(dblPorcent);

				if (porcentagem <= 2) {
					progresso.setProgBarra(2);
				} else {
					progresso.setProgBarra(porcentagem);
				}

			}

			document.close();
			listaFotos += "]";
			Collections.sort(fotosDTO.fotos);
			return pastaArquivos + "/imagens/" + nomeEExtensaoArquivoPDF + ".zip";
		} catch (InvalidPdfException ePdf) {
			return "invalidPdfException";
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return "ioexception";
		} catch (Exception e) {
			e.printStackTrace();
			return "Não conseguimos identificar texto na página: " + paginaAtual;
		}

	}





	public void separarPDFporIntervalos() {

		if (intervalosSelecionados.size() == 0) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"NENHUM INTERVALO.", "Selecione ao menos 1 intervalo."));
			return;
		}

		try {
			progresso.setProgBarra(0);
			File pdf = new File(ArquivoEnviadoDTO.caminhoCompletoArquivoEnviado);
			PDDocument document = PDDocument.load(pdf);
			totalPaginasPDF = document.getNumberOfPages();
			Splitter splitter = new Splitter();
			PDFMergerUtility PDFmerger = new PDFMergerUtility();
			List<String> arquivosParaJuntar = new ArrayList<>();
			String pdfMergeABaixar = ArquivoEnviadoDTO.pastaOndeColocarImagens + "/"
					+ ArquivoEnviadoDTO.nomeArquivoEnviado + "_div.pdf";
			PDFmerger.setDestinationFileName(pdfMergeABaixar);
			int i = 0;
			int numIntervalo = 0;
			String caminhoArquivoDividido = "";
			List<String> arquivosParaZipar = new ArrayList<>();

			for (Intervalo interv : intervalosSelecionados) {
				numIntervalo++;

				if (interv.getFim() <= 0 || interv.getInicio() <= 0) {
					log.info("Intervalo " + numIntervalo + " incorreto.");
					continue;
				} else if (interv.getInicio() > interv.getFim()) {
					log.info("Intervalo inicial MAIOR que intervalo final.");
					continue;
				}

				Integer qtdePaginasSelecionadas = interv.getFim() - interv.getInicio() + 1;

				if (!ArquivoEnviadoDTO.umArquivoPorFoto) {
					splitter.setSplitAtPage(qtdePaginasSelecionadas);
				}

				splitter.setStartPage(interv.getInicio());
				splitter.setEndPage(interv.getFim());
				List<PDDocument> paginasSeparadas = splitter.split(document);

				for (PDDocument folha : paginasSeparadas) {
					i++;
					caminhoArquivoDividido = ArquivoEnviadoDTO.pastaOndeColocarImagens + "/"
							+ ArquivoEnviadoDTO.nomeArquivoEnviado + "_div" + ".pdf" + i + ".pdf";
					folha.save(new File(caminhoArquivoDividido));

					if (paginasSeparadas.size() > 1) {

						if (ArquivoEnviadoDTO.umArquivoPorFoto) {
							arquivosParaZipar.add(caminhoArquivoDividido);
						} else {
							arquivosParaJuntar.add(caminhoArquivoDividido);
						}

					} else {
						arquivosParaJuntar.add(caminhoArquivoDividido);
					}

				}

			}

			pdfOuZip(arquivosParaZipar, arquivosParaJuntar, PDFmerger, pdfMergeABaixar);
			document.close();
			ArquivoEnviadoDTO.umArquivoPorFoto = false;
			PrimeFaces.current().executeScript("PF('dlgBarraEterna').hide();");
			telaFinalizada();
		} catch (FileNotFoundException fe) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"NENHUM PDF", "Escolha um arquivo PDF para separar."));
		} catch (NullPointerException ne) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"NENHUM PDF", "Escolha um arquivo PDF para separar."));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}





	public void separarPDFPorDragDrop() {

		if (fotosSelecionadas.size() == 0) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, "NENHUMA PÁGINA.", "Selecione ao menos 1 página."));
			return;
		}

		try {
			File pdf = new File(ArquivoEnviadoDTO.caminhoCompletoArquivoEnviado);
			PDDocument document = PDDocument.load(pdf);
			totalPaginasPDF = document.getNumberOfPages();
			Splitter splitter = new Splitter();
			PDFMergerUtility PDFmerger = new PDFMergerUtility();
			List<String> arquivosParaJuntar = new ArrayList<>();
			String pdfMergeABaixar = ArquivoEnviadoDTO.pastaOndeColocarImagens + "/"
					+ ArquivoEnviadoDTO.nomeArquivoEnviado + "_div.pdf";
			PDFmerger.setDestinationFileName(pdfMergeABaixar);
			etiquetaArquivoABaixar = ArquivoEnviadoDTO.nomeArquivoEnviado + "_div" + ".pdf";
			int i = 0;
			@SuppressWarnings("unused")
			int numIntervalo = 0;
			String caminhoArquivoDividido = "";
			List<String> arquivosParaZipar = new ArrayList<>();

			for (Fotos foto : fotosSelecionadas) {
				numIntervalo++;
				i++;
				Integer qtdePaginasSelecionadas = fotosSelecionadas.size() + 1;

				if (!ArquivoEnviadoDTO.umArquivoPorFoto) {
					splitter.setSplitAtPage(qtdePaginasSelecionadas);
				}

				splitter.setStartPage(foto.getPagina());
				splitter.setEndPage(foto.getPagina());
				List<PDDocument> paginasSeparadas = splitter.split(document);

				for (PDDocument folha : paginasSeparadas) {
					caminhoArquivoDividido = ArquivoEnviadoDTO.pastaOndeColocarImagens + "/pdfDividido" + i + ".pdf";
					folha.save(new File(caminhoArquivoDividido));

					if (fotosSelecionadas.size() > 1) {

						if (ArquivoEnviadoDTO.umArquivoPorFoto) {
							arquivosParaZipar.add(caminhoArquivoDividido);
						} else {
							arquivosParaJuntar.add(caminhoArquivoDividido);
						}

					} else {
						arquivosParaJuntar.add(caminhoArquivoDividido);
					}

				}

			}

			pdfOuZip(arquivosParaZipar, arquivosParaJuntar, PDFmerger, pdfMergeABaixar);
			document.close();
			ArquivoEnviadoDTO.umArquivoPorFoto = false;
			ArquivoEnviadoDTO.caminhoCompletoArquivoEnviado = "";
			telaFinalizada();
			PrimeFaces.current().executeScript("PF('dlgBarraEterna').hide();");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}





	public void separarTodasAsPaginas() {

		try {
			File pdf = new File(ArquivoEnviadoDTO.caminhoCompletoArquivoEnviado);
			PDDocument document = PDDocument.load(pdf);
			Splitter splitter = new Splitter();
			int i = 0;
			@SuppressWarnings("unused")
			int numIntervalo = 0;
			String caminhoArquivoDividido = "";
			List<String> arquivosParaZipar = new ArrayList<>();
			splitter.setStartPage(1);
			splitter.setEndPage(document.getNumberOfPages());
			List<PDDocument> paginasSeparadas = splitter.split(document);

			for (PDDocument folha : paginasSeparadas) {
				i++;
				caminhoArquivoDividido = ArquivoEnviadoDTO.pastaOndeColocarImagens + "/pdfDividido" + i + ".pdf";
				folha.save(new File(caminhoArquivoDividido));
				arquivosParaZipar.add(caminhoArquivoDividido);
			}

			pdfOuZipSepararTodasPaginas(arquivosParaZipar);
			document.close();
			ArquivoEnviadoDTO.caminhoCompletoArquivoEnviado = "";
			telaFinalizada();
			PrimeFaces.current().executeScript("PF('dlgBarraEterna').hide();");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}





	public void aoReceberFoto(DragDropEvent<Fotos> eventoDd) {

		try {
			Fotos foto = eventoDd.getData();
			fotosSelecionadas.add(foto);
			fotosDTO.fotosSelecionadas = fotosSelecionadas;
			fotosDTO.fotos = fotos;
			intervInicio = 0;
			intervFim = 0;
			mostraOuEscondeBotoesSeparacao();
		} catch (IllegalArgumentException f) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"PÁGINA EXPIRADA", "Já se passou muito tempo, desde a sua última ação. Favor refazer a operação."));
		}

	}


	public void addExcluirFotosMarcadas(Fotos foto) {

		if (foto.getAdicionar()) {
			adicionarFotosMarcadas(foto);
		} else {
			tirarPaginaDaLista(foto);
		}

	}


	public void adicionarFotosMarcadas(Fotos foto) {

		try {
			fotosSelecionadas.add(foto);
			fotosDTO.fotosSelecionadas = fotosSelecionadas;
			fotosDTO.fotos = fotos;
			intervInicio = 0;
			intervFim = 0;
			existeFotoMarcada = true;
			mostraOuEscondeBotoesSeparacao();
		} catch (IllegalArgumentException f) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"PÁGINA EXPIRADA", "Já se passou muito tempo, desde a sua última ação. Favor refazer a operação."));
		}

	}


	public void tirarPaginaDaLista(Fotos foto) {
		fotosDTO.fotosSelecionadas.remove(foto);
		PrimeFaces.current().executeScript("legendaTabelaVazia();");

		if (fotosDTO.fotosSelecionadas.size() == 0) {
			existeFotoMarcada = false;
			PrimeFaces.current()
					.executeScript("document.getElementById('divBotoesPagEscolhidas').style.display = 'none'");
		}

		fotos.get(fotos.indexOf(foto)).setAdicionar(false);
		mostraEscondeBotaoSepararMob();
	}





	public void mostraEscondeBotaoSepararMob() {

		if (existeFotoMarcada) {
			PrimeFaces.current()
					.executeScript("window.document.getElementById('divBotaoSepararDragMob').style.display = 'flex';");
		} else {
			PrimeFaces.current()
					.executeScript("window.document.getElementById('divBotaoSepararDragMob').style.display = 'none';");
		}

	}





	public void PDFEmImagemAltaResolucao(int paginaAConverter) {
		int pagina = 0;

		if (paginaAConverter > 0) {
			pagina = paginaAConverter - 1;
		} else {
			pagina = paginaAConverter;
		}

		String nomeEExtensaoArquivoPDF = ArquivoEnviadoDTO.nomeArquivoEnviado;
		String extensaoDaImagem = "jpeg";
		String pastaOndeColocarImagens = ArquivoEnviadoDTO.pastaOndeColocarImagens;

		try {
			PDDocument document = PDDocument.load(new File(ArquivoEnviadoDTO.caminhoCompletoArquivoEnviado));
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			BufferedImage bim = pdfRenderer.renderImageWithDPI(pagina, 300, ImageType.RGB);
			String caminhoFoto = pastaOndeColocarImagens + "/" + nomeEExtensaoArquivoPDF + (pagina < 10 ? "0" : "")
					+ pagina + "ALTA." + extensaoDaImagem;
			ImageIOUtil.writeImage(bim, caminhoFoto, 300);
			caminhoFotoAltaResolucaoDialog = caminhoFoto;
			document.close();
			PrimeFaces.current().executeScript("mostraDialogFotoAltaResolucao();");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}





	private void pdfOuZip(List<String> arquivosParaZipar, List<String> arquivosParaJuntar, PDFMergerUtility PDFmerger,
			String pdfMergeABaixar) {
		String caminhoArquivoZIP = "";
		List<File> arquivosAApagar = new ArrayList<>();

		try {

			if (arquivosParaZipar.size() > 0) {
				caminhoArquivoZIP = ziparArquivosPDF(arquivosParaZipar);

				for (String arquivo : arquivosParaJuntar) {
					File arq = new File(arquivo);
					arquivosAApagar.add(arq);
				}

				caminhoCompletoArquivoParaBaixar = new File(caminhoArquivoZIP);
				etiquetaArquivoABaixar = ArquivoEnviadoDTO.nomeArquivoEnviado + ".zip";
			} else {

				for (String arquivo : arquivosParaJuntar) {
					File arq = new File(arquivo);
					PDFmerger.addSource(arq);
					arquivosAApagar.add(arq);
				}

				PDFmerger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly(tamanhoMaxArquivo));
				caminhoCompletoArquivoParaBaixar = new File(pdfMergeABaixar);
				etiquetaArquivoABaixar = ArquivoEnviadoDTO.nomeArquivoEnviado + "_div" + ".pdf";
			}

			for (File arquivo : arquivosAApagar) {
				arquivo.delete();
			}

			caminhoCompletoArquivoParaBaixar.deleteOnExit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}





	private void pdfOuZipSepararTodasPaginas(List<String> arquivosParaZipar) {
		String caminhoArquivoZIP = "";
		List<File> arquivosAApagar = new ArrayList<>();

		try {

			if (arquivosParaZipar.size() > 1) {
				caminhoArquivoZIP = ziparArquivosPDF(arquivosParaZipar);

				for (String arquivo : arquivosParaZipar) {
					File arq = new File(arquivo);
					arquivosAApagar.add(arq);
				}

				caminhoCompletoArquivoParaBaixar = new File(caminhoArquivoZIP);
				etiquetaArquivoABaixar = ArquivoEnviadoDTO.nomeArquivoEnviado + ".zip";
			} else {

				for (String arquivo : arquivosParaZipar) {
					File arq = new File(arquivo);
					arquivosAApagar.add(arq);
					caminhoCompletoArquivoParaBaixar = new File(arquivo);
					etiquetaArquivoABaixar = ArquivoEnviadoDTO.nomeArquivoEnviado + "_div" + ".pdf";
				}

			}

			for (File arquivo : arquivosAApagar) {
				arquivo.delete();
			}

			caminhoCompletoArquivoParaBaixar.deleteOnExit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	private void telaFinalizada() {
		PrimeFaces.current().executeScript("window.document.getElementById('divIntervalos').style.display = 'none';");
		PrimeFaces.current().executeScript("renderizarDragDropPeloTamanhoDaTela(false)");
		PrimeFaces.current().executeScript("mostrarPainelBaixar();");
		// Se tirar esse update daqui, a eiqueta do nome do arquivo não aparece na
		// página
		PrimeFaces.current().ajax().update("formPrincipal:lblNomeArquivo");
		PrimeFaces.current().executeScript("window.document.getElementById('divReProcModo').style.display = 'none';");
		PrimeFaces.current().executeScript("window.document.getElementById('divEscolhaModo').style.display = 'none';");
	}





	private String ziparArquivosPDF(List<String> arquivosAZipar) {
		ArquivoEnviadoDTO.pararProcesso = false; // não tirar daqui
		String nomeEExtensaoArquivoPDF = ArquivoEnviadoDTO.nomeArquivoEnviado;
		String pastaOndeColocarImagens = ArquivoEnviadoDTO.pastaOndeColocarImagens;
		String caminhoArquivoZip = pastaOndeColocarImagens + "/" + nomeEExtensaoArquivoPDF + ".zip";
		int pagina = 0;

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(caminhoArquivoZip);
			ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

			for (String arquivoPDF : arquivosAZipar) {
				pagina++;
				File flArquivoPDF = new File(arquivoPDF);

				if (pagina == (arquivosAZipar.size())) {
					ZiparArquivos.ziparArquivoUmAUm(flArquivoPDF, true, fileOutputStream, zipOutputStream);
				} else {
					ZiparArquivos.ziparArquivoUmAUm(flArquivoPDF, false, fileOutputStream, zipOutputStream);
				}

			}

			zipOutputStream.close();
			fileOutputStream.close();
			return caminhoArquivoZip;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

	}





	private void mostraOuEscondeBotoesSeparacao() {

		if (fotosDTO.fotosSelecionadas.size() > 0) {
			strIntervalosSelecionados = "";
			intervalosSelecionados.clear();
			PrimeFaces.current()
					.executeScript("window.document.getElementById('divIntervalos').style.display = 'none';");
			PrimeFaces.current()
					.executeScript("document.getElementById('divBotoesPagEscolhidas').style.display = 'flex'");
			return;
		}

		if (intervalosSelecionados.size() <= 0) {
			PrimeFaces.current()
					.executeScript("document.getElementById('formPrincipal:btnSepararInterv').style.display = 'none'");
		} else {
			PrimeFaces.current()
					.executeScript("document.getElementById('formPrincipal:btnSepararInterv').style.display = 'flex'");
			PrimeFaces.current()
					.executeScript("document.getElementById('divBotoesPagEscolhidas').style.display = 'none'");
		}

	}





	public void adicionarIntervalo() {

		try {

			if (intervFim <= 0 || intervInicio <= 0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"INTERVALO ERRADO", "Os intervalos inicial e final devem serm maiores que zero."));
				return;
			} else if (intervFim < intervInicio) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"INTERVALO ERRADO", "O intervalo final deve ser maior que o intervalo inicial."));
				return;
			}

			intervalosSelecionados.add(new Intervalo(intervInicio, intervFim));

			if (intervalosSelecionados.size() <= 1) {
				strIntervalosSelecionados += "Páginas: | " + intervInicio + " a " + intervFim;
			} else {
				strIntervalosSelecionados += " | " + intervInicio + " a " + intervFim;
			}

			mostraOuEscondeBotoesSeparacao();
		} catch (ViewExpiredException ve) {
			PrimeFaces.current()
					.executeScript("document.getElementById('formPrincipal:btnSepararInterv').style.display = 'none'");
			strIntervalosSelecionados = "";
			ve.printStackTrace();
		} catch (Exception e) {
			PrimeFaces.current()
					.executeScript("document.getElementById('formPrincipal:btnSepararInterv').style.display = 'none'");
			strIntervalosSelecionados = "";
			e.printStackTrace();
		}

	}





	public void excluirIntervalo() {

		try {
			strIntervalosSelecionados = "";
			intervalosSelecionados.remove(intervalosSelecionados.size() - 1);
			int contador = 0;

			for (Intervalo intervalo : intervalosSelecionados) {
				contador++;

				if (contador == 1) {
					strIntervalosSelecionados += "Páginas: | " + intervalo.getInicio() + " a " + intervalo.getFim();
				} else {
					strIntervalosSelecionados += " | " + intervalo.getInicio() + " a " + intervalo.getFim();
				}

			}

			mostraOuEscondeBotoesSeparacao();
		} catch (ViewExpiredException ve) {
			PrimeFaces.current()
					.executeScript("document.getElementById('formPrincipal:btnSepararInterv').style.display = 'none'");
			strIntervalosSelecionados = "";
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"PÁGINA EXPIRADA", "Já passou muito tempo, desde a última alteração. Favor refazer o processo."));
		} catch (Exception e) {
			PrimeFaces.current()
					.executeScript("document.getElementById('formPrincipal:btnSepararInterv').style.display = 'none'");
			strIntervalosSelecionados = "";
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





	public void setaUmArquivoPorFoto() {
		ArquivoEnviadoDTO.umArquivoPorFoto = umArquivoPorFoto;
	}
}
