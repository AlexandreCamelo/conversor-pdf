package br.com.camelodev.conversoespdf.config;

import java.io.Serializable;

import javax.faces.view.ViewScoped;

import org.springframework.stereotype.Component;

import lombok.Data;



//#############################
//TEMAS PARA ESCOLHER:
//afterdark (tema escuro - MUITO BOM)
//afternoon (cinza claro, com realces em azul claro)
//aristo
//black-tie (tema preto e branco)
//blitzer (branco com detalhes em vermelho e amarelo claro)
//bluesky (azul claro)
//bootstrap
//casablanca (cinza-amarelado, com detalhes/realces em amarelo claro)
//cruze (tema escuro - MUITO BOM)
//cupertino (cinza com azul claro. Realces em amarelo e azul claro - BEM LEGAL) 
//dark-hive (escuro, preto, com detalhes em azul citilante - BEM INTERESSANTE)
//delta (branco com detalhes em azul royal. Alguns detalhes bem interessantes, tais como o botão de "fechar" dos dialogs ser REDONDO)
//dot-luv (escuro - PRETO, com detalhes em azul escuro - as seleções em azul, são quadriculadas - BEM INTERESSANTE)
//eggplant (cinza escuro com algum degradê ao fundo - detalhes em branco - BEM LEGAL)
//excite-bike (cinza claro, com muitos detalhes em azul - os azuis são enfeitados com listras diagonais - DIFERENTE)
//flick (tema claro, com detalhes em azul e rosa escuro - BONITO E DIFERENTE)
//glass-x (cinza claro em degradê, imitando um vidro transparente)
//home (cinza com detalhes e realces em azul petróleo - MUITO BOM)
//hot-sneaks (fundo branco, com headers em azul-petróleo-quadriculado e seleções em amarelo escuro  -MUITO LEGAL
//humanity (amarelo claro - BOM)
//le-frog (verde escuro, com detalhes em verde claro, branco e amarelo) - MUITO, MUITO LEGAL)
//luna-amber
//luna-blue
//luna-green
//luna-pink
//midnight (escuro com fundo PRETO e realces em azul claro)
//mint-choc (escuro, quase preto, com detalhes em verde claro e realces em marrom - MUITO LEGAL)
//overcast (tema cinza, parecido com o ARISTO, só que mais bonito)
//pepper-grinder (beje, com detalhes em marrom e pequenos detalhes em vermelho - INTERESSANTE)
//redmond (azul claro com branco)
//rocket (verde claro)
//sam (igual ao ARISTO)
//smoothness (tema cinza claro, parecido com ARISTO, só que mais bonito - alguns detalhes em amarelo claro)
//south-street (verde)
//start (azul)
//sunny (amarelo)
//swanky-purse (marrom - DIFERENTÃO - todo quadriculado e com fontes diferentes - "psicodélico")
//trontastic (escuro com detalhes e realces em verde claro)
//ui-darkness (escuro-preto, com detalhes em cinza escuro e seleções em azul claro)
//ui-lightness (cinza com laranja claro)
//vader (tema escuro, só que com a cor PRETA ao fundo - BOM)


//PRIMEFACES 10.0.0-RC1
//saga (o mesmo tema da página do primefaces - detalhes em azul, cinza e algumas transparências)
//arya (tema escuro - preto, com detalhes em azul claro)



@Component
@ViewScoped
@Data
public class ConfigPrimefacesTemas implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String tema = "aristo";
	

	public void mudaTema(String novoTema)
	{
		tema = novoTema;
	}




}
