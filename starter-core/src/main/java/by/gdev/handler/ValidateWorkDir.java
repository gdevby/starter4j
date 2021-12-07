package by.gdev.handler;

import java.io.File;
import java.nio.file.Files;

import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;

public class ValidateWorkDir extends ValisatedEnviromentAbstract {
	//TODO add dir from user configuration from user file which located stater/...properties transfer argument like param
	@Override
	public boolean validate() {
		File workDir = DesktopUtil.getSystemPath(OSInfo.getOSType(), "starter");
		if (workDir.exists()) {
			if (!Files.isWritable(workDir.toPath()) || !Files.isReadable(workDir.toPath()))
				return false;
		}
		return true;
	}

	@Override
	public String getExceptionMessage() {
		return localizationBandle.getString("validate.workdir");
	}
}