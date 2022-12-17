/**
 * FUNÇÕES JAVASCRIPT ÚTEIS
 */
 

	//#################################################################
	//#########EXEMPLO DE COMO CRIA COMPONENTE COM JAVASCRIPT##########
	//#################################################################
 
 	function constroiFrameFotos() 
	{
		var linkUparquivos = "http://localhost:8080/fragmentos/contasGaleria_min.xhtml";
		
		ifrm = document.createElement("IFRAME");
	   	ifrm.setAttribute("src", linkUparquivos);
	   	ifrm.setAttribute("id", "frameFotos");
	   	
	   	ifrm.style.width = 95+"%";
	   	ifrm.style.height = 400+"px";
	   	ifrm.style.margin = "auto";
	   	ifrm.style.border = "3px solid #000";
	   	
	   	//PF('dlgMid').show();
	   	document.getElementById("divTitulo").appendChild(ifrm);
	   	//document.body.appendChild(ifrm);
	}
 
 
 
 
 