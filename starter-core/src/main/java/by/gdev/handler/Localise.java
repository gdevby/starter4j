package by.gdev.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Localise {

	static Locale locale;

	public Locale getLocal() {
		try {
			Properties property = new Properties();
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			InputStream inputStream = classloader.getResourceAsStream("application.properties");
			property.load(inputStream);
			String language = property.getProperty("language");
			String[] parts = language.split(",");
			if (ArrayUtils.contains(parts, Locale.getDefault().getLanguage())) {
				if (Locale.getDefault().getLanguage().contains("_")) {
					String[] ar = Locale.getDefault().getLanguage().split("_");
					locale = new Locale(ar[0], ar[1]);
				} else
					locale = new Locale.Builder().setLanguage(Locale.getDefault().getLanguage()).build();
			} else
				locale = new Locale.Builder().setLanguage("en").build();
		} catch (IOException e) {
			log.error("Error", e);
		}
		return locale;
	}
}