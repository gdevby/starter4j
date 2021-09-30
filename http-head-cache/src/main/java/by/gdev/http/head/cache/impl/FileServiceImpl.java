package by.gdev.http.head.cache.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.gson.Gson;

import by.gdev.http.head.cache.model.RequestMetadata;
import by.gdev.http.head.cache.service.FileService;
import by.gdev.http.head.cache.service.HttpService;

public class FileServiceImpl implements FileService {
	/**
	 * Saved files and searched files in this directory
	 */
	//todo private use
	Gson gson;
	Charset charset;
	private Path directory;
	private HttpService httpService;

	public FileServiceImpl(HttpService httpService, Gson gson, Charset charset) {
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
	 * @throws NoSuchAlgorithmException 
	 */

	@Override
	public Path getRawObject(String url, boolean cache) throws IOException, NoSuchAlgorithmException {
		//todo получать по конструктору
		directory = Paths.get("target");
		Path urlPath = Paths.get(directory.toString(), url.replaceAll("://", "_").replaceAll("[:?=]", "_"));
		Path metaFile = Paths.get(String.valueOf(urlPath).concat(".metadata"));
		//в любом случае обращаемся к серверу?
		checkMetadataFile(metaFile, url);
		if (cache == true) {
			//пишется method
			return cacheMetod(url, metaFile, urlPath);
		} else {
			return defaultMetod(url, urlPath, metaFile);
		}
	}
	
	private Path cacheMetod(String url, Path metaFile, Path urlPath) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		//время указывать через конструктор в секундах будет, название метода странное
		long purgeTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
		if (urlPath.toFile().lastModified() < purgeTime)
			Files.deleteIfExists(urlPath);
		RequestMetadata localMetadata = read(metaFile, RequestMetadata.class);
		String sha = getChecksum(urlPath.toFile(), "SHA-1");
		if (sha.equals(localMetadata.getSha1())) {
			return urlPath;
		} else {
			RequestMetadata serverMetadata = httpService.getResourseByUrlAndSave(url, urlPath);
			createSha(serverMetadata, urlPath, metaFile);
			return urlPath;
		}
	}
	
	private Path defaultMetod(String url, Path urlPath, Path metaFile) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		if (urlPath.toFile().exists()) {
			RequestMetadata localMetadata = read(metaFile, RequestMetadata.class);
			RequestMetadata serverMetadata = httpService.getMetaByUrl(url);
			//what is it?
			createSha(serverMetadata, urlPath, metaFile);
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
			RequestMetadata serverMetadata = httpService.getResourseByUrlAndSave(url, urlPath);
			createSha(serverMetadata, urlPath, metaFile);
			return urlPath;
		}
	}	
	//todo why public
	public <T> T read(Path file, Class<T> clas) throws FileNotFoundException, IOException {
		try (BufferedReader read = new BufferedReader(new FileReader(file.toFile()))) {
			return gson.fromJson(read, clas);
		}
	}
	
	private void checkMetadataFile(Path metaFile, String url) throws IOException {
		if (!metaFile.toFile().exists()) {
			RequestMetadata metadata = httpService.getMetaByUrl(url);
			write(metadata, metaFile);
		}
	}

	private void write(Object create, Path config) throws FileNotFoundException, IOException {
		if (Files.notExists(config.getParent()))
			Files.createDirectories(config.getParent());
		try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(config.toFile()), charset)) {
			gson.toJson(create, out);
		}
	}
	
	private void createSha (RequestMetadata metadata, Path urlPath, Path metaFile) throws IOException, NoSuchAlgorithmException {
		metadata.setSha1(getChecksum(urlPath.toFile(), "SHA-1"));
		write(metadata, metaFile);
	}
	//connect desktop util and use that method
	private static String getChecksum(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
		byte[] b = createChecksum(file, algorithm);
		StringBuilder result = new StringBuilder();
		for (byte cb : b)
			result.append(Integer.toString((cb & 0xff) + 0x100, 16).substring(1));
		return result.toString();
	}

	private static byte[] createChecksum(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
		try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file))) {
			byte[] buffer = new byte[8192];
			MessageDigest complete = MessageDigest.getInstance(algorithm);
			int numRead;
			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);
			return complete.digest();
		}
	}
}