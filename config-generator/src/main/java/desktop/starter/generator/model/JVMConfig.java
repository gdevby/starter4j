package desktop.starter.generator.model;

import java.util.Map;

import desktop.starter.util.OSInfo.Arch;
import desktop.starter.util.OSInfo.OSType;
import desktop.starter.util.model.download.Repo;
import lombok.Data;
/**
 * Used to create config for any operation system and arch of the system to download jvm config.
 * Example we find a link for windows -> x32 -> jre-default -> jre-8u281-linux-i586.
 * Build on appConfigModel.json field -> jvms
 * @author rob
 *
 */
@Data
public class JVMConfig {
	private Map<OSType, Map<Arch, Map<String,Repo>>> jvms;
}
