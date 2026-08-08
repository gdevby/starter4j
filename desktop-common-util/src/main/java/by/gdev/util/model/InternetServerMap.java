package by.gdev.util.model;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.impl.io.HttpService;

import by.gdev.util.comporator.InternetServerMapComparator;
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

	/**
	 * Used for getAliveDomainsOrUseAllWithSort to sort with any order
	 * 
	 * 
	 */
	@Getter
	@Setter
	private Class<? extends InternetServerMapComparator> comparator;

	public InternetServerMap() {
		this.comparator = InternetServerMapComparator.class;
	}

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
	 * @param urn  used any comparator in the future, Can be empty.
	 * @return find worked servers
	 */
	public List<String> getAliveDomainsOrUseAllWithSort(List<String> list, String urn) {
		List<String> l1 = filter(list);
		if (l1.isEmpty()) {
			return list;
		}
		try {
			Constructor<? extends InternetServerMapComparator> constructor = comparator
					.getDeclaredConstructor(ConcurrentHashMap.class, String.class);
			Comparator<String> sorter = constructor.newInstance(this, urn);
			l1.sort(sorter);
		} catch (Exception e) {
			log.error("not proper comparator", e);
		}
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
