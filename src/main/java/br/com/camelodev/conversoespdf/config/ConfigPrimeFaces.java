package br.com.camelodev.conversoespdf.config;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sun.faces.config.ConfigureListener;


@Configuration
public class ConfigPrimeFaces
{
	@Bean
	public ServletContextInitializer servletContextInitializer()
	{
		return servletContext ->
		{
			servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration", Boolean.TRUE.toString());
			servletContext.setInitParameter("primefaces.THEME", "#{configPrimefacesTemas.tema}"); //"start"   
			servletContext.setInitParameter("primefaces.FONT_AWESOME", "true"); //Ícones   
		
		};
	}

	@Bean
	public ServletListenerRegistrationBean<ConfigureListener> jsfConfigureListener()
	{
		return new ServletListenerRegistrationBean<>(new ConfigureListener());
	}
	
	
	

	
	
}
