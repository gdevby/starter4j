package by.gdev.handler;

import java.io.FileInputStream;
import java.util.Locale;
import java.util.Properties;

public class Localise {
	
	static Locale locale;
	
	public Locale getLocal(){
		try {
			Properties property = new Properties();
			FileInputStream fis = new FileInputStream("src/main/resources/application.properties");
			property.load(fis);
			String language = property.getProperty("language");
			String[] parts = language.split(",");
			for (String string : parts) {
				// Поверяет есть ли русский язык
				if (!string.equals(Locale.getDefault().toString())) {
					return locale = new Locale.Builder().setLanguage("ru").build();
				}else {
					return locale = new Locale.Builder().setLanguage("en").build();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return locale;
	}	
}