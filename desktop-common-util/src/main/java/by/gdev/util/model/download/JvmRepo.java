package by.gdev.util.model.download;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Used several repositories(URLs) to download jre
 *
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class JvmRepo extends Repo {
	/*
	 * Directory name inside archive
	 */
	private String jreDirectoryName;

}
