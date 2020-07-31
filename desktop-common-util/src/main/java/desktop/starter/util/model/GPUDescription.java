package desktop.starter.util.model;

import desktop.starter.util.OSInfo;
import lombok.Data;

import java.util.List;

@Data
public class GPUDescription {
    String name;
    String chipType;
    String memory;
}
