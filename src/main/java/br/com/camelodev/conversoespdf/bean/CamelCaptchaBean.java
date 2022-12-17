package br.com.camelodev.conversoespdf.bean;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.shaded.commons.io.output.ByteArrayOutputStream;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.Data;

@Scope("viewJSF")
@Component
@Data
public class CamelCaptchaBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private final int QTDE_CARACTERES_CAPTCHA = 6;
	private final int LARGURA_CAPTCHA = 250;
	private final int ALTURA_CAPTCHA = 100;
	private final int TAMANHO_MIN_FONTE = 20;
	private final int TAMANHO_MAX_FONTE = 40;
	private final int QTDE_CIRCULOS = 6;
	private final int QTDE_LINHAS = 12;
	private final String CARACTERES_POSSIVEIS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%&+*?";
	private final String CARACTERES_POSSIVEIS_FACEIS = "abdefghjmnqurtyABDEFGHLMNQRTY23456789!@#$%&?";
	private String strQuebraCabecas = "";
	private String valorCaptchaUsuario = "";
	private StreamedContent imagemQuebraCabecas;
	

	
	public CamelCaptchaBean() {
		imagemQuebraCabecas = gerarQuebraCabecas(CARACTERES_POSSIVEIS);
	}
	

	public void atualizarCaptcha() {
		imagemQuebraCabecas = gerarQuebraCabecas(CARACTERES_POSSIVEIS_FACEIS);
		PrimeFaces.current().ajax().update("quebra-cabecas");
	}


	public StreamedContent gerarQuebraCabecas(String caracteresPossiveis) {
		try {
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> {

				try {
					strQuebraCabecas = "";
					List<Graphics2D> listaCaracteres = new ArrayList<>();
					BufferedImage bufferedImg = new BufferedImage(LARGURA_CAPTCHA, ALTURA_CAPTCHA,
							BufferedImage.TYPE_INT_RGB);

					for (int i = 0; i < QTDE_CARACTERES_CAPTCHA; i++) {
						Graphics2D imgCaractere = bufferedImg.createGraphics();
						Font fonte = new Font(Font.SANS_SERIF, Font.BOLD, escolheTamanhoFonte());
						imgCaractere.setFont(fonte);
						imgCaractere.setColor(Color.green);
						String caractereEscolhido = escolherCaractere(caracteresPossiveis);
						imgCaractere.drawString(caractereEscolhido, posicionarX(i), posicionarY());
						listaCaracteres.add(imgCaractere);
						strQuebraCabecas += caractereEscolhido;
					}

					
					for (int i = 0; i < QTDE_CIRCULOS; i++) {
						Graphics2D circulo = bufferedImg.createGraphics();
						desenhaCirculoAleatorio(circulo);
					}
					
					for (int i = 0; i < QTDE_LINHAS; i++) {
						Graphics2D linha = bufferedImg.createGraphics();
						desenhaLinhaHorizontal(linha, (i * 10));
					}
					
					for (int i = 0; i < QTDE_LINHAS; i++) {
						Graphics2D linha = bufferedImg.createGraphics();
						desenhaLinhaAleatoria(linha);
					}
					
					
					RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					ImageIO.write(bufferedImg, "png", os);
					return new ByteArrayInputStream(os.toByteArray());
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}

			}).build();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}


	private String escolherCaractere(String caracteresPossiveis) {
		String carac = caracteresPossiveis;
		List<String> listaCaracteres = new ArrayList<>();

		for (int i = 0; i < carac.length(); i++) {
			listaCaracteres.add(carac.substring(i, i + 1));
		}

		return sortearCaractere(listaCaracteres);
	}


	private String sortearCaractere(List<String> lista) {
		Random random = new Random();
		int numAleatorio = random.nextInt(lista.size());
		return lista.get(numAleatorio);
	}
	
	private int posicionarX(int multiplicador) {
		return 20 + (30 * multiplicador);
	}

	private int posicionarY() {
		Random r = new Random();
		int altoOuBaixo = r.nextInt(2);
		if (altoOuBaixo == 0) {
			return (ALTURA_CAPTCHA / 2) - 10;
		} else {
			return (ALTURA_CAPTCHA / 2) + 10;
		}
	}

	private int escolheTamanhoFonte() {
		Random r = new Random();
		int tamanho = r.nextInt(TAMANHO_MAX_FONTE);
		
		if(tamanho < TAMANHO_MIN_FONTE) {
			tamanho = TAMANHO_MIN_FONTE;
		}
		
		return tamanho;
	}
	
	
	private void desenhaCirculoAleatorio(Graphics2D circulo) {
		Random r = new Random();
		
		int x = r.nextInt(LARGURA_CAPTCHA - 20);
		int y = r.nextInt(ALTURA_CAPTCHA - 20);
		int larg = r.nextInt(LARGURA_CAPTCHA - 20);
		int alt = r.nextInt(ALTURA_CAPTCHA - 20);
		int corR = r.nextInt(255);
		int corG = r.nextInt(255);
		int corB = r.nextInt(255);
		circulo.setColor(new Color(corR, corG, corB));
		circulo.drawOval(x, y, larg, alt);
	}

	
	
	private void desenhaLinhaAleatoria(Graphics2D linha) {
		Random r = new Random();
		int corR = r.nextInt(255);
		int corG = r.nextInt(255);
		int corB = r.nextInt(255);
		linha.setColor(new Color(corR, corG, corB));
		linha.drawLine(r.nextInt(LARGURA_CAPTCHA),r.nextInt(ALTURA_CAPTCHA), r.nextInt(LARGURA_CAPTCHA),r.nextInt(ALTURA_CAPTCHA));
	}
	
	private void desenhaLinhaHorizontal(Graphics2D linha, int y) {
		Random r = new Random();
		int corR = r.nextInt(255);
		int corG = r.nextInt(255);
		int corB = r.nextInt(255);
		linha.setColor(new Color(corR, corG, corB));
		linha.drawLine(0, y, LARGURA_CAPTCHA, y);
	}
	
	
	public boolean quebraCabecasResolvido() {
		if(valorCaptchaUsuario == null || valorCaptchaUsuario.isBlank()) {
			atualizarCaptcha();
			return false;
		}
		
		if(strQuebraCabecas.equals(valorCaptchaUsuario)) {
			PrimeFaces.current().ajax().update("quebra-cabecas");
			return true;
		}else {
			PrimeFaces.current().ajax().update("quebra-cabecas");
			return false;
		}
	}
	
}
