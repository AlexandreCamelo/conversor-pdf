package br.com.camelodev.conversoespdf.entidades;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Fotos implements Comparable<Fotos> {
	private int pagina;
	private String foto;
	private Boolean adicionar;

	@Override
	public int compareTo(Fotos outraFoto) {

		if (this.pagina < outraFoto.getPagina()) {
			return -1;
		}

		if (this.pagina > outraFoto.getPagina()) {
			return 1;
		}

		return 0;
	}
}
