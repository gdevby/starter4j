package http.head.get.hash.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.client.methods.CloseableHttpResponse;

import com.google.gson.Gson;

import http.head.get.hash.model.RequestMetadata;

public class FileService {
	/**
	 * Saved files and searched files in this directory
	 */
	Gson gson;
	Charset charset;
	private Path directory;
	private HttpService httpService;

	public FileService(HttpService httpService, Gson gson, Charset charset) {
		this.httpService = httpService;
		this.gson = gson;
		this.charset = charset;
	}

	/**
	 * 
	 * @param url
	 * @param cache - If cache = true file exists and hashsum is valid it should
	 *              return content without head.
	 * @return
	 * @throws IOException
	 */
	public Path getRawObject(String url, boolean cache) throws IOException {
		directory = Paths.get("target");
		Path urlPath = Paths.get(directory.toString(), url.replaceAll("://", "_").replaceAll("[:?=]", "_"));
		Path metaFile = Paths.get(String.valueOf(urlPath).concat(".metadata"));
		checkMetadataFile(metaFile, url);

		if (urlPath.toFile().exists()) {
			RequestMetadata localMetadata = (RequestMetadata) read(metaFile, RequestMetadata.class);
			CloseableHttpResponse headResponse = httpService.getHeadResponse(url);
			RequestMetadata serverMetadata = httpService.getMetaByUrl(url, headResponse);
			if (serverMetadata.getETag().equals(localMetadata.getETag())
					& serverMetadata.getContentLength().equals(localMetadata.getContentLength())
					& serverMetadata.getLastModified().equals(localMetadata.getLastModified())) {
				return urlPath;
			} else {
				httpService.getResourseByUrlAndSave(url, urlPath);
				write(serverMetadata, metaFile);
				return urlPath;
			}
		} else {
			httpService.getResourseByUrlAndSave(url, urlPath);
			return urlPath;
		}
		/**
		 * делаем замену и у нас новый юрл+ проверяем по этому пути наличие файла в
		 * папке если файл есть, то считываем метаданные из файла( который лежит рядом
		 * имя файла.мета делаем запрос хэд и сравниваем метаданные, если они равны,
		 * тогда возвращаем локальный файл если не равны , тогда делаем запрос гет и
		 * возвращаем новый файл
		 * 
		 * Если файла нету, то вызываем гет
		 */
	}

	private void checkMetadataFile(Path metaFile, String url) throws IOException {
		if (!metaFile.toFile().exists()) {
			CloseableHttpResponse headResponse = httpService.getHeadResponse(url);
			RequestMetadata metadata = httpService.getMetaByUrl(url, headResponse);
			write(metadata, metaFile);
		}
	}

	public <T> T read(Path file, Class<T> clas) throws FileNotFoundException, IOException {
		try (BufferedReader read = new BufferedReader(new FileReader(file.toFile()))) {
			return gson.fromJson(read, clas);
		}
	}

	public void write(Object create, Path config) throws FileNotFoundException, IOException {
		if (Files.notExists(config.getParent()))
			Files.createDirectories(config.getParent());
		try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(config.toFile()), charset)) {
			gson.toJson(create, out);
		}
	}
}