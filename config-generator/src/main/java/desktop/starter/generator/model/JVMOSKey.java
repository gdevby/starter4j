package desktop.starter.generator.model;

import desktop.starter.util.OSInfo.Arch;
import desktop.starter.util.OSInfo.OSType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class JVMOSKey {
	private OSType type;
	private Arch arch;
}
