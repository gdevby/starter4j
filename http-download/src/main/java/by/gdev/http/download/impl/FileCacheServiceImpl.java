package by.gdev.http.download.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

import by.gdev.http.download.model.Headers;
import by.gdev.http.download.model.RequestMetadata;
import by.gdev.http.download.service.FileCacheService;
import by.gdev.http.download.service.HttpService;
import by.gdev.util.DesktopUtil;
import by.gdev.util.model.InternetServerMap;
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
	/**
	 * Can skip requests except this servers. Used only for related uri.
	 */
	private InternetServerMap workedServers;

	public FileCacheServiceImpl(HttpService httpService, Gson gson, Charset charset, Path directory, int timeToLife,
			InternetServerMap workedServers) {
		this.httpService = httpService;
		this.directory = directory;
		this.timeToLife = timeToLife;
		fileMapperService = new FileMapperService(gson, charset, "");
		this.workedServers = workedServers;
	}

	private Path getRawObject(String url, boolean cache, Path savedPath) throws IOException {
		Path metaFile = Paths.get(String.valueOf(savedPath).concat(".metadata"));
		if (cache) {
			return getResourceWithoutHttpHead(url, metaFile, savedPath);
		} else {
			return getResourceWithHttpHead(url, metaFile, savedPath);
		}
	}

	@Override
	public Path getRawObject(List<String> urls, Metadata metadata, boolean cache)
			throws IOException {
		String urn = metadata.getRelativeUrl();
		return getRawObject1(urls, urn, cache);
	}

	protected Path getRawObject1(List<String> urls, String urn, boolean cache)
			throws IOException {
		IOException ex = null;
		Path savedPath = buildPath(urls.get(0) + urn);
		for (String url : workedServers.getAliveDomainsOrUseAllWithSort(urls)) {
			try {
				return getRawObject(url + urn, cache, savedPath);
			} catch (IOException e) {
				ex = e;
			}
		}
		throw ex;
	}

	@Override
	public Path getRawObject(List<String> urls, String urn, boolean cache)
			throws IOException {
		return getRawObject1(urls, urn, cache);
	}

	@Override
	public Path getLocalRawObject(List<String> urls, Metadata metadata) throws IOException {
		return readLocalRawObject(urls, metadata.getRelativeUrl());
	}

	@Override
	public Path getLocalRawObject(List<String> urls, String urn) throws IOException {
		return readLocalRawObject(urls, urn);
	}

	protected Path readLocalRawObject(List<String> urls, String urn) throws IOException {
		Path savedPath = buildPath(urls.get(0) + urn).toAbsolutePath();
		Path metaFile = Paths.get(String.valueOf(savedPath).concat(".metadata")).toAbsolutePath();
		if (savedPath.toFile().exists() && Files.exists(metaFile)) {
			RequestMetadata localMetadata = fileMapperService.read(metaFile.toString(), RequestMetadata.class);
			String sha = DesktopUtil.getChecksum(savedPath.toFile(), Headers.SHA1.getValue());
			if (Objects.isNull(localMetadata) || !Objects.equals(localMetadata.getSha1(), sha)) {
				throw new IOException("sha not equals");
			}
			return savedPath;
		}
		return null;
	}

	private Path getResourceWithoutHttpHead(String url, Path metaFile, Path urlPath)
			throws IOException {
		if (urlPath.toFile().exists() && Files.exists(metaFile)) {
			RequestMetadata localMetadata = fileMapperService.read(metaFile.toString(), RequestMetadata.class);
			String sha = DesktopUtil.getChecksum(urlPath.toFile(), Headers.SHA1.getValue());
			if (Objects.nonNull(localMetadata) && Objects.equals(localMetadata.getSha1(), sha)) {
				log.trace("use local file -> " + url);
				localMetadata.setLastAccessAt(System.currentTimeMillis());
				fileMapperService.write(localMetadata, metaFile.toString());
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

	private Path getResourceWithHttpHead(String url, Path metaFile, Path urlPath)
			throws IOException {
		boolean fileExists = urlPath.toFile().exists();
		try {
			if (fileExists) {
				RequestMetadata serverMetadata = httpService.getMetaByUrl(url);
				RequestMetadata localMetadata = fileMapperService.read(metaFile.toString(), RequestMetadata.class);
				log.info("do head request -> {} {} local file {}", url, localMetadata, urlPath);
				if (Objects.nonNull(localMetadata) && Objects.nonNull(localMetadata.getETag())
						&& StringUtils.equals(serverMetadata.getETag(), localMetadata.getETag())
						&& StringUtils.equals(serverMetadata.getLastModified(), localMetadata.getLastModified())
						&& StringUtils.equals(DesktopUtil.getChecksum(urlPath.toFile(), "SHA-1"),
								localMetadata.getSha1())) {
					localMetadata.setLastAccessAt(System.currentTimeMillis());
					fileMapperService.write(localMetadata, metaFile.toString());
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
			throws IOException {
		RequestMetadata requestMetadata = httpService.getRequestByUrlAndSave(url, urlPath);
		requestMetadata.setSha1(DesktopUtil.getChecksum(urlPath.toFile(), "SHA-1"));
		requestMetadata.setLastAccessAt(System.currentTimeMillis());
		fileMapperService.write(requestMetadata, metaFile.toString());
		return urlPath;
	}

	protected Path buildPath(String url) {
		Path urlPath = Paths.get(directory.toString(), url.replaceAll("://", "_").replaceAll("[:?=]", "_"));
		return urlPath;
	}

	@Override
	public void cleanOldCache() {

		long now = System.currentTimeMillis();
		long ttlMillis = timeToLife * 1000L;

        try (Stream<Path> stream = Files.walk(directory)) {

			stream.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().endsWith(".metadata"))
					.filter(path -> {

						RequestMetadata metadata = fileMapperService.read(path.toString(), RequestMetadata.class);
						if (metadata == null) {
							return false;
						}
						long lastAccessAt = metadata.getLastAccessAt();

                        return now - lastAccessAt > ttlMillis;

                    }).toList().forEach(path -> {
						Path dataFile = Paths.get(path.toString().replace(".metadata", ""));
						try {
							Files.deleteIfExists(path);
							Files.deleteIfExists(dataFile);
							log.info("delete old file {}", path);
						} catch (IOException e) {
							log.error("Failed to delete old file {} error: {}", path, e.getMessage());
						}

					});

		} catch (IOException e) {
            log.error("Failed to walk cache directory {}: {}", directory, e.getMessage());
        }
    }

}