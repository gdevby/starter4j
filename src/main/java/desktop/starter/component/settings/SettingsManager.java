package desktop.starter.component.settings;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;

import java.io.InputStream;
import java.io.InputStreamReader;

@Data
public class SettingsManager {
    private static SettingsManager instance;

    private String configUrl;
    private int connectionTimeout;

    public SettingsManager() {
        //load settings.json
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream("settings.json");
        JsonObject json = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();

        //get values
        configUrl = json.get("configUrl").getAsString();
        connectionTimeout = json.get("connectionTimeout").getAsInt();

        instance = this;
    }

    public static SettingsManager getInstance() {
        return instance;
    }
}
