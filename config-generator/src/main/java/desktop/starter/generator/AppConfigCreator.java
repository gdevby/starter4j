package desktop.starter.generator;

import desktop.starter.generator.model.AppConfigModel;
import desktop.starter.generator.util.Util;
import desktop.starter.model.AppConfig;
import desktop.starter.util.DesktopUtil;
import desktop.starter.util.model.download.Metadata;
import desktop.starter.util.model.download.Repo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class AppConfigCreator {

    public static final String APP_CONFIG_GENERATOR = "appConfigModel.json";
    public static final String FTP_CONFIG = "ftpConfig.json";
    public static final String TEMP_APP_CONFIG = "tempAppConfig.json";
    /**
     * Read from local machine
     */
    private List<FtpInfo> ftpInfos;
    //todo add sftp and ftp saving of the file
    //todo create config

    /**
     * @param configFile contains config app
     * @return generated AppConfig
     */

    public AppConfig createConfig(AppConfigModel configFile, List<FtpInfo> ftpInfos) throws IOException {
        AppConfig appConfig = new AppConfig();
        appConfig.setAppName(configFile.getAppName());
        appConfig.setArguments(configFile.getArguments());
//        appConfig.setJvms(configFile.getJvms());
        appConfig.setMainClass(configFile.getMainClass());
//        appConfig.setDependencies(createRepo(Paths.get(configFile.getDependenciesPath()), ftpInfos));
//        appConfig.setResources(createRepo(Paths.get(configFile.getResourcesPath()), ftpInfos));
        return appConfig;
    }


    public void saveFiles(AppConfig config) {
        //todo test before add exists or not(file will have filename.zip.signature
        //check current remote file withsignature  can be like this {"length":1,"sha1":"ddf"};
        //and if file exists we can add filename.zip.1 ... 10 etc and generage signature filename.zip.signature

    }

    /**
     * we can save config on remote machines like files
     *
     * @param config
     */
    public void printConfig(AppConfig config) {
        //saved config we ca
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
    private Repo createRepo(Path folder, List<FtpInfo> ftpInfos) throws IOException {
        List<String> domains = ftpInfos.stream().map(FtpInfo::getDomain).collect(Collectors.toList());
        List<Metadata> metadataList = Files.walk(folder).filter(Files::isRegularFile).map(Util.wrap(e -> {
            Path s = e.subpath(1, e.getNameCount());
            Metadata m = new Metadata();
            m.setRelativeUrl(s.toString());
            m.setSha1(DesktopUtil.getChecksum(e.toFile(), "SHA-1"));
            m.setSize(e.toFile().length());
            m.setPath(s.toString());
            return m;
        })).collect(Collectors.toList());
        Repo r = new Repo();
//        r.setResources(metadataList);
        r.setRepositories(domains);
        return r;
    }
}
