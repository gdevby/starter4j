package by.gdev.model;

import by.gdev.process.JavaProcess;
import lombok.Data;
/**
 * Displays the state of the running application
 * @author Robert Makrytski
 *
 */
@Data
public class StarterAppProcess {
	private JavaProcess process;
	private String line;
	private Exception exeption;
	private Integer errorCode;
}
