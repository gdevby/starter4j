package by.gdev.handler;

import java.io.File;
import java.nio.file.Files;

import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;

public class ValidateWorkDir implements ValidateEnvironment {

	@Override
	public boolean valite() {
		File workDir = DesktopUtil.getSystemPath(OSInfo.getOSType(), "starter");
//		File workDir = new File("/home/aleksandr/Desktop/qwert/acces");
		if (workDir.exists()) {
			if (!Files.isWritable(workDir.toPath()) || !Files.isReadable(workDir.toPath()))
				return false;
		}
		return true;
	}

	@Override
	public String getExceptionMessage() {
		return "Нет прав доступа, установите корректные права доступа ";
	}
}