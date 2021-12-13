package by.gdev.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import by.gdev.util.OSInfo.OSType;
import by.gdev.util.model.download.Metadata;
import by.gdev.util.model.download.Repo;
import lombok.extern.slf4j.Slf4j;

/**
 * Class utilities. Chagned mode fo the file for linux and mac {@link DesktopUtil#PERMISSIONS}
 * 
 * or merge and https://github.com/wille/oslib
 */

@Slf4j
public class DesktopUtil {
	 private static final String PROTECTION = "protection.txt";
	 private FileLock lock;
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
	 * Defined default working directory + path.
	 * Examples for WINDOWS C:\\Users\\user\\AppData\\Roaming\\MyAppName 
	 * LINUX /home/user/MyAppName
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

	public static void sleep(int seconds) {
		try {
			Thread.sleep(1000 * seconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public static int numberOfAttempts(List <String>urls, int maxAttepmts, RequestConfig requestConfig, CloseableHttpClient httpclient) {
		int attempt = 1;
		
		for (String url : urls) {
			try {
				HttpGet http = new HttpGet(url);
				http.setConfig(requestConfig);
				httpclient.execute(http);
				return  maxAttepmts;
				
			} catch (IOException e) {}
		}
		return attempt;
	}
	
    private static void createDirectory(File file) throws IOException {
        if (file.isFile())
            return;
        if (file.getParentFile() != null)
            file.getParentFile().mkdirs();
    }	
	
	public void activeDoubleDownloadingResourcesLock(String container) throws IOException {
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
	
	public void diactivateDoubleDownloadingResourcesLock() throws IOException {
        if (Objects.nonNull(lock))
            lock.release();
    }
	
	/**
	 * Converts an array to a string separating each element with the specified delimiter
	 * @param del separator
	 * @param list an array whose elements need to be converted to a single string
	 * @return A string of the list array, each element of which is delimited by the specified delimiter
	 */
    public static String convertListToString(String del, List<Path> list) {
        StringBuilder b = new StringBuilder();
        for (Path string : list) {
            b.append(string).append(del);
        }
        return b.toString();
    }

    /**
     * Allows to get the path to the executable file
     * @param java 
     * @return
     */
	public static String getJavaRun(Repo java) {
		String javaRun = null;
		 for (Metadata s : java.getResources()) {
			 if (s.isExecutable() && s.getPath().endsWith(appendBootstrapperJvm2(s.getPath())))
				 javaRun = s.getPath();
		 }
		return javaRun;
	}
	
	
	private static String appendBootstrapperJvm2(String path) {
		StringBuilder b = new StringBuilder();
		if (OSInfo.getOSType() == OSInfo.OSType.MACOSX) {
			b.append("Contents").append(File.separatorChar).append("Home").append(File.separatorChar);
		}
		return appendToJVM(new File(b.toString()).getPath());
	}
}