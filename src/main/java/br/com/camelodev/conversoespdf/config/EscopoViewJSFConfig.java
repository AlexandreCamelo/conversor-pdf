package br.com.camelodev.conversoespdf.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class EscopoViewJSFConfig
{

	@Bean
	public static CustomScopeConfigurer ConfiguradorCustomizadoViewJSF()
	{
		Map<String, Object> escopos = new HashMap<>();
		escopos.put("viewJSF", new EscopoViewJSF());

		CustomScopeConfigurer configurador = new CustomScopeConfigurer();
		configurador.setScopes(escopos);

		return configurador;
	}







}
