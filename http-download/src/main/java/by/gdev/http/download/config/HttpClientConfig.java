package by.gdev.http.download.config;

import java.util.concurrent.TimeUnit;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * This class allows you to set the maximum number of total open connections,
 * the maximum number of concurrent connections per route and connection
 * Keep-Alive strategy.
 * 
 * @author Robert Makrytski
 *
 */
public class HttpClientConfig {
	public static CloseableHttpClient getInstanceHttpClient(int connectTimeout, int socketTimeout, int maxPerRoute,
			int maxTotalPool) {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setDefaultMaxPerRoute(maxPerRoute);
		cm.setMaxTotal(maxTotalPool);
		RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout)
				.build();
		CloseableHttpClient builder = HttpClients.custom()
				.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE).setConnectionManager(cm)
				.setDefaultRequestConfig(config).evictIdleConnections(10, TimeUnit.SECONDS).disableContentCompression().build();
		return builder;
	}

	public static CloseableHttpClient getInstanceHttpClient() {
		return getInstanceHttpClient(60 * 1000, 60 * 1000, 5, 20);
	}
}