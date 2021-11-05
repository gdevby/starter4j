package by.gdev.http.head.cache.impl;


/**
 * {@inheritDoc}
 */

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
import java.security.NoSuchAlgorithmException;

import com.google.gson.Gson;

import by.gdev.http.head.cache.model.Headers;
import by.gdev.http.head.cache.model.RequestMetadata;
import by.gdev.http.head.cache.service.FileCacheService;
import by.gdev.http.head.cache.service.HttpService;
import by.gdev.util.DesktopUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FileCacheServiceImpl implements FileCacheService {
	/**
	 * {@inheritDoc}
	 */
	
	private HttpService httpService;
	private Gson gson;
	private Charset charset;
	private Path directory;
	private int timeToLife;

	 /**
	  * {@inheritDoc}
	  */

	@Override
	public Path getRawObject(String url, boolean cache) throws IOException, NoSuchAlgorithmException {
		Path urlPath = Paths.get(directory.toString(), url.replaceAll("://", "_").replaceAll("[:?=]", "_"));
		Path metaFile = Paths.get(String.valueOf(urlPath).concat(".metadata"));
		if (cache == true) {
			return getResourceWithoutHttpHead(url, metaFile, urlPath);
		} else {
			return getResourceWithHttpHead(url, urlPath, metaFile);
		}
	}

	private Path getResourceWithoutHttpHead(String url, Path metaFile, Path urlPath) throws IOException, NoSuchAlgorithmException {
		long purgeTime = System.currentTimeMillis() - (timeToLife * 1000);
		if (urlPath.toFile().lastModified() < purgeTime) 
			Files.deleteIfExists(urlPath); 
		if (urlPath.toFile().exists()) {
			RequestMetadata localMetadata = read(metaFile, RequestMetadata.class);
			String sha = DesktopUtil.getChecksum(urlPath.toFile(), Headers.SHA1.getValue());
			if (sha.equals(localMetadata.getSha1())) {
				return urlPath;
			} else {
				RequestMetadata serverMetadata = httpService.getResourseByUrlAndSave(url, urlPath);
				createSha(serverMetadata, urlPath, metaFile);
				return urlPath;
			}
		} else {
			RequestMetadata serverMetadata = httpService.getResourseByUrlAndSave(url, urlPath);
			checkMetadataFile(metaFile, url);
//			createSha(serverMetadata, urlPath, metaFile);
			return urlPath;
		}
	}
	
	private Path getResourceWithHttpHead(String url, Path urlPath, Path metaFile) throws IOException, NoSuchAlgorithmException {	
		boolean fileExists = urlPath.toFile().exists();
		checkMetadataFile(metaFile, url);
		if (fileExists) {
			RequestMetadata serverMetadata = httpService.getMetaByUrl(url);
			RequestMetadata localMetadata = read(metaFile, RequestMetadata.class);
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

	private <T> T read(Path file, Class<T> clas) throws FileNotFoundException, IOException {
		try (BufferedReader read = new BufferedReader(new FileReader(file.toFile()))) {
			return gson.fromJson(read, clas);
		}
	}

	private void write(Object create, Path config) throws FileNotFoundException, IOException {
		if (Files.notExists(config.getParent()))
			Files.createDirectories(config.getParent());
		try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(config.toFile()), charset)) {
			gson.toJson(create, out);
		}
	}

	private void createSha(RequestMetadata metadata, Path urlPath, Path metaFile) throws IOException, NoSuchAlgorithmException {
		metadata.setSha1(DesktopUtil.getChecksum(urlPath.toFile(), "SHA-1"));
		write(metadata, metaFile);
	}
	
	private void checkMetadataFile(Path metaFile, String url) throws IOException {
	    if (!metaFile.toFile().exists()) {
	      RequestMetadata metadata = httpService.getMetaByUrl(url);
	      write(metadata, metaFile);
	    }
	  }
	
}