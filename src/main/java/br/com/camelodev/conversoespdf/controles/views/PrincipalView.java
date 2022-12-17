package br.com.camelodev.conversoespdf.controles.views;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;




@Controller
@RequestMapping("/")
public class PrincipalView
{



	@GetMapping()
	public ModelAndView Listar()
	{
		ModelAndView modelAndView = new ModelAndView("th_principal");
		return modelAndView;
	}

}
