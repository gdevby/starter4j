package by.gdev.generator;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import by.gdev.generator.model.AppConfigModel;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HttpRequestHandlerServer implements HttpRequestHandler{
	AppConfigModel acm;
	
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
		String decodeValue = URLDecoder.decode(request.getRequestLine().getUri(), StandardCharsets.UTF_8.name());
		String defaultPath = "target/out/" + decodeValue.substring(1);
		if (decodeValue.substring(1).startsWith(acm.getAppName()+ "/jres_configuration_default")) 			
			defaultPath = decodeValue.replaceAll(acm.getAppName(), "").substring(2);
		byte[] array = Files.readAllBytes(Paths.get(defaultPath));
         response.setStatusCode(HttpStatus.SC_OK);
         response.setEntity(new ByteArrayEntity(array));
	}
}
