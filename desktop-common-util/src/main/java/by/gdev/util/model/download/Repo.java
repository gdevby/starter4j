package by.gdev.util.model.download;

import java.util.List;

import lombok.Data;

/**
 * Used several repositories(URLs) to download files
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
	 * Saves sha1 on the server allow to add more flexibility implement low
	 * priority
	 * Not implemented
	 */
	private boolean remoteServerSHA1;
}
