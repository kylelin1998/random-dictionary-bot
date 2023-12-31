package code.util;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Slf4j
public class DownloadUtil {

    public static Optional<File> download(boolean cache, String path, String url) {
        if (cache) {
            File file = new File(path);
            if (file.exists() && file.length() != 0) {
                return Optional.of(file);
            }
        }

        try {
            HttpResponse<File> response = Unirest
                    .get(url)
                    .asFile(path, StandardCopyOption.REPLACE_EXISTING);
            File body = response.getBody();
            if (null == body || body.length() == 0) {
                return Optional.empty();
            }
            return Optional.of(body);
        } catch (Exception e) {
            log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
        }
        return Optional.empty();
    }

}
