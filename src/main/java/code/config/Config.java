package code.config;

import code.util.ExceptionUtil;
import com.world.knife.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class Config {

    private static String UserDir = System.getProperty("user.dir");

    public final static String CurrentDir = (UserDir.equals("/") ? "" : UserDir) + File.separator + "config";

    public static String SettingsPath = CurrentDir + File.separator + "config.yaml";
    public static final String DBPath = CurrentDir + File.separator + "db.db";
    public static final String DictionaryPath = CurrentDir + File.separator + "dictionary.db";
    public final static String TempDir = CurrentDir + "/temp";

    static {
        mkdirs(CurrentDir, TempDir);

        List<String> list = new ArrayList<>();
        list.add(UserDir);
        list.add(CurrentDir);
        list.add(SettingsPath);
        log.info(list.stream().collect(Collectors.joining("\n")));

        ThreadUtil.newIntervalWithTryCatch(() -> {
            File file = new File(TempDir);
            ArrayList<File> files = new ArrayList<>();
            file.list((File dir, String name) -> {
                File file1 = new File(dir, name);
                try {
                    BasicFileAttributes attributes = Files.readAttributes(file1.toPath(), BasicFileAttributes.class);
                    FileTime fileTime = attributes.creationTime();
                    long millis = System.currentTimeMillis() - fileTime.toMillis();
                    if (millis > 3600000) {
                        files.add(file1);
                    }
                } catch (IOException e) {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                }

                return true;
            });

            for (File file1 : files) {
                file1.delete();
            }
        }, 1, TimeUnit.MINUTES);
    }

    private static void mkdirs(String... path) {
        for (String s : path) {
            File file = new File(s);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }

    public synchronized static ConfigSettings readConfig() {
        try {
            File file = new File(SettingsPath);
            boolean exists = file.exists();
            if (exists) {
                String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                DumperOptions dumperOptions = new DumperOptions();
                Representer representer = new Representer(dumperOptions);
                representer.setPropertyUtils(new CustomPropertyUtils());
                LoaderOptions loaderOptions = new LoaderOptions();
                Yaml yaml = new Yaml(new Constructor(ConfigSettings.class, loaderOptions), representer);
                ConfigSettings configSettings = yaml.load(text);

                Boolean debug = configSettings.getDebug();
                if (null == debug) {
                    configSettings.setDebug(false);
                }
                return configSettings;
            } else {
                log.warn("Settings file not found, " + SettingsPath);
            }
        } catch (IOException e) {
            log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e), SettingsPath);
        }
        return null;
    }

//    public synchronized static boolean saveConfig(ConfigSettings configSettings) {
//        try {
//            File file = new File(SettingsPath);
//            FileUtils.write(file, JSON.toJSONString(configSettings, JSONWriter.Feature.PrettyFormat), StandardCharsets.UTF_8);
//            return true;
//        } catch (IOException e) {
//            log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
//        }
//        return false;
//    }

}
