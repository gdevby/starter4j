package by.gdev.util.comporator;

import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import by.gdev.util.model.InternetServer;
import lombok.Data;

/**
 * Compare domains and sort from fast to slow by checked response time.
 */
@Data
public class InternetServerMapComparator implements Comparator<String> {
	/**
	 * Default value for not exist domains
	 */
	private int defaultMaxResponse = 10000;
	/**
	 * Used same order when response time difference is less then 10_000 MS.
	 */
	private int difference = 500;
	/**
	 * Map with domains and their info status.
	 */
	private final ConcurrentHashMap<String, InternetServer> map;
	/**
	 * Added to comparing in the future
	 */
	private final String urn;

	@Override
	public int compare(String o1, String o2) {
		Optional<Entry<String, InternetServer>> op1 = map.entrySet().stream().filter(e -> o1.contains(e.getKey()))
				.findAny();
		Optional<Entry<String, InternetServer>> op2 = map.entrySet().stream().filter(e -> o2.contains(e.getKey()))
				.findAny();
		long longOp1 = op1
				.orElseGet(
						() -> new SimpleEntry<String, InternetServer>("", new InternetServer(true, defaultMaxResponse)))
				.getValue().getResponseTime();
		long longOp2 = op2
				.orElseGet(
						() -> new SimpleEntry<String, InternetServer>("", new InternetServer(true, defaultMaxResponse)))
				.getValue().getResponseTime();
		return Math.abs(longOp1 - longOp2) < difference ? 0 : Long.compare(longOp1, longOp2);
	}

}
