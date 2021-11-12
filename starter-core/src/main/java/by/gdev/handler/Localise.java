package by.gdev.handler;

import java.io.FileInputStream;
import java.util.Locale;
import java.util.Properties;
//TODO for what?
public class Localise {
	
	static Locale locale;
	
	public Locale getLocal(){
		try {
			Properties property = new Properties();
			//TODO we use file, it doesn't work with java elem, use classpath like in example 
			//ClassLoader classloader = Thread.currentThread().getContextClassLoader();
	        //InputStream inputStream = classloader.getResourceAsStream("settings.json");
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