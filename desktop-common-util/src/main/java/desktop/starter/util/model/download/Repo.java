package desktop.starter.util.model.download;

import java.util.List;

import lombok.Data;

/**
 * We can config direct link and part path.
 *
 */
@Data
public class Repo {
	/**
	 * Example : https://github.com , ...
	 */
	private List<String> repositories;
	private List<Metadata> resources;
	/**
	 * Saves sha1 on the server allow to add more flexibility todo implement low
	 * priority
	 */
	private boolean remoteServerSHA1;
}
