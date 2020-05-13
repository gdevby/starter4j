package desktop.starter.config;

import com.google.gson.*;
import desktop.starter.settings.SettingsManager;
import desktop.starter.model.Metadata;
import desktop.starter.util.OSInfo;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static ConfigManager instance;
    private final OSInfo.OSType osType;
    private JsonObject json;
    private SettingsManager settingsManager;
    private RequestConfig requestConfig;
    private List<Metadata> metadata;
    private String directory;


    public ConfigManager() {
        instance = this;

        osType = OSInfo.getOSType();
        metadata = new ArrayList<>();
        settingsManager = SettingsManager.getInstance();
        requestConfig = RequestConfig.custom()
                .setRedirectsEnabled(true)
                .setMaxRedirects(10)
                .setSocketTimeout(settingsManager.getConnectionTimeout())
                .setConnectTimeout(settingsManager.getConnectionTimeout())
                .setConnectionRequestTimeout(settingsManager.getConnectionTimeout())
                .build();
    }

    /**
     * Load json config from URL
     */
    public void load() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(settingsManager.getConfigUrl());
        httpget.setConfig(requestConfig);

        try (CloseableHttpResponse response = httpclient.execute(httpget)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                //todo we need to add message to any exception to understand
                throw new Exception();
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //check for json content
                if (entity.getContentType().getValue().equalsIgnoreCase("application/json")) {
                    //get json contents
                    String responseJson = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    //convert to json
                    json = JsonParser.parseString(responseJson).getAsJsonObject();
                    //todo we can parse  wiht gson in this place
                    //todo i create full object to
                }
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
            metadata.add(gson.fromJson(file, Metadata.class));
        }

        //get default directory
        JsonObject directories = json.getAsJsonObject("directories");
        String osName = osType.name().toLowerCase();
        if (directories.has(osName)) {
            directory = directories.get(osName).getAsJsonObject().get("default").getAsString();
        }
    }
}
