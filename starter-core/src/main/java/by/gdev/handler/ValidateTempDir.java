package by.gdev.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.openide.filesystems.FileUtil;

public class ValidateTempDir implements ValidateEnvironment {

	@Override
	public boolean valite() {
		Path folder = Paths.get(System.getProperty("java.io.tmpdir"));
		try {
			if (Files.isRegularFile(folder))
				Files.delete(folder);
			if (!Files.exists(folder))
				FileUtil.createFolder(folder.toFile());
		} catch (Exception e) {
		}
		return true;
	}

	@Override
	public String getExceptionMessage() {
		return "Переменные темп установлены неправильно, вам надо самим установить их. "
				+ "\nВ Пуск введите 'переменные среды' и откройте это окно, дальше вы найдите в этом окне TEMP, TMP. "
				+ "\nТеперь вам необходимо ввести реальные папки для этих переменных, "
				+ "\nНапример C:\\\\TEMP , C:\\\\TMP. " + "\nА так же проверить созданы ли эти папки.";
	}
}
