package by.gdev.util.model;

import lombok.Data;

import java.util.List;

import by.gdev.util.OSInfo;

@Data
public class GPUDescription {
    String name;
    String chipType;
    String memory;
}
