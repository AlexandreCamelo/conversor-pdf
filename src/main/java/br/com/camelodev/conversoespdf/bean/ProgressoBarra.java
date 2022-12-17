package br.com.camelodev.conversoespdf.bean;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Scope("viewJSF")
@Getter
@Setter
public class ProgressoBarra implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer progBarra;
}
