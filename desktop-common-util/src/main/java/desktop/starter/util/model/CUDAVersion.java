package desktop.starter.util.model;

public enum CUDAVersion {
    V10_2("10.2"),V10_1("10.1"),V_10("10");

    String value;

    CUDAVersion(String s) {
        value = s;
    }

    public String getValue() {
        return value;
    }
}
