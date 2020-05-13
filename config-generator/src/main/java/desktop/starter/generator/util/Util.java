package desktop.starter.generator.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

public class Util {

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

    public static <T, R> Function<T, R> wrap(CheckedFunction<T, R> checkedFunction) {
        return t -> {
            try {
                return checkedFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
