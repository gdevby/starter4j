package by.gdev.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;

public class ValidatedPartionSize extends AbstractBandle {

	private	File file = new File(new File("").getAbsolutePath());		
//	private	long size = new StarterAppConfig().getMinMemorySize();
//	private	long size = 144350011393L;
	private long minMemorySize;

	public ValidatedPartionSize(long minMemorySize) {
		this.minMemorySize = minMemorySize;
	}

	@Override
	public boolean validate() {
		try {
			FileStore store = Files.getFileStore(file.toPath().getRoot());
			long res = store.getUsableSpace();
			return res > minMemorySize;
		} catch (IOException e) {}
		return true;
	}

	@Override
	public String getExceptionMessage() {
		return String.format(bundle.getString("validate.size"), file , file.getFreeSpace() / 1024 / 1024, file);
		
		
	}
}