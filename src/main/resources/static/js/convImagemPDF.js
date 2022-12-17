window.document.getElementById('formPrincipal:idEnviar').style.display = 'none';
window.document.getElementById('formPrincipal:outroArquivo').style.display = 'none';
window.document.getElementById('formPrincipal:outroArquivoAoBaixar').style.display = 'none';
window.document.getElementById('formPrincipal:refOperacao').style.display = 'none';
window.document.getElementById('formPrincipal:btnAjustarImagem').style.display = 'none';
window.document.getElementById('formPrincipal:btnFecharAjustarImagem').style.display = 'none';
window.document.getElementById('divRegularImagem').style.display = 'none';
// window.document.getElementById('formPrincipal:cardRegularImagem').style.display = 'none';


//****************************************************************************************************************
//FUNÇÕES PARA FAZER O BOTÃO 'ENVIAR', APARECER QUANDO SE APERTA O BOTÃO DE 'ESCOLHER'

//TENTEI FAZER ISSO DE TODAS AS FORMAS, COM PRIMEFACES, MAS O BOTÃO DO fileupload NO MODO SIMPLES, NÃO ACEITA
//NENHUM TIPO DE EVENTO E, AO MESMO TEMPO, O fileupload NO MODO AVANÇADO É CHEIO DE PROBLEMAS.
//FAZER ATRAVÉS DE JAVASCRIPT FOI A MELHOR SOLUÇÃO. E... RECOMENDO QUE NÃO SE TENTE FAZER ISSO DE 
//OUTRA FORMA, PARA QUE NÃO SE PERCA GRANDE QUANTIDADE DE TEMPO
function mostraBotaoEnviar() {
	window.document.getElementById('formPrincipal:idEnviar').style.display = 'flex';
	window.document.getElementById('captcha').style.display = 'flex';
	window.document.getElementById('formPrincipal:btnAjustarImagem').style.display = 'flex';
	// window.document.getElementById('divRegularImagem2').style.display = 'flex';

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

window.addEventListener('pagehide', apertaBotoesAoFecharAba, true);


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





function mostrarAjustarImagens(){
		window.document.getElementById('formPrincipal:btnAjustarImagem').style.display = 'none';
		window.document.getElementById('formPrincipal:btnFecharAjustarImagem').style.display = 'flex';
		window.document.getElementById('divRegularImagem').style.display = 'flex';
}

function esconderAjustarImagens(){
		window.document.getElementById('formPrincipal:btnAjustarImagem').style.display = 'flex';
		window.document.getElementById('formPrincipal:btnFecharAjustarImagem').style.display = 'none';
		window.document.getElementById('divRegularImagem').style.display = 'none';
}

// function adicionarMais() {
// 	var campo = window.document.getElementById('formPrincipal:nbrAumentar_input');
// 	var valorAtual = window.document.getElementById('formPrincipal:nbrAumentar_input').value;
// 	var valorConvertidoZero = parseFloat(0);

// 	if (valorAtual <= 0 || valorAtual == "") {
// 		campo.value = valorConvertidoZero + parseFloat(0.10);
// 	} else {
// 		campo.value = parseFloat(valorAtual) + parseFloat(0.10);
// 	}
// }

// function adicionarMenos() {
// 	var campo = window.document.getElementById('formPrincipal:nbrDiminuir_input');
// 	var valorAtual = window.document.getElementById('formPrincipal:nbrDiminuir_input').value;
// 	var valorConvertidoZero = parseFloat(0);

// 	if (valorAtual <= 0 || valorAtual == "") {
// 		campo.value = valorConvertidoZero + parseFloat(0.10);
// 	} else {
// 		campo.value = parseFloat(valorAtual) + parseFloat(0.10);
// 	}

// }










