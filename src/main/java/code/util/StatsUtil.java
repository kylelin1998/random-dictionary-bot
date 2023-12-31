package code.util;

import com.world.knife.telegram.objects.Update;
import com.world.knife.util.JSONUtil;
import kong.unirest.ContentType;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class StatsUtil {

    @Data
    private static class StatsSubmit {
        private String domain;
        private String botToken;
        private String apiKey;
        private String update;
    }

    private static ConcurrentLinkedQueue<StatsSubmit> queue = new ConcurrentLinkedQueue<>();

    public static void init() {
        new Thread(() -> {
            while (true) {
                if (queue.size() > 0) {
                    StatsSubmit poll = queue.poll();
                    stats(poll);
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public static void submit(String domain, String botToken, String apiKey, Update update) {
        StatsSubmit submit = new StatsSubmit();
        submit.setUpdate(JSONUtil.toJSONString(update));
        submit.setDomain(domain);
        submit.setApiKey(apiKey);
        submit.setBotToken(botToken);

        submitStats(submit);
    }

    public static void submitStats(StatsSubmit submit) {
        if (null == submit) {
            return;
        }
        queue.add(submit);
    }

    private static void stats(StatsSubmit submit) {
        for (int i = 1; i <= 3; i++) {
            try {
                HttpResponse<String> response = Unirest
                        .post(String.format("%s/api/stats/add", submit.getDomain()))
                        .header("Authorization", submit.getApiKey())
                        .header("BotToken", submit.getBotToken())
                        .body(JSONUtil.toJSONString(submit))
                        .contentType(ContentType.APPLICATION_JSON.getMimeType())
                        .connectTimeout((int) TimeUnit.MILLISECONDS.convert(20, TimeUnit.SECONDS))
                        .socketTimeout((int) TimeUnit.MILLISECONDS.convert(20, TimeUnit.SECONDS))
                        .asString();
                String body = response.getBody();
                log.info(body);

                int status = response.getStatus();
                if (status == 200) {
                    break;
                }
                TimeUnit.SECONDS.sleep(i * 20);
            } catch (Exception e) {
                log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
            }
        }
    }

}
