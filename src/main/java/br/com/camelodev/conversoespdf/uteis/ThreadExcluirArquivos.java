package br.com.camelodev.conversoespdf.uteis;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadExcluirArquivos extends Thread {
	private final int _2HORAS = 7200000;
	private static final long LIMITE_HORAS = 2;
	private String pastaPrincipal;
	private File filePasta;

	
	private static final Logger logger = LoggerFactory.getLogger(ThreadExcluirArquivos.class);
	
	
	public ThreadExcluirArquivos(String pastaGeral) {
		pastaPrincipal = pastaGeral;
		filePasta = new File(pastaPrincipal);
	}





	public void run() {

		try {
			logger.warn("THREAD DE DELEÇÃO DE ARQUIVOS NÃO USADOS, INICIADA");
			tarefasDaThread();

			while (true) {
				Thread.sleep(_2HORAS);
				tarefasDaThread();
			}

		} catch (Exception e) {
			logger.warn("THREAD DE BACKUP INTERROMPIDA!!!!");
			e.printStackTrace();
		}

	}





	private void tarefasDaThread() throws Exception {
		String[] listaPastas = filePasta.list();
		logger.info("PASSANDO PELA THREAD DE APAGAMENTO...");
		logger.info("DATA e HORA: " + LocalDateTime.now());

		for (String pastaListada : listaPastas) {
			String caminhoCompleto = pastaPrincipal + "/" + pastaListada;
			File p = new File(caminhoCompleto);
			long horas = horasDesdeAModificaco(caminhoCompleto);

			if (horas >= LIMITE_HORAS) {

				if (p.isDirectory()) {
					FileUtils.deleteDirectory(p);
					log.info(p.getName() + ": PASTA EXCLUÍDA!");
				} else {
					log.info(p.getName() + " APAGADO.");
					p.delete();
				}

			}

		}

	}





	private static long horasDesdeAModificaco(String caminhoCompletoArquivo) {

		try {
			BasicFileAttributes attrs;
			String pattern = "yyyy-MM-dd HH:mm:ss";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			File p = new File(caminhoCompletoArquivo);
			attrs = Files.readAttributes(p.toPath(), BasicFileAttributes.class);
			FileTime time = attrs.lastModifiedTime();
			String strDataArquivo = simpleDateFormat.format(new Date(time.toMillis()));
			Date dataArquivo = simpleDateFormat.parse(strDataArquivo);
			long miliArquivo = dataArquivo.getTime();
			long miliAgora = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
			long horasDesdeACriacao = ((miliAgora - miliArquivo) / 1000 / 60 / 60);
			return horasDesdeACriacao;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

	}
}
