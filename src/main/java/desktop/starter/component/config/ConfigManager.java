package desktop.starter.component.config;

import com.google.gson.*;
import desktop.starter.Settings;
import desktop.starter.model.OSInfo;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final OSInfo.OSType osType;
    private JsonObject json;
    private List<File> files;
    private String directory;

    public ConfigManager(OSInfo.OSType osType) {
        this.osType = osType;
        this.files = new ArrayList<>();
    }

    /**
     * Load json config from URL
     */
    public void load() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(Settings.getConfigURL());
        try (CloseableHttpResponse response = httpclient.execute(httpget)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //get json contents
                String responseJson = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                //convert to json
                json = JsonParser.parseString(responseJson).getAsJsonObject();
            }
        }
    }

    /**
     * Parse json config
     */
    public void parse() {
        Gson gson = new Gson();

        //get files to download
        JsonArray jsonFiles = json.getAsJsonArray("files");
        for (JsonElement element : jsonFiles) {
            JsonObject file = element.getAsJsonObject();
            files.add(gson.fromJson(file, File.class));
        }

        //get default directory
        JsonObject directories = json.getAsJsonObject("directories");
        String osName = osType.name().toLowerCase();
        if (directories.has(osName)) {
            directory = directories.get(osName).getAsJsonObject().get("default").getAsString();
        }
    }
}
