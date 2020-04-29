package desktop.starter;

public class Settings {
    //todo create config in resources, this we can remove
    private static final String configURL = "https://minecraft.relevant-craft.su/desktop_starter/config.json";

    public static String getConfigURL() {
        return configURL;
    }
}
