package by.gdev.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


import com.google.common.eventbus.Subscribe;

import by.gdev.model.AppConfig;
import by.gdev.model.StarterAppConfig;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;
import lombok.Data;

@Data
public class Validate implements ValidateEnvironment {

	private	File file = new File(new File("").getAbsolutePath());		
	private	long size = new StarterAppConfig().getMinMemorySize();
//	private	long size = 144350011393L;

	@Override
	public boolean valite() {
		try {
			FileStore store = Files.getFileStore(file.toPath().getRoot());
			long res = store.getUsableSpace();
			return res > size;
		} catch (IOException e) {}
		return true;
	}

	@Override
	public String getExceptionMessage() {
		return "Для корректной работы приложения рекомендуется освободить место на жестком диске " + file
				+ " после освобождения перезапустите лаунчер снова \nСвободно места на жестком дике: "
				+ file.getFreeSpace() / 1024 / 1024 + "mb \nНеобходимо освободить: " + size / 1024 / 1024 + "mb";
	}
	

	
}