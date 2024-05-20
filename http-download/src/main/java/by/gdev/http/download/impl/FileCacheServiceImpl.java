package by.gdev.http.download.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

import by.gdev.http.download.model.Headers;
import by.gdev.http.download.model.RequestMetadata;
import by.gdev.http.download.service.FileCacheService;
import by.gdev.http.download.service.HttpService;
import by.gdev.util.DesktopUtil;
import by.gdev.util.model.download.Metadata;
import by.gdev.utils.service.FileMapperService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileCacheServiceImpl implements FileCacheService {
	private HttpService httpService;
	/**
	 * Directory for storing files and metadata files downloaded from the server
	 */
	private Path directory;
	/**
	 * The time that the file is up-to-date
	 */
	private int timeToLife;

	private FileMapperService fileMapperService;

	public FileCacheServiceImpl(HttpService httpService, Gson gson, Charset charset, Path directory, int timeToLife) {
		this.httpService = httpService;
		this.directory = directory;
		this.timeToLife = timeToLife;
		fileMapperService = new FileMapperService(gson, charset, "");
	}

	/**
	 * {@inheritDoc}
	 */

	@Override
	public Path getRawObject(String url, boolean cache) throws IOException, NoSuchAlgorithmException {
		Path urlPath = Paths.get(directory.toString(), url.replaceAll("://", "_").replaceAll("[:?=]", "_"));
		Path metaFile = Paths.get(String.valueOf(urlPath).concat(".metadata"));
		if (cache) {
			return getResourceWithoutHttpHead(url, metaFile, urlPath);
		} else {
			return getResourceWithHttpHead(url, urlPath, metaFile);
		}
	}

	@Override
	public Path getRawObject(List<String> urls, boolean cache) throws IOException, NoSuchAlgorithmException {
		IOException error = null;
		for (String url : urls) {
			try {
				Path urlPath = Paths.get(directory.toString(), url.replaceAll("://", "_").replaceAll("[:?=]", "_"));
				Path metaFile = Paths.get(String.valueOf(urlPath).concat(".metadata"));
				return cache ? getResourceWithoutHttpHead(url, metaFile, urlPath)
						: getResourceWithHttpHead(url, urlPath, metaFile);
			} catch (IOException e) {
				error = e;
			}
		}
		throw error;
	}

	@Override
	public Path getRawObject(List<String> urls, Metadata metadata, boolean cache)
			throws IOException, NoSuchAlgorithmException {
		IOException error = null;
		for (String url : urls) {
			try {
				return getRawObject(url + metadata.getRelativeUrl(), cache);
			} catch (IOException e) {
				error = e;
			}
		}
		throw error;
	}

	@Override
	public Path getRawObject(List<String> urls) throws NoSuchAlgorithmException, IOException {
		IOException error = null;
		for (String url : urls) {
			try {
				Path urlPath = Paths.get(directory.toString(), url.replaceAll("://", "_").replaceAll("[:?=]", "_"))
						.toAbsolutePath();
				Path metaFile = Paths.get(String.valueOf(urlPath).concat(".metadata")).toAbsolutePath();
				if (urlPath.toFile().exists() && Files.exists(metaFile)) {
					RequestMetadata localMetadata = fileMapperService.read(metaFile.toString(), RequestMetadata.class);
					String sha = DesktopUtil.getChecksum(urlPath.toFile(), Headers.SHA1.getValue());
					if (Objects.isNull(localMetadata) || !Objects.equals(localMetadata.getSha1(), sha))
						throw new IOException("sha not equals");
					return urlPath;
				}
			} catch (IOException e) {
				error = e;
			}
		}
		if (Objects.nonNull(error))
			throw error;
		else
			return null;
	}

	private Path getResourceWithoutHttpHead(String url, Path metaFile, Path urlPath)
			throws IOException, NoSuchAlgorithmException {
		long purgeTime = System.currentTimeMillis() - (timeToLife * 1000);
		if (urlPath.toFile().lastModified() < purgeTime)
			Files.deleteIfExists(urlPath);
		if (urlPath.toFile().exists() && Files.exists(metaFile)) {
			RequestMetadata localMetadata = fileMapperService.read(metaFile.toString(), RequestMetadata.class);
			String sha = DesktopUtil.getChecksum(urlPath.toFile(), Headers.SHA1.getValue());
			if (Objects.nonNull(localMetadata) && Objects.equals(localMetadata.getSha1(), sha)) {
				log.trace("use local file -> " + url);
				return urlPath;
			} else {
				log.trace("not proper hashsum HTTP GET -> " + url);
				generateRequestMetadata(url, urlPath, metaFile);
				return urlPath;
			}
		} else {
			log.trace("HTTP GET -> " + url);
			generateRequestMetadata(url, urlPath, metaFile);
			return urlPath;
		}
	}

	private Path getResourceWithHttpHead(String url, Path urlPath, Path metaFile)
			throws IOException, NoSuchAlgorithmException {
		boolean fileExists = urlPath.toFile().exists();
		try {
			if (fileExists) {
				RequestMetadata serverMetadata = httpService.getMetaByUrl(url);
				RequestMetadata localMetadata = fileMapperService.read(metaFile.toString(), RequestMetadata.class);
				if (Objects.nonNull(localMetadata)
						&& StringUtils.equals(serverMetadata.getETag(), localMetadata.getETag())
						&& StringUtils.equals(serverMetadata.getLastModified(), localMetadata.getLastModified())
						&& StringUtils.equals(DesktopUtil.getChecksum(urlPath.toFile(), "SHA-1"),
								localMetadata.getSha1())) {
					return urlPath;
				} else {
					return generateRequestMetadata(url, urlPath, metaFile);
				}
			} else {
				return generateRequestMetadata(url, urlPath, metaFile);
			}
		} catch (Exception e) {
			log.error("error with url " + url);
			throw e;
		}
	}

	private Path generateRequestMetadata(String url, Path urlPath, Path metaFile)
			throws IOException, NoSuchAlgorithmException {
		RequestMetadata requestMetadata = httpService.getRequestByUrlAndSave(url, urlPath);
		requestMetadata.setSha1(DesktopUtil.getChecksum(urlPath.toFile(), "SHA-1"));
		fileMapperService.write(requestMetadata, metaFile.toString());
		return urlPath;
	}
}