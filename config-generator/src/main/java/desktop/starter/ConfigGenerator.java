package desktop.starter;

import desktop.starter.model.AppConfig;
import desktop.starter.model.Metadata;
import desktop.starter.model.Repo;
import desktop.starter.util.OSInfo;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConfigGenerator {
    /**
     * Read from local machine
     */
    List<FtpInfo> ftpInfos;
    //todo add sftp and ftp saving of the file
    //todo create config
    public AppConfig generateConfig(String version, String dependenciesFolder, String resourcesFolder){

        return null;
    }

    public void saveFiles(AppConfig config){
        //todo test before add exists or not(file will have filename.zip.signature
        //check current remote file withsignature  can be like this {"length":1,"sha1":"ddf"};
        //and if file exists we can add filename.zip.1 ... 10 etc and generage signature filename.zip.signature

    }

    /**
     * we can save config on remote machines like files
     * @param config
     */
    public void printConfig(AppConfig config){
        //saved config we ca
    }
    public static AppConfig generateTest(String appName) {
        AppConfig a = new AppConfig();
        a.setMainClass("desktop.starter.app.Main");
        Map<OSInfo.OSType, String> defaultFolders = new HashMap<>();
        a.setAppName("starter");
        List<Repo> list = new ArrayList<>();
        Repo r = new Repo();
        r.setRepo(Collections.singletonList("https://repo1.maven.org/maven2"));
        Metadata m = new Metadata();
        m.setRelUrl("/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar");
        m.setPath("/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar");
        m.setSize(290339);
        m.setSha1("7c4f3c474fb2c041d8028740440937705ebb473a");
        r.setResources(Collections.singletonList(m));
        list.add(r);



        r = new Repo();
        m = new Metadata();
        m.setUrls(Collections.singletonList("https://raw.githubusercontent.com/robertmakrytski/starter-app/master/apps/original-starter-app-1.0.jar"));
        m.setPath("/starter-app/master/apps/original-starter-app-1.0.jar");
        m.setSize(57830);
        m.setSha1("0bed0b7e14cbdd9e383714d5c3aa67a59eaa5311");
        r.setResources(Collections.singletonList(m));
        list.add(r);
        a.setDependencies(list);
        return a;
    }
/*
    protected FTPClient create(FtpInfo info) throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setControlKeepAliveTimeout(300);
        ftpClient.connect(info.getAddress(), port);
        ftpClient.login(info.getLogin(), info.getPass());
        logger.debug("ftp reply code  is " + ftpClient.getReplyCode());
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        return ftpClient;
    }

    public void deleteFile(String path) throws IOException {
       synchronized (this) {
            if (!init)
                init();
            for (FTPClient client : ftpClients) {
                if (!client.deleteFile(path)) {
                    throw new RuntimeException("problem with save of the file: " + path);
                }
            }
        }
    }

    private void ftpCreateDirectoryTree(FTPClient client, String dirTree) throws IOException {

        boolean dirExists = true;

        // tokenize the string and attempt to change into each directory level. If you
        // cannot, then start creating.
        String[] directories = dirTree.substring(0, dirTree.lastIndexOf("/")).split("/");
        for (String dir : directories) {
            if (!dir.isEmpty()) {
                if (dirExists) {
                    logger.trace("change working directory and check " + dir);
                    dirExists = client.changeWorkingDirectory(dir);
                }
                if (!dirExists) {
                    logger.debug("make working directory" + dir);
                    if (!client.makeDirectory(dir)) {
                        throw new IOException("Unable to create remote directory '" + dir + "'.  error='"
                                + client.getReplyString() + "'");
                    }
                    if (!client.changeWorkingDirectory(dir)) {
                        logger.debug("change working directory " + dir);
                        throw new IOException("Unable to change into newly created remote directory '" + dir
                                + "'.  error='" + client.getReplyString() + "'");
                    }
                }
            }
        }
        client.changeWorkingDirectory("/");
    }
    @PreDestroy
    public void close() {
        for (FTPClient client : ftpClients) {
            try {
                if (client.isConnected()) {
                    client.logout();
                }
            } catch (IOException ex) {
                logger.debug("", ex);
            } finally {
                try {
                    client.disconnect();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
        }
    }

    public void storeFile(byte[] array, String path) throws IOException {
        String decode = UriUtils.decode(path, StandardCharsets.ISO_8859_1.toString());
        synchronized (this) {
            if (!init)
                init();
            try {
                for (FTPClient client : ftpClients) {
                    logger.debug("trying to save file " + path + " on server " + client.getRemoteAddress());
                    ftpCreateDirectoryTree(client, decode);
                    if (!client.storeFile(decode, new ByteArrayInputStream(array))) {
                        cleanMetadata(decode);
                        throw new RuntimeException(client.getReplyCode() + " " +
                                client.getReplyString() + " " + decode);
                    }
                    logger.debug("saved file " + path + " on server " + client.getRemoteAddress());
                }

            } catch (FTPConnectionClosedException f) {
                logger.warn("", f);
                try {
                    Thread.sleep(10 * 1000L);
                } catch (InterruptedException e) {
                }
                close();
                init();
                storeFile(array, path);
            }
        }
    }

    protected void init() throws IOException {
        init = true;
        Resource resource = new ClassPathResource(defaultConfigFile);
        String text = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        List<FtpInfo> servers = ParserUtil.createMapper().readValue(text,
                new TypeReference<List<FtpInfo>>() {
                });
        ftpClients = new ArrayList<>();
        for (FtpInfo s : servers) {
            ftpClients.add(create(s));
        }
    }*/

}
