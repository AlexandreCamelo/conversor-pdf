window.document.getElementById('formPrincipal:idEnviar').style.display = 'none';
window.document.getElementById('formPrincipal:outroArquivo').style.display = 'none';
window.document.getElementById('formPrincipal:outroArquivoAoBaixar').style.display = 'none';
window.document.getElementById('formPrincipal:refOperacao').style.display = 'none';

window.addEventListener('pagehide', apertaBotoesAoFecharAba, true);

window.addEventListener('resize', larguraTela, true);

function larguraTela() {
	var largura = window.innerWidth;
	document.getElementById('divLargura').innerHTML = largura;
}

//****************************************************************************************************************
//FUNÇÕES PARA FAZER O BOTÃO 'ENVIAR', APARECER QUANDO SE APERTA O BOTÃO DE 'ESCOLHER'

//TENTEI FAZER ISSO DE TODAS AS FORMAS, COM PRIMEFACES, MAS O BOTÃO DO fileupload NO MODO SIMPLES, NÃO ACEITA
//NENHUM TIPO DE EVENTO E, AO MESMO TEMPO, O fileupload NO MODO AVANÇADO É CHEIO DE PROBLEMAS.
//FAZER ATRAVÉS DE JAVASCRIPT FOI A MELHOR SOLUÇÃO. E... RECOMENDO QUE NÃO SE TENTE FAZER ISSO DE 
//OUTRA FORMA, PARA QUE NÃO SE PERCA GRANDE QUANTIDADE DE TEMPO
function mostraBotaoEnviar() {
	window.document.getElementById('formPrincipal:idEnviar').style.display = 'flex';
	window.document.getElementById('captcha').style.display = 'flex';
}

function load() {
	var el = document.getElementById("formPrincipal:escolher_input");
	el.addEventListener("click", mostraBotaoEnviar, false);
}

document.addEventListener("DOMContentLoaded", load, false);

function apertaBotoesAoFecharAba() {
	document.getElementById(
		'formPrincipal:botaoPararProcessosInvisivel').click();
}







function redirecionarParaAMesmaPagina() {
	document.location.href = document.location.href;
}

function escondeDownloadEMostraRedirecionar() {
	window.document.getElementById('divBaixar').style.display = 'none';
	window.document.getElementById('formPrincipal:lblNomeArquivo').style.display = 'none';
	window.document.getElementById('formPrincipal:colunaNomeArquivo').style.display = 'none';
	window.document.getElementById('formPrincipal:outroArquivo').style.display = 'flex';
}

function escondeEscolherMostraConverterOutro() {
	window.document.getElementById('formPrincipal:escolher_label').style.display = 'none';
	window.document.getElementById('formPrincipal:outroArquivo').style.display = 'flex';
}

function rejeicaoCaptcha() {
	window.document.getElementById('divBaixar').style.display = 'none';
	window.document.getElementById('formPrincipal:lblNomeArquivo').style.display = 'none';
	window.document.getElementById('formPrincipal:colunaNomeArquivo').style.display = 'none';
	window.document.getElementById('divEscolha').style.display = 'none';
	window.document.getElementById('formPrincipal:refOperacao').style.display = 'flex';
}



// function fazerAparecerDIV(caminhoImagem, altura, largura, divOndeImagensDevemFicar) {
// 	console.log('O CAMINHO DAS IMAGENS É: ' + caminhoImagem);
// 	var caminho2 = [];
// 	caminho2 = caminhoImagem;
// 	var totalDeFotos = caminho2.length;
// 	var totalDeFotosNaDIV = document.querySelectorAll('#divImagensPDF img').length;

// 	if (totalDeFotosNaDIV >= totalDeFotos) {
// 		return;
// 	}

// 	var divisoria = window.document.getElementById(divOndeImagensDevemFicar);

// 	for (item = 0; item < caminho2.length; item++) {
// 		console.log("Item: " + item);
// 		var imagem = document.createElement('img');
// 		imagem.src = caminho2[item];
// 		imagem.style = 'height: ' + altura + 'px; width: auto; border: 1px solid #000; margin: 10px;';
// 		divisoria.appendChild(imagem);
// 	}

// 	console.log("A quantidade de elementos na DIV é: " + document.querySelectorAll('#divImagensPDF img').length);


// }



function escondeDivEscolha() {
	document.getElementById('formPrincipal:escolher_label').style.display = 'none';
	document
			.querySelector('#formPrincipal\\:escolher > span.ui-button.ui-widget.ui-state-default.ui-corner-all.ui-button-text-icon-left').style.display = 'none';
	document.getElementById('divDaEscolha').style.height = "0px";
}

function mostrarPainelBaixar() {
	document.getElementById('formPrincipal:colunaNomeArquivo').style.display = 'flex';
	document.getElementById('formPrincipal:outroArquivoAoBaixar').style.display = 'flex';
}

function handleDrop(event, ui) {
	var droppedProduct = ui.draggable;
	droppedProduct.fadeOut('fast');
}

function renderizarDragDropPeloTamanhoDaTela(mostrar) {
	var largura = window.innerWidth;

	if (mostrar) {

		if (largura >= 1200) {
			document.getElementById('divDragDrop').style.display = 'flex';
			document.getElementById('divDragDropMob').style.display = 'none';
			document.getElementById('divBotaoVoltarEscolhaModoGrande').style.display = 'flex';

		} else {
			document.getElementById('divDragDrop').style.display = 'none';
			document.getElementById('divDragDropMob').style.display = 'flex';
			document.getElementById('divBotaoVoltarEscolhaModoGrande').style.display = 'none';

		}

	}

	else {
		document.getElementById('divDragDrop').style.display = 'none';
		document.getElementById('divDragDropMob').style.display = 'none';
		document.getElementById('divBotaoVoltarEscolhaModoGrande').style.display = 'none';
	}

}

function mostraDialogFotoAltaResolucao() {
	var largura = window.innerWidth;

	if (largura >= 1200) {
		PF('dlgFotoAltaResolucao').show();

	} else {
		PF('dlgFotoAltaResolucaoMob').show();
	}

}




function escModoIntervalo() {
	document.getElementById('divIntervalos').style.display = 'flex';
	document.getElementById('divEscolhaModo').style.display = 'none';

}

function escModoDragDrop() {
	var largura = window.innerWidth;

	document.getElementById('divEscolhaModo').style.display = 'none';
	renderizarDragDropPeloTamanhoDaTela(true);
}

function voltaEscolhaModo() {
	renderizarDragDropPeloTamanhoDaTela(false);
	document.getElementById('divIntervalos').style.display = 'none';
	document
			.getElementById('divBotaoVoltarEscolhaModoGrande').style.display = 'none';
	document.getElementById('divEscolhaModo').style.display = 'flex';
}


function legendaTabelaVazia() {
	var conteudoDIVSelec = document
			.querySelector("#formPrincipal\\:paginasSelecionadas_content").innerHTML;

	var conteudoDIVSelecMob = document
			.querySelector("#formPrincipal\\:paginasSelecionadasMob_content").innerHTML;

	if (conteudoDIVSelec == "Nenhum registro encontrado.") {
		document
				.querySelector("#formPrincipal\\:paginasSelecionadas_content").innerHTML = "Arraste as páginas para cá!";
	}

	if (conteudoDIVSelecMob == "Nenhum registro encontrado.") {
		document
				.querySelector("#formPrincipal\\:paginasSelecionadasMob_content").innerHTML = "Nenhuma página para separar";

	}
}

legendaTabelaVazia();








