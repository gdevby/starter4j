package by.gdev.http.download.config;



import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;


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
		RequestConfig config = RequestConfig.custom().setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
				.setResponseTimeout(Timeout.ofMilliseconds(socketTimeout))
				.build();
		CloseableHttpClient builder = HttpClients.custom()
				.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE).setConnectionManager(cm)
				.setDefaultRequestConfig(config).evictIdleConnections(Timeout.ofSeconds(10)).disableContentCompression()
				.build();
		return builder;
	}

	public static CloseableHttpClient getInstanceHttpClient() {
		return getInstanceHttpClient(5 * 1000, 10 * 1000, 5, 20);
	}
}