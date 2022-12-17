/*####################################################################################################################
AUTOR: ALEXANDRE CAMELO

Esse arquivo contém 2 métodos para emissão de ALERTAS e CONFIRMAÇÕES ao usuário.
Algumas observações são necessárias:

1) Ele funciona em conjunto com o BOOTSTRAP, ou seja, as mensagens são MODAIS, construídos com HTML e CSS do bootstrap.
O código de um dos modais, foi copiado do site do bootstrap e colado aqui, neste código, de forma que esses modais
são gerados dinamicamente, pelo javascript, no momento em que os métodos são chamados;

2) Como são códigos do bootstrap, é necessário que se tenha o cuidado de colocar as IMPORTAÇÕES(no arquivo HTML) dos arquivos do bootstrap (CSS e JAVASCRIPT),
ACIMA da importação deste arquivo aqui (mensagens.js). Caso as importações sejam inseridas na ordem errada, as mensagens
não funcionarão; 

3) As TAREFAS dos botões, passadas por argumento, podem ser ignoradas. É só colocar '' no lugar do argumento;

4) Como as tarefas dos botões serão MÉTODOS JAVASCRIPT, então NÃO SE PODE ESQUECER DE COLOCAR OS DOIS PARÊNTESES + ';' AO FINAL DO NOME DO MÉTODO. Exemplo:
confirmar("Salvando...", "O novo cliente será salvo. Confirma?", "Sim", "Não", "SalvarInterno();", '');

5) Os métodos abaixo podem ser utilizados, diretamente no HTML ou nos seus arquivos javascript, desde que as importações estejam
inseridas na ordem correta;

6) ***** IMPORTANTE: o código do bootstrap é reativo. Dessa forma, ele é ASSÍCRONO. Ou seja, após o modal ser aberto, o código que vier logo abaixo será executado AO MESMO TEMPO.
Ele não esperará o usuário clicar em um dos botões. Dessa forma, o usuário deverá gerar artifícios para que o código siga seu curso normal (separar em vários métodos, é uma saída).


######################################################################################################################
*/




function mensagem(titulo, corpo, textoBotao, tarefaBotao, campoParaFoco)
{
		
		var testeMensagem = document.getElementById("divModalMensagem");
		if(testeMensagem != '' && testeMensagem != null)
		{
			//É necessário, pois, se não existir, a mensagem fica sendo repetida, pois ele não recria a DIV
			document.getElementById("divModalMensagem").remove();
		}
		
		
		div1 = document.createElement("DIV");
		div1.setAttribute("id", "divModalMensagem");

		
		if(textoBotao == '')
		{
			textoBotao = 'Ok';
		}

		if(titulo == '')
		{
			titulo = 'Aviso';
		}


		if(tarefaBotao == '')
		{
			//Esse é o HTML, copiada da página do bootstrap. É só escolher o modelo do modal desejado
			div1.innerHTML = "<div class='modal fade' id='confirmacao' data-bs-backdrop='static' data-bs-keyboard='false' tabindex='-1' aria-labelledby='staticBackdropLabel' aria-hidden='true'>" +
			"<div class='modal-dialog'><div class='modal-content'>" +
					"<div class='modal-header'>" +
						"<h5 class='modal-title' id='staticBackdropLabel'>" + titulo + "</h5>" +
					   "<button type='button' class='btn-close' data-bs-dismiss='modal' aria-label='Close'></button>" +
						"</div>" +
				   "<div class='modal-body'>" + corpo + "</div>" +
				   "<div class='modal-footer'>" +
					   "<button id='botaoSim' type='button' class='btn btn-primary' data-bs-dismiss='modal'>" + textoBotao + "</button>" +   
				   "</div>" +
				   "</div>" +
				   "</div>" +
		   "</div>";

		}
		else
		{
			div1.innerHTML = "<div class='modal fade' id='confirmacao' data-bs-backdrop='static' data-bs-keyboard='false' tabindex='-1' aria-labelledby='staticBackdropLabel' aria-hidden='true'>" +
			"<div class='modal-dialog'><div class='modal-content'>" +
					"<div class='modal-header'>" +
						"<h5 class='modal-title' id='staticBackdropLabel'>" + titulo + "</h5>" +
					   "<button type='button' class='btn-close' data-bs-dismiss='modal' aria-label='Close'></button>" +
						"</div>" +
				   "<div class='modal-body'>" + corpo + "</div>" +
				   "<div class='modal-footer'>" +
				   "<button id='botaoSim' type='button' class='btn btn-secondary' data-bs-dismiss='modal' onclick='" + tarefaBotao + "'>" + textoBotao + "</button>" +   
			   "</div>" +
				   "</div>" +
				   "</div>" +
		   "</div>";

		}

		document.body.appendChild(div1);


		var myModalEl = document.getElementById('confirmacao')
		myModalEl.addEventListener('hidden.bs.modal', function (event) {
			
			if(campoParaFoco != '' && campoParaFoco != null)
			{
				document.getElementById(campoParaFoco).focus();
			}
			
		})



		var confirmacao = new bootstrap.Modal(document.getElementById('confirmacao'), 
		{
			backdrop: 'static'
		}
		);
	
		confirmacao.show();


	}








	function confirmar(titulo, corpo, textoBotaoSim, textoBotaoNao, tarefaBotaoSim, tarefaBotaoNao)
	{
		
		var testeMensagem = document.getElementById("divModalMensagem");
		if(testeMensagem != '' && testeMensagem != null)
		{
			//É necessário, pois, se não existir, a mensagem fica sendo repetida, pois ele não recria a DIV
			document.getElementById("divModalMensagem").remove();
		}
		
		
		div1 = document.createElement("DIV");
		div1.setAttribute("id", "divModalMensagem");

		
		//Essa é o HTML, copiada da página do bootstrap. É só escolher o modelo do modal desejado

		if(tarefaBotaoNao == '')
		{
			div1.innerHTML = "<div class='modal fade' id='confirmacao' data-bs-backdrop='static' data-bs-keyboard='false' tabindex='-1' aria-labelledby='staticBackdropLabel' aria-hidden='true'>" +
			"<div id='divMod1' class='modal-dialog'><div class='modal-content'>" +
					"<div id='divMod2' class='modal-header'>" +
						"<h5 class='modal-title' id='staticBackdropLabel'>" + titulo + "</h5>" +
					   "<button type='button' class='btn-close' data-bs-dismiss='modal' aria-label='Close'></button>" +
						"</div>" +
				   "<div id='divMod3' class='modal-body'>" + corpo + "</div>" +
				   "<div id='divMod4' class='modal-footer'>" +
					   "<button id='botaoNao' type='button' class='btn btn-primary' onclick='" + tarefaBotaoSim + "'>" + textoBotaoSim + "</button>" +
					   "<button id='botaoSim' type='button' class='btn btn-secondary' data-bs-dismiss='modal'>" + textoBotaoNao + "</button>" +   
				   "</div>" +
				   "</div>" +
				   "</div>" +
		   "</div>";

		}
		else
		{
			div1.innerHTML = "<div class='modal fade' id='confirmacao' data-bs-backdrop='static' data-bs-keyboard='false' tabindex='-1' aria-labelledby='staticBackdropLabel' aria-hidden='true'>" +
			"<div id='divMod1' class='modal-dialog'><div class='modal-content'>" +
					"<div id='divMod2' class='modal-header'>" +
						"<h5 class='modal-title' id='staticBackdropLabel'>" + titulo + "</h5>" +
					   "<button type='button' class='btn-close' data-bs-dismiss='modal' aria-label='Close'></button>" +
						"</div>" +
				   "<div id='divMod3'class='modal-body'>" + corpo + "</div>" +
				   "<div id='divMod4' class='modal-footer'>" +
				   "<button id='botaoNao' type='button' class='btn btn-primary' onclick='" + tarefaBotaoSim + "'>" + textoBotaoSim + "</button>" +
				   "<button id='botaoSim' type='button' class='btn btn-secondary' data-bs-dismiss='modal' onclick='" + tarefaBotaoNao  + "'>" + textoBotaoNao + "</button>" +   
			   "</div>" +
				   "</div>" +
				   "</div>" +
		   "</div>";

		}


		
		
		
		document.body.appendChild(div1);

		var confirmacao = new bootstrap.Modal(document.getElementById('confirmacao'), 
		{
			backdrop: 'static'
		}
		);
	
		confirmacao.show();
	

	}


	

















