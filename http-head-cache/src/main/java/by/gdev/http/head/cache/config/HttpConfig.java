package by.gdev.http.head.cache.config;

import java.util.concurrent.TimeUnit;

import org.apache.http.HeaderElementIterator;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class HttpConfig {
	//TODO check how it works
	//В результате мы должны убедиться что это работает и не создает новые соединения 
	public CloseableHttpClient httpClient() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setDefaultMaxPerRoute(1);
		cm.setMaxTotal(20);
		CloseableHttpClient builder = HttpClients.custom().setKeepAliveStrategy((response, context) -> {
			Args.notNull(response, "HTTP response");
			final HeaderElementIterator it = new BasicHeaderElementIterator(
					response.headerIterator(HTTP.CONN_KEEP_ALIVE));
			if (it.hasNext()) {
				log.info("used keep alive 5000");
				return 5000L;
			}
			return -1;
		}).setConnectionManager(cm).evictIdleConnections(10, TimeUnit.SECONDS).build();
		return builder;
	}
}