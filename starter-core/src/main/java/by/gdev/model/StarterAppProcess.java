package by.gdev.model;

import lombok.Data;
/**
 * Displays the state of the running application
 * @author Robert Makrytski
 *
 */
@Data
public class StarterAppProcess {
	private Process process;
	private String line;
	private Exception exeption;
	private Integer errorCode;
}
