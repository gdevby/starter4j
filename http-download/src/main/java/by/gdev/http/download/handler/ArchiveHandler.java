package by.gdev.http.download.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import by.gdev.http.upload.download.downloader.DownloadElement;
import by.gdev.http.upload.download.downloader.DownloaderJavaContainer;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;
import by.gdev.util.OSInfo.OSType;
import by.gdev.util.model.download.JvmRepo;
import by.gdev.util.model.download.Metadata;
import by.gdev.util.model.download.Repo;
import by.gdev.utils.service.FileMapperService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
public class ArchiveHandler implements PostHandler {

	private FileMapperService fileMapperService;
	private String jreConfig;

	@Override
	public void postProcessDownloadElement(DownloadElement e) throws IOException, NoSuchAlgorithmException {
		Path p = Paths.get(e.getPathToDownload(), e.getMetadata().getPath());
		String jrePath = Paths.get(DownloaderJavaContainer.JRE_DEFAULT, ((JvmRepo) e.getRepo()).getJreDirectoryName())
				.toString();
		if (String.valueOf(p).endsWith(".zip"))
			unZip(p.toFile(), new File(e.getPathToDownload()), false, false);
		else
			unTarGz(p.toFile(), new File(e.getPathToDownload()), false, false);
		if (OSInfo.getOSType() == OSType.LINUX | OSInfo.getOSType() == OSType.MACOSX) {
			Files.walk(Paths.get(e.getPathToDownload(), jrePath))
					.filter(f -> Files.isRegularFile(f) && (f.endsWith("java") || f.endsWith("java.exe")
							|| f.endsWith("jspawnhelper") || f.endsWith("jspawnhelper.exe")))
					.forEach(file -> {
						try {
							Files.setPosixFilePermissions(file, DesktopUtil.PERMISSIONS);
						} catch (IOException e1) {
							log.error("Error with set file permissions ", e1);
						}
					});
		}
		generateJreConfig(e.getPathToDownload(), jrePath);
		Files.delete(p);

	}

	private void createFile(File file) throws IOException {
		if (file.isFile())
			return;
		if (file.getParentFile() != null)
			file.getParentFile().mkdirs();
		if (!file.createNewFile())
			throw new IOException(
					"Cannot createScrollWrapper file, or it was created during runtime: " + file.getAbsolutePath());
	}

	private void unTarGz(File zip, File folder, boolean replace, boolean deleteEmptyFile)
			throws IOException, NoSuchAlgorithmException {
		try (TarArchiveInputStream zis = new TarArchiveInputStream(
				new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(zip))))) {
			TarArchiveEntry ze;
			while ((ze = (TarArchiveEntry) zis.getNextEntry()) != null) {
				String fileName = Paths.get(DownloaderJavaContainer.JRE_DEFAULT, ze.getName()).toString();
				if (ze.isDirectory())
					continue;
				unZipAndTarGz(fileName, folder, replace, zis, deleteEmptyFile);
			}
			zis.close();
		}
	}

	private void unZip(File zip, File folder, boolean replace, boolean deleteEmptyFile)
			throws IOException, NoSuchAlgorithmException {
		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)),
				StandardCharsets.UTF_8)) {
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				String fileName = Paths.get(DownloaderJavaContainer.JRE_DEFAULT, ze.getName()).toString();
				if (ze.isDirectory())
					continue;
				unZipAndTarGz(fileName, folder, replace, zis, deleteEmptyFile);
			}
			zis.closeEntry();
		}
	}

	private void unZipAndTarGz(String fileName, File folder, boolean replace, InputStream zis, boolean deleteEmptyFile)
			throws IOException, NoSuchAlgorithmException {
		byte[] buffer = new byte[1024];
		File newFile = new File(folder, fileName);
		if (!replace && newFile.isFile()) {
			return;
		}
		createFile(newFile);
		OutputStream fos = new BufferedOutputStream(new FileOutputStream(newFile));
		int len;
		int count = 0;
		while ((len = zis.read(buffer)) > 0) {
			count += len;
			fos.write(buffer, 0, len);
		}
		fos.close();
		if (deleteEmptyFile && count == 0) {
			Files.delete(newFile.toPath());
		}
	}

	private void generateJreConfig(String path, String jrePath) {
		try {
			List<Metadata> list = DesktopUtil.generateMetadataForJre(path, jrePath);
			Repo r = new Repo();
			r.setResources(list);
			fileMapperService.write(r, Paths.get(jrePath, jreConfig).toString());
		} catch (Exception e) {
			log.error("error {}", e);
		}
	}
}