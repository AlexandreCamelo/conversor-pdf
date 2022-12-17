package br.com.camelodev.conversoespdf.bean;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.camelodev.conversoespdf.entidades.Fotos;
import lombok.Data;


@Component
@Scope("viewJSF")
@Data
public class SepararPDFBeanDTO {
	public List<Fotos> fotos = new ArrayList<>();
	public List<Fotos> fotosSelecionadas = new ArrayList<>();
	public Fotos fotoSelecionada;
	
	
}
