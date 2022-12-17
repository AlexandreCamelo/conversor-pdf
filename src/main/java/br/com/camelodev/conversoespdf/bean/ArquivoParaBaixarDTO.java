package br.com.camelodev.conversoespdf.bean;

import java.io.Serializable;

import org.primefaces.model.StreamedContent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Scope("viewJSF")
@Data
public class ArquivoParaBaixarDTO implements Serializable{
	private static final long serialVersionUID = 1L;
	private StreamedContent arquivoParaBaixar;
}
