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
import by.gdev.util.OSInfo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HttpRequestHandlerServer implements HttpRequestHandler{
	AppConfigModel acm;
	
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
		String decodeValue = URLDecoder.decode(request.getRequestLine().getUri(), StandardCharsets.UTF_8.name());
		String defaultPath = "target/out/" + decodeValue.substring(1);
		if (decodeValue.substring(1).startsWith("jvms")) 
			defaultPath = "" + decodeValue.substring(1);
		
		if (decodeValue.substring(1).startsWith(acm.getAppName()+"/jre_default/"))
			defaultPath = decodeValue.replaceAll(acm.getAppName(), Paths.get(acm.getJavaFolder(), String.valueOf(OSInfo.getOSType()).toLowerCase(), String.valueOf(OSInfo.getJavaBit())).toString()).substring(1);
			
		byte[] array = Files.readAllBytes(Paths.get(defaultPath));
         response.setStatusCode(HttpStatus.SC_OK);
         response.setEntity(new ByteArrayEntity(array));
	}
}
