package by.gdev.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;

import by.gdev.util.OSInfo.OSType;

/**
 * add
 * https://github.com/wille/startuplib/blob/master/src/startuplib/WindowsStartup.java
 * or merge and https://github.com/wille/oslib
 */
//TODO describe every method of the util
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
	
	
//    public static File getSystemRelatedDirectory(OSInfo.OSType type, String path) {
//    	if (!type.equals(OSInfo.OSType.MACOSX)|| !type.equals(OSInfo.OSType.UNKNOWN)) {
//            path = '.' + path;
//    	}
//        return getSystemPath(type, path);
//    }
	
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

	public static int init(int maxAttepmts, RequestConfig requestConfig, CloseableHttpClient httpclient) {
		try {
			HttpHead http = new HttpHead("http://www.google.com");
			http.setConfig(requestConfig);
			httpclient.execute(http);
			return maxAttepmts;
		} catch (IOException e) {
			return 1;
		}
	}
	
    private static void createFile(File file) throws IOException {
        if (file.isFile())
            return;
        if (file.getParentFile() != null)
            file.getParentFile().mkdirs();
    }	
	
	public void activeDoublePreparingJVM(String container) throws IOException {
        File f = new File(container, PROTECTION);
        createFile(f);
        if (f.exists()) {
            FileChannel ch = FileChannel.open(f.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            lock = ch.tryLock();
            if (Objects.isNull(lock)) {
            	//TODO отключение ведение журнала sl4g?
//                LogManager.shutdown();
                System.exit(4);
            }
        }
    }

    private void diactivateDoublePreparingJVM() throws IOException {
        if (Objects.nonNull(lock))
            lock.release();
    }
}