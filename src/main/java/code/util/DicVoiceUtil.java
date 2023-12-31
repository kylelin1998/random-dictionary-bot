package code.util;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URLEncoder;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Slf4j
public class DicVoiceUtil {

    /**
     *
     * @param word 单词
     * @param type 1 为英音 2 为美音
     * @return
     */
    public static Optional<File> speak(String filePath, String word, int type) {
        String wordEncode = URLEncoder.encode(word);
        try {
            HttpResponse<File> response = Unirest
                    .get("https://dict.youdao.com/dictvoice?audio=%s&type=%s".formatted(wordEncode, type))
                    .asFile(filePath, StandardCopyOption.REPLACE_EXISTING);
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
