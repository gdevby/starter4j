package by.gdev.model;

import by.gdev.process.JavaProcess;
import lombok.Data;

@Data
public class StatusModel {
	JavaProcess process;
	String line;
	Exception exeption;
}
