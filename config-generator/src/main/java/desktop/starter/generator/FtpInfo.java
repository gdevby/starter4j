package desktop.starter.generator;

import lombok.Data;

@Data
public class FtpInfo {
    String address;
    String login;
    String pass;
    /**
     * check exists file or not with this domain name
     */
    String domain;
}