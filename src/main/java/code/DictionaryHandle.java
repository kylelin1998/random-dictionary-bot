package code;

import code.eneity.EnglishDictionaryTableEntity;
import code.repository.EnglishDictionaryRepository;
import code.repository.base.TableField;
import code.util.ExceptionUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DictionaryHandle {

    public final static code.repository.EnglishDictionaryRepository EnglishDictionaryRepository = new EnglishDictionaryRepository();

    public static void main(String[] args) {
        File file = new File(System.getProperty("user.dir") + File.separator + "config" + File.separator + "resources");
        file.list((File dir, String name) -> {
            File json = new File(dir, name);
            try {
                String content = FileUtils.readFileToString(json, "UTF-8");
                handle(content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        });
    }

    private static void handle(String content) {
        log.info("处理中...");
        String[] split = StringUtils.split(content, "\n");
        log.info("需要处理 {} 条数据...", split.length);
        List<EnglishDictionaryTableEntity> list = new ArrayList<>();
        for (String line : split) {
            EnglishDictionaryTableEntity entity = parse(line);
            if (null != entity) {
                list.add(entity);
            }
        }
        for (EnglishDictionaryTableEntity entity : list) {
            EnglishDictionaryRepository.save(entity);
        }
        log.info("处理完毕");
    }

    private static EnglishDictionaryTableEntity parse(String line) {
        if (StringUtils.isBlank(line)) {
            return null;
        }
        try {
            JSONObject object = JSON.parseObject(line);
            String headWord = object.getString("headWord");
            String bookId = object.getString("bookId");
            JSONObject word = object.getJSONObject("content").getJSONObject("word");
            String wordId = word.getString("wordId");
            JSONObject content = word.getJSONObject("content");
            JSONObject sentence = content.getJSONObject("sentence");
            JSONArray sentences = null;
            if (null != sentence) {
                sentences = sentence.getJSONArray("sentences");
            }

            String usphone = content.getString("usphone");
            String ukphone = content.getString("ukphone");
            JSONObject syno = content.getJSONObject("syno");
            JSONArray syncs = null;
            if (null != syno) {
                syncs = syno.getJSONArray("synos");
            }

            if (null == sentences) {
                sentences = new JSONArray();
            }
            if (null == syncs) {
                syncs = new JSONArray();
            }

            EnglishDictionaryTableEntity entity = new EnglishDictionaryTableEntity();
            entity.setWord(headWord);
            entity.setBookId(bookId);
            entity.setSentenceJson(sentences.toJSONString());
            entity.setSynosJson(syncs.toJSONString());
            entity.setUsSpeech(usphone);
            entity.setUkSpeech(ukphone);
            entity.setWordId(wordId);
            return entity;
        } catch (Exception e) {
            log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
        }
        return null;
    }

}
