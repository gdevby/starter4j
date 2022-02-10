package by.gdev.http.download.impl;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

import by.gdev.http.download.model.Headers;
import by.gdev.http.download.model.RequestMetadata;
import by.gdev.http.download.service.FileCacheService;
import by.gdev.http.download.service.HttpService;
import by.gdev.util.DesktopUtil;
import by.gdev.utils.service.FileMapperService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class FileCacheServiceImpl implements FileCacheService {
	private HttpService httpService;
	private Gson gson;
	private Charset charset;
	/**
	 * Directory for storing files and metadata files downloaded from the server
	 */
	private Path directory;
	/**
	 * The time that the file is up-to-date
	 */
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
		if (urlPath.toFile().exists() && Files.exists(metaFile)) {
			RequestMetadata localMetadata = new FileMapperService(gson, charset, "").read(metaFile.toString(), RequestMetadata.class);
			String sha = DesktopUtil.getChecksum(urlPath.toFile(), Headers.SHA1.getValue());
			if (sha.equals(localMetadata.getSha1())) {
				log.trace("HTTP HEAD -> " + url);
				return urlPath;
			} else {
				log.trace("not proper hashsum HTTP GET -> " + url);
				RequestMetadata serverMetadata = httpService.getRequestByUrlAndSave(url, urlPath);
				createSha1(serverMetadata, urlPath, metaFile);
				return urlPath;
			}
		} else {
			log.trace("HTTP GET -> " + url);
			httpService.getRequestByUrlAndSave(url, urlPath);
			checkMetadataFile(metaFile, url);
			return urlPath;
		}
	}
	
	private Path getResourceWithHttpHead(String url, Path urlPath, Path metaFile) throws IOException, NoSuchAlgorithmException {	
		boolean fileExists = urlPath.toFile().exists();
		checkMetadataFile(metaFile, url);
		if (fileExists) {
			RequestMetadata serverMetadata = httpService.getMetaByUrl(url);
			RequestMetadata localMetadata = new FileMapperService(gson, charset, "").read(metaFile.toString(), RequestMetadata.class);
			if (StringUtils.equals(serverMetadata.getETag(), localMetadata.getETag())
					& StringUtils.equals(serverMetadata.getContentLength(), localMetadata.getContentLength())
					& StringUtils.equals(serverMetadata.getLastModified(), localMetadata.getLastModified())) {
				return urlPath;
			} else {
				httpService.getRequestByUrlAndSave(url, urlPath);
				new FileMapperService(gson, charset, "").write(serverMetadata, metaFile.toString());
				return urlPath;
			}
		} else {
			RequestMetadata serverMetadata = httpService.getRequestByUrlAndSave(url, urlPath);
			createSha1(serverMetadata, urlPath, metaFile);
			return urlPath;
		}
	}

	private void createSha1(RequestMetadata metadata, Path urlPath, Path metaFile) throws IOException, NoSuchAlgorithmException {
		metadata.setSha1(DesktopUtil.getChecksum(urlPath.toFile(), "SHA-1"));
		new FileMapperService(gson, charset, "").write(metadata, metaFile.toString());
	}
	
	private void checkMetadataFile(Path metaFile, String url) throws IOException {
	    if (!metaFile.toFile().exists()) {
	      RequestMetadata metadata = httpService.getMetaByUrl(url);
	      new FileMapperService(gson, charset, "").write(metadata, metaFile.toString());
	    }
	  }
}