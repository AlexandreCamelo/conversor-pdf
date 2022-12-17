 package br.com.camelodev.conversoespdf.bean;

import java.io.FileOutputStream;

import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;


public class ArquivoEnviadoDTO {

 public static String nomeArquivoEnviado;
 public static String nomeArquivoEnviadoParaBaixar;
 public static String caminhoCompletoArquivoEnviado;
 public static String pastaOndeColocarImagens;
 public static UploadedFile uplArquivoEnviado;
 public static boolean pararProcesso = false;
 public static StreamedContent arquivoParaBaixar;
 public static FileOutputStream arquivoStream;
 public static int totalCaracteresArquivoGerado;
 public static boolean palavraLongaTxtPdf = false;	
 public static boolean umArquivoPorFoto = false;	
	
	
	
}
