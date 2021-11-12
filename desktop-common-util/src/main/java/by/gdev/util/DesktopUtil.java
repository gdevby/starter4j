package by.gdev.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.io.FileUtils;

import by.gdev.util.OSInfo.OSType;

/**
 * add
 * https://github.com/wille/startuplib/blob/master/src/startuplib/WindowsStartup.java
 * or merge and https://github.com/wille/oslib
 */
public class DesktopUtil {
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

	public void extractArchive(ArchiveInputStream in, String dir) throws IOException {
		try {
			String temp_dir = dir + "_temp";
			if (Files.exists(Paths.get(temp_dir))) {
				FileUtils.deleteDirectory(new File(temp_dir));
				FileUtils.deleteDirectory(new File(dir));
			}
			int BUFFER_SIZE = 8092;
			try (ArchiveInputStream tarIn = in) {
				ArchiveEntry entry;

				while ((entry = tarIn.getNextEntry()) != null) {
					// If the entry is a directory, create the directory.
					if (entry.isDirectory()) {
						Path p = Paths.get(".", temp_dir, entry.getName());
						Files.createDirectories(p);
						if (!Files.exists(p)) {
							System.err.println("Unable to create directory, during extraction of archive contents. "
									+ p.toAbsolutePath());
						}
					} else {
						int count;
						byte[] data = new byte[BUFFER_SIZE];
						Path entryPath = Paths.get(".", temp_dir, entry.getName());
						Files.createDirectories(entryPath.getParent());
						FileOutputStream fos = new FileOutputStream(entryPath.toFile(), false);
						try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
							while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
								dest.write(data, 0, count);
							}
						}
					}
				}
			}
			Files.move(Paths.get(temp_dir), Paths.get(dir));
		} catch (IOException e) {
			FileUtils.deleteDirectory(new File(dir));
			throw e;
		}
	}

	public static <T> T uncheckCall(Callable<T> callable) {
		try {
			return callable.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String getChecksum(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
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
	
	public static <T, R> Function<T, R> wrap(CheckedFunction<T, R> checkedFunction) {
		return t -> {
			try {
				return checkedFunction.apply(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static void sleep(int seconds) {
		try {
			Thread.sleep(1000 * seconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}