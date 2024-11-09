package util;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Util {
	public static IOException throwException(List<String> urls, String urn, IOException ex) throws IOException {
		if (Objects.isNull(ex))
			return new IOException(String.format("%s %s %s", "can't find proper resource", urls.toString(), urn));
		else
			return ex;
	}
}
