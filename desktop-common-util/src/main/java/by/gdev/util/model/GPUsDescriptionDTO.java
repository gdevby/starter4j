package by.gdev.util.model;

import lombok.Data;

import java.util.List;

@Data
public class GPUsDescriptionDTO {
    List<GPUDescription> gpus;
    String rawDescription;
}
