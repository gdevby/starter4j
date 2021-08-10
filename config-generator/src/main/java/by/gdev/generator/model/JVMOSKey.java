package by.gdev.generator.model;

import by.gdev.util.OSInfo.Arch;
import by.gdev.util.OSInfo.OSType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class JVMOSKey {
	private OSType type;
	private Arch arch;
}
