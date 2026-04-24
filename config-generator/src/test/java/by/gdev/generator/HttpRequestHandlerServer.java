package by.gdev.generator;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.io.HttpRequestHandler;

import by.gdev.generator.model.AppConfigModel;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HttpRequestHandlerServer implements HttpRequestHandler{
	AppConfigModel acm;

	@Override
	public void handle(ClassicHttpRequest classicHttpRequest, ClassicHttpResponse classicHttpResponse, HttpContext httpContext) throws HttpException, IOException {
        String decodeValue = URLDecoder.decode(classicHttpRequest.getPath(), StandardCharsets.UTF_8.name());
        String defaultPath = "target/out/" + decodeValue.substring(1);
		if (decodeValue.substring(1).startsWith(acm.getAppName()+ "/jres_configuration_default"))
			defaultPath = decodeValue.replaceAll(acm.getAppName(), "").substring(2);
		byte[] array = Files.readAllBytes(Paths.get(defaultPath));
		classicHttpResponse.setCode(HttpStatus.SC_OK);
		classicHttpResponse.setEntity(new ByteArrayEntity(array, null));
	}
}
