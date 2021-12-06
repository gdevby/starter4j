package by.gdev.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	
	//TODO new function
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
//                LogManager.shutdown();
                System.exit(4);
            }
        }
    }

	public void diactivateDoublePreparingJVM() throws IOException {
        if (Objects.nonNull(lock))
            lock.release();
    }
	
    public static String convertListToString(String c, List<Path> l) {
        StringBuilder b = new StringBuilder();
        for (Path string : l) {
            b.append(string).append(c);
        }
        return b.toString();
    }
    
    
    
    
    
    
	public static Path getAbsolutePathToJava(OSInfo.OSType type, String path) {
		StringBuilder b = new StringBuilder();
		//for Linux x64
		if (type == OSInfo.OSType.LINUX && OSInfo.getJavaBit() ==  OSInfo.Arch.x64) 
			b.append("jre_default").append(File.separatorChar).append("jre-8u281-linux-x64").append(File.separatorChar);
		//for Linux x32
		if (type == OSInfo.OSType.LINUX && OSInfo.getJavaBit() ==  OSInfo.Arch.x32) 
			b.append("jre_default").append(File.separatorChar).append("jre-8u281-linux-i586").append(File.separatorChar);
		//for Windows x64
		if (type == OSInfo.OSType.WINDOWS && OSInfo.getJavaBit() ==  OSInfo.Arch.x64) 
			b.append("jre_default").append(File.separatorChar).append("jre-8u281-windows-x64").append(File.separatorChar);
		//for Windows x32
		if (type == OSInfo.OSType.WINDOWS && OSInfo.getJavaBit() ==  OSInfo.Arch.x32) 
			b.append("jre_default").append(File.separatorChar).append("jre-8u111-windows-i586").append(File.separatorChar);
		//for MACOSX x32
		if (type == OSInfo.OSType.MACOSX  && !path.toLowerCase().endsWith("jre") && !path.toLowerCase().endsWith("home"))
			b.append("jre_default").append(File.separatorChar).append("jdk1.8.0_202.jdk").append(File.separatorChar)
			.append("Contents").append(File.separatorChar).append("Home").append(File.separatorChar).append("jre").append(File.separatorChar);
		
		return Paths.get(appendToJVM(String.valueOf(new File(path, b.toString())))).toAbsolutePath();
	}
}