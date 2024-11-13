package by.gdev.util;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JFileChooser;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;

import by.gdev.util.OSInfo.OSType;
import by.gdev.util.model.download.Metadata;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Class utilities. Chagned mode fo the file for linux and mac
 * {@link DesktopUtil#PERMISSIONS}
 * 
 * or merge and https://github.com/wille/oslib
 */

@Slf4j
public class DesktopUtil {
	private static final String PROTECTION = "protection.txt";
	private static FileLock lock;
	@SuppressWarnings("serial")
	public static Set<PosixFilePermission> PERMISSIONS = new HashSet<PosixFilePermission>() {
		{
			add(PosixFilePermission.OWNER_READ);
			add(PosixFilePermission.OWNER_WRITE);
			add(PosixFilePermission.OWNER_EXECUTE);

			add(PosixFilePermission.OTHERS_READ);
			add(PosixFilePermission.OTHERS_WRITE);
			add(PosixFilePermission.OTHERS_EXECUTE);

			add(PosixFilePermission.GROUP_READ);
			add(PosixFilePermission.GROUP_WRITE);
			add(PosixFilePermission.GROUP_EXECUTE);
		}
	};

	/**
	 * Defined default working directory + path. Examples for WINDOWS
	 * C:\\Users\\user\\AppData\\Roaming\\MyAppName LINUX /home/user/MyAppName
	 * 
	 * @param type {@link OSType}
	 * @param path is MyAppName in description
	 * @return
	 */
	public static File getSystemPath(OSInfo.OSType type, String path) {
		String userHome = System.getProperty("user.home", ".");
		File file;
		switch (type) {
		case LINUX:
		case SOLARIS:
			file = new File(userHome, path);
			break;
		case WINDOWS:
			String applicationData = System.getenv("APPDATA");
			String folder = applicationData != null ? applicationData : userHome;
			file = new File(folder, path);
			break;
		case MACOSX:
			file = new File(userHome, "Library/Application Support/" + path);
			break;
		default:
			file = new File(userHome, path);
		}
		return file;
	}

	public static String getChecksum(byte[] array, String algorithm) throws IOException, NoSuchAlgorithmException {
		return createChecksum(array, algorithm);
	}

	public static String getChecksum(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
		return createChecksum(Files.readAllBytes(file.toPath()), algorithm);
	}

	private static String createChecksum(byte[] array, String algorithm) throws NoSuchAlgorithmException {
		MessageDigest complete = MessageDigest.getInstance(algorithm);
		complete.update(array);
		byte[] b = complete.digest();
		StringBuilder result = new StringBuilder();
		for (byte cb : b)
			result.append(Integer.toString((cb & 0xff) + 0x100, 16).substring(1));
		return result.toString();
	}

	public static String getJavaPathByHome(boolean appendBinFolder) {
		String path = System.getProperty("java.home");
		if (appendBinFolder) {
			path = appendToJVM(path);
		}
		return path;
	}

	public static String appendToJVM(String path) {
		char separator = File.separatorChar;
		StringBuilder b = new StringBuilder(path);
		b.append(separator);
		b.append("bin").append(separator).append("java");
		if (OSInfo.getOSType().equals(OSType.WINDOWS))
			b.append("w.exe");
		return b.toString();
	}

	/**
	 * Used to call without checked exception.
	 * 
	 * @param <T>
	 * @param callable
	 * @return
	 */
	public static <T> T uncheckCall(Callable<T> callable) {
		try {
			return callable.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> void uncheckCallVoid(Callable<T> callable) {
		try {
			callable.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc CheckedFunction}
	 */
	public static <T, R> Function<T, R> wrap(CheckedFunction<T, R> checkedFunction) {
		return t -> {
			try {
				return checkedFunction.apply(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static void sleep(int milliSeconds) {
		try {
			Thread.sleep(milliSeconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@SneakyThrows
	public static InternetServerMap testServers(List<String> urls, CloseableHttpClient httpclient) {
		InternetServerMap ism = new InternetServerMap();
		ForkJoinPool fjp = new ForkJoinPool(10);
		fjp.submit(() -> {
			ism.putAll(urls.stream().parallel().map(link -> {
				int time = 2000;
				IOException e = null;
				String host = "";
				for (int i = 0; i < 2; i++) {
					try {
						HttpHead http = new HttpHead(link);
						host = http.getURI().getHost();
						http.setConfig(RequestConfig.custom().setConnectTimeout(time).setSocketTimeout(time).build());
						log.info("check internet connection {} timeout {} ms", link, time);
						httpclient.execute(http);
						return new AbstractMap.SimpleEntry<>(host, Boolean.TRUE);
					} catch (IOException e1) {
						e = e1;
					}
					DesktopUtil.sleep(1000);
					time *= 3;
				}
				log.info("error during test net {} {}", link, e.getMessage());
				return new AbstractMap.SimpleEntry<>(host, Boolean.FALSE);
			}).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)));
		}).get();
		fjp.shutdown();
		return ism;
	}

	private static void createDirectory(File file) throws IOException {
		if (file.isFile())
			return;
		if (file.getParentFile() != null)
			file.getParentFile().mkdirs();
	}

	public static void diactivateDoubleDownloadingResourcesLock() throws IOException {
		if (Objects.nonNull(lock))
			lock.release();
	}

	/**
	 * Converts an array to a string separating each element with the specified
	 * delimiter
	 * 
	 * @param del  separator
	 * @param list an array whose elements need to be converted to a single string
	 * @return A string of the list array, each element of which is delimited by the
	 *         specified delimiter
	 */
	public static String convertListToString(String del, List<Path> list) {
		StringBuilder b = new StringBuilder();
		for (Path string : list) {
			b.append(string).append(del);
		}
		return b.toString();
	}

	public static void activeDoubleDownloadingResourcesLock(String container) throws IOException {
		File f = new File(container, PROTECTION);
		createDirectory(f);
		if (f.exists()) {
			FileChannel ch = FileChannel.open(f.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
			lock = ch.tryLock();
			if (Objects.isNull(lock)) {
				log.warn("Lock could not be acquired ");
				System.exit(4);
			}
		}
	}

	/**
	 * Allows to get the path to the executable file
	 * 
	 * @param java
	 * @return
	 */

	public static Path getJavaRun(Path java) throws IOException {
		return Files.walk(java).filter(e -> Files.isRegularFile(e) && (e.endsWith("java") || e.endsWith("java.exe")))
				.findAny().orElseThrow(() -> new RuntimeException("java executable not found "));
	}

	public static String appendBootstrapperJvm2(String path) {
		StringBuilder b = new StringBuilder();
		if (OSInfo.getOSType() == OSInfo.OSType.MACOSX) {
			b.append("Contents").append(File.separatorChar).append("Home").append(File.separatorChar);
		}
		return appendToJVM(new File(b.toString()).getPath());
	}

	/**
	 * For JavaFX used Application.getHostServices().showDocument
	 * 
	 * @param type
	 * @param uri
	 * @param alertError
	 */
	public static void openLink(OSType type, String uri) {
		// TOD there is some problem with swing app. is is hanging in swing thread.
		CompletableFuture.runAsync(() -> {
			try {
				Desktop.getDesktop().browse(new URI(uri));
			} catch (IOException | URISyntaxException e) {
				log.warn("can't open link", e);
				if (type.equals(OSType.LINUX))
					try {
						Runtime.getRuntime().exec("gnome-open " + uri);
					} catch (IOException e1) {
						log.warn("can't open link for linix", e);
					}
			}
		});
	}

	/**
	 * Some windows has problem with JFileChooser when you use look and feel
	 */
	public static void initLookAndFeel() {
		LookAndFeel defaultLookAndFeel = null;
		try {
			defaultLookAndFeel = UIManager.getLookAndFeel();
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			new JFileChooser();
		} catch (Throwable t) {
			log.warn("problem with ", t);
			if (Objects.nonNull(defaultLookAndFeel))
				try {
					UIManager.setLookAndFeel(defaultLookAndFeel);
				} catch (Throwable e) {
					log.warn("coudn't set defualt look and feel", e);
				}
		}
	}

	public static List<String> generatePath(List<String> repositories, List<Metadata> resources) {
		return repositories.stream().map(repo -> {
			return resources.stream().map(res -> String.format("%s/%s", repo, res.getRelativeUrl()))
					.collect(Collectors.toList());
		}).flatMap(List::stream).collect(Collectors.toList());
	}

	public static List<Metadata> generateMetadataForJre(String path, String jrePath) throws IOException {
		return Files.walk(Paths.get(path, jrePath)).filter(Files::isRegularFile)
				.filter(pr -> !(pr.getFileName().toString().endsWith(".json"))).map(DesktopUtil.wrap(p -> {
					Metadata m = new Metadata();
					m.setSha1(DesktopUtil.getChecksum(p.toFile(), "SHA-1"));
					String relativize = Paths.get(path).relativize(p).toString();
					m.setPath(relativize.replace("\\", "/"));
					m.setSize(p.toFile().length());
					return m;
				})).collect(Collectors.toList());
	}

	@SneakyThrows
	public static String getRootFolderZip(File zip) {
		if (zip.getName().endsWith(".tar.gz")) {
			try (TarArchiveInputStream zis = new TarArchiveInputStream(
					new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(zip))))) {
				TarArchiveEntry ze = zis.getNextTarEntry();
				return ze.getName();
			}
		} else if (zip.getName().endsWith(".zip")) {
			try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)),
					StandardCharsets.UTF_8)) {
				ZipEntry ze = zis.getNextEntry();
				return ze.getName();
			}
		} else {
			return "";
		}
	}

	public static String getTime(Class<?> cl) {
		try {
			String rn = cl.getName().replace('.', '/') + ".class";
			JarURLConnection j = (JarURLConnection) cl.getClassLoader().getResource(rn).openConnection();
			return new Date(j.getJarFile().getEntry("META-INF/MANIFEST.MF").getTime()).toString();
		} catch (Exception e) {
			return "dev";
		}
	}
}