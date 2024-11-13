package by.gdev.util;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InternetServerMap extends HashMap<String, Boolean> {
	private static final long serialVersionUID = 1L;
	/**
	 * We can ignore skipped url without internet. Because it can restore.
	 */
	@Setter
	private boolean availableInternet;

	public boolean isSkippedURL(String url) {
		if (!availableInternet)
			return false;
		if (keySet().stream().filter(e -> url.contains(e) && !this.get(e)).findAny().isPresent()) {
			log.debug("skip request to server {}", url);
			return true;
		} else
			return false;
	}

	/**
	 * Tried find proper service for request or return all domains
	 * 
	 * @param list will analyze proxies
	 * @return find worked servers
	 */
	public List<String> getAliveDomainsOrUseAll(List<String> list) {
		List<String> l1 = list.stream().filter(s -> !isSkippedURL(s)).collect(Collectors.toList());
		if (l1.isEmpty())
			return list;
		return l1;
	}

	public boolean hasInternet() {
		return values().stream().anyMatch(e -> e) ? true : false;
	}

}
