package br.com.camelodev.conversoespdf.controles.views;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/")
public class ViewsGerais {
	
	
	@GetMapping("/erro403")
	public ModelAndView Listar()
	{
		ModelAndView modelAndView = new ModelAndView("th_erro403");
		return modelAndView;
	}
	
	
	@GetMapping("/blank")
	public ModelAndView emBranco()
	{
		ModelAndView modelAndView = new ModelAndView("blank");
		return modelAndView;
	}
	
	
	

}
