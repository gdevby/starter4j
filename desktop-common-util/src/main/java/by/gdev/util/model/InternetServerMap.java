package by.gdev.util.model;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.http.protocol.HttpService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InternetServerMap extends ConcurrentHashMap<String, InternetServer> {
	private static final long serialVersionUID = 1L;
	/**
	 * We can ignore skipped url without internet. Because it can restore.
	 */
	@Setter
	@Getter
	private volatile boolean availableInternet;
	/**
	 * max attempts to do request for {@link HttpService}, when it doesn't have
	 * connection it does only one request.
	 */
	@Getter
	@Setter
	private volatile int maxAttemps = 3;

	private int maxResponseFromServerForComparing = 10000;

	public boolean isSkippedURL(String url) {
		if (!availableInternet) {
			return false;
		}
		if (keySet().stream().filter(e -> url.contains(e) && !this.get(e).isAvailable()).findAny().isPresent()) {
			log.debug("skip request to server {}", url);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Tried find proper service for request or return all domains
	 * 
	 * @param list will analyze proxies
	 * @return find worked servers
	 */
	public List<String> getAliveDomainsOrUseAll(List<String> list) {
		List<String> l1 = filter(list);
		if (l1.isEmpty()) {
			return list;
		}
		return l1;
	}

	/**
	 * Tried find proper service for request or return all domains. Sort by faster
	 * response in milliseconds. If the different is less than 100 ms, then keep the
	 * order.
	 * 
	 * @param list will analyze proxies
	 * @return find worked servers
	 */
	public List<String> getAliveDomainsOrUseAllWithSort(List<String> list) {
		List<String> l1 = filter(list);
		if (l1.isEmpty()) {
			return list;
		}
		l1.sort(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				Optional<Entry<String, InternetServer>> op1 = entrySet().stream().filter(e -> o1.contains(e.getKey()))
						.findAny();
				Optional<Entry<String, InternetServer>> op2 = entrySet().stream().filter(e -> o2.contains(e.getKey()))
						.findAny();
				long longOp1 = op1
						.orElseGet(() -> new SimpleEntry<String, InternetServer>("",
								new InternetServer(true, maxResponseFromServerForComparing)))
						.getValue().getResponseTime();
				long longOp2 = op2
						.orElseGet(() -> new SimpleEntry<String, InternetServer>("",
								new InternetServer(true, maxResponseFromServerForComparing)))
						.getValue().getResponseTime();
				return Math.abs(longOp1 - longOp2) < 100 ? 0 : Long.compare(longOp1, longOp2);
			}
		});
		return l1;
	}

	private List<String> filter(List<String> list) {
		List<String> l1 = list.stream().filter(s -> !isSkippedURL(s)).collect(Collectors.toList());
		return l1;
	}

	public boolean hasInternet() {
		return values().stream().anyMatch(e -> e.isAvailable()) ? true : false;
	}

	public boolean hasInternetForDomains(List<String> domains) {
		if (!availableInternet) {
			return false;
		}
		List<String> l1 = filter(domains);
		return !l1.isEmpty();
	}

}
