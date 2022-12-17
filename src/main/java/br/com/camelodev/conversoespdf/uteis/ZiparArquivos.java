package br.com.camelodev.conversoespdf.uteis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZiparArquivos {
	
	
	
	public static void ziparArquivo(File file, String zipName) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(zipName + ".zip");
		ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
		addFileInZip(file, zipOutputStream);
		zipOutputStream.close();
		fileOutputStream.close();
	}





	public static void ziparArquivoUmAUm(File file, boolean fecharStream, FileOutputStream fileOutputStream,
			ZipOutputStream zipOutputStream) {

		try {
			addFileInZip(file, zipOutputStream);

			if (fecharStream) {
				zipOutputStream.close();
				fileOutputStream.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}





	public static void ziparVariosArquivos(List<File> files, String zipName) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(zipName + ".zip");
		ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

		for (File file : files) {
			log.info("Zipando arquivo " + file.getName());
			addFileInZip(file, zipOutputStream);
		}

		zipOutputStream.close();
		fileOutputStream.close();
	}





	public static void zipFolder(File folder, String zipName) throws IOException {
		ziparArquivo(folder, zipName);
	}





	private static void addFileInZip(File file, ZipOutputStream zipOutputStream, String parentPath) throws IOException {

		if (file == null || file.isDirectory() && file.listFiles() == null) {
			return;
		}

		if (file.isDirectory()) {

			for (File childFile : file.listFiles()) {
				addFileInZip(childFile, zipOutputStream, parentPath + "/" + file.getName());
			}

		} else {
			FileInputStream fileInputStream = new FileInputStream(file);
			ZipEntry zipEntry = new ZipEntry(parentPath + "/" + file.getName());
			zipOutputStream.putNextEntry(zipEntry);
			byte[] bytes = new byte[1048576];
			int length;

			while ((length = fileInputStream.read(bytes)) >= 0) {
				zipOutputStream.write(bytes, 0, length);
			}

			fileInputStream.close();
		}

	}





	private static void addFileInZip(File file, ZipOutputStream zipOutputStream) throws IOException {
		addFileInZip(file, zipOutputStream, "");
	}
}