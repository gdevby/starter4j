package by.gdev.generator;

import java.io.IOException;
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
		String defaultPath = "target/out/" + request.getRequestLine().getUri().substring(1);
		if (request.getRequestLine().getUri().substring(1).startsWith("jvms")) {
			defaultPath = "" + request.getRequestLine().getUri().substring(1);
		}
		
		if (request.getRequestLine().getUri().substring(1).startsWith(acm.getAppName()+"/jre_default/")) {
			defaultPath = request.getRequestLine().getUri().replaceAll(acm.getAppName(), Paths.get(acm.getJavaFolder(), 
					String.valueOf(OSInfo.getOSType()).toLowerCase(), String.valueOf(OSInfo.getJavaBit())).toString()).substring(1);
		}
		
		byte[] array = Files.readAllBytes(Paths.get(defaultPath));
         response.setStatusCode(HttpStatus.SC_OK);
         response.setEntity(new ByteArrayEntity(array));
	}
}
