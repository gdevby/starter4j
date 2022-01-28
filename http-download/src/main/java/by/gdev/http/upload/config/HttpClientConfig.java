package by.gdev.http.upload.config;

import java.util.concurrent.TimeUnit;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
/**
 * TODO
 * @author Robert Makrytski
 *
 */
public class HttpClientConfig {

	public CloseableHttpClient getInstanceHttpClient() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setDefaultMaxPerRoute(5);
		cm.setMaxTotal(20);
		CloseableHttpClient builder = HttpClients.custom()
				.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE).setConnectionManager(cm)
				.evictIdleConnections(10, TimeUnit.SECONDS).build();
		return builder;
	}
}