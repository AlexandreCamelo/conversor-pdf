package br.com.camelodev.conversoespdf.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@EnableWebMvc
@ComponentScan
public class ConfigTomCatImagens implements WebMvcConfigurer
{

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry)
	{
		registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");		
		registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");		
		registry.addResourceHandler("/imagens/**").addResourceLocations("classpath:/static/imagens/");	
		registry.addResourceHandler("/fontes/**").addResourceLocations("classpath:/static/fontes/");	
		registry.addResourceHandler("/enviados/**").addResourceLocations("file:/home/tudodev/enviadosUsuarios/");	
		registry.addResourceHandler("/home/**").addResourceLocations("file:/home/");	
	}






}
