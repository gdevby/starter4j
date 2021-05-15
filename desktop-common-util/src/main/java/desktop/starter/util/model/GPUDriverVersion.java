package desktop.starter.util.model;

public enum GPUDriverVersion {
    CUDA_V10_2("10.2"),CUDA_V10_1("10.1"),CUDA_V_10("10"),
    ANY_AMD("ANY_AMD");

    String value;

    GPUDriverVersion(String s) {
        value = s;
    }

    public String getValue() {
        return value;
    }
}
