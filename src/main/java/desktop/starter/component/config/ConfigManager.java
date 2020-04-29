package desktop.starter.component.config;

import com.google.gson.*;
import desktop.starter.Settings;
import desktop.starter.model.Metadata;
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
    private List<Metadata> metadata;
    private String directory;

    public ConfigManager(OSInfo.OSType osType) {
        this.osType = osType;
        this.metadata = new ArrayList<>();
    }

    /**
     * Load json config from URL
     */
    public void load() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(Settings.getConfigURL());
        /*todo check entigy == 404 cas we can get not proper code
         * todo config proper client(redirect,timeout,keep alive todo 5 second) and init once ,inject it */
        try (CloseableHttpResponse response = httpclient.execute(httpget)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //get json contents
                String responseJson = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                //convert to json
                json = JsonParser.parseString(responseJson).getAsJsonObject();
                //todo we can parse  wiht gson in this place
                //todo i create full object to
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
