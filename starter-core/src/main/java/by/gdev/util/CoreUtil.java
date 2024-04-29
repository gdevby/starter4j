package by.gdev.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CoreUtil {

	@SuppressWarnings("rawtypes")
	public static Pair<String, byte[]> readFileLog() throws IOException {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		FileAppender appender = (FileAppender) lc.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("FILE_APPENDER");
		log.info("path {}", appender.getFile());
		return Pair.of(appender.getFile(), Files.readAllBytes(Paths.get(appender.getFile())));
	}
}
