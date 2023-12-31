package code.config;

import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;

import java.util.concurrent.*;

@Slf4j
public class CommonExecutorsConfig {

    private static ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(false).setNameFormat("common-pool-%d").build();

    private static ExecutorService fixedThreadPool = new ThreadPoolExecutor(
            4,
            8,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1000000),
            threadFactory,
            (Runnable r, ThreadPoolExecutor executor) -> {
                log.error(r.toString()+" is Rejected");
            }
    );

    public static void submit(Runnable task) {
        fixedThreadPool.submit(task);
    }

}
