package code.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ConfigSettings {

    private Boolean debug;
    private Proxy proxy;
    private Bot bot;
    private Stats stats;

    @Getter
    @Setter
    public static class Proxy {
        private Boolean enable;
        private String host;
        private Integer port;
    }

    @Getter
    @Setter
    public static class Bot {
        private String domain;
        private String adminId;
        private String name;
        private String token;
        private String helpText;
        private String mediaHandleChatId;
    }

    @Getter
    @Setter
    public static class Stats {
        private String domain;
        private String apiKey;
    }

}
