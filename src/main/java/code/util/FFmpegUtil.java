package code.util;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FFmpegUtil {

    public static void main(String[] args) {
        compress("F:\\ffmpeg\\bin\\ffmpeg.exe", "C:\\Users\\10673\\Videos\\948415346-1-30080.mp4", "50M", "C:\\Users\\10673\\Videos\\b.mp4");
    }

    public static void compress(String ffmpegPath, String aviPath, String size, String outPath) {
        List<String> commend = new ArrayList<String>();
        commend.add(ffmpegPath);
        commend.add("-i");
        commend.add(aviPath);
        commend.add("-fs");
        commend.add(size);
        commend.add(outPath);
        try {
            ProcessBuilder builder = new ProcessBuilder(commend);
            builder.command(commend);
            log.info("ffmpeg commend: {}", JSON.toJSONString(commend));
            Process p = builder.start();

            // 获取外部程序标准输出流
            new Thread(new OutputHandlerRunnable(p.getInputStream(), false)).start();
            // 获取外部程序标准错误流
            new Thread(new OutputHandlerRunnable(p.getErrorStream(), true)).start();
            int code = p.waitFor();
            log.info("ffmpeg commend: {} result: {}", JSON.toJSONString(commend), code);

            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mergeVideoAndAudio(String ffmpegPath,String mp3Path,String aviPath,String outPath) {
        List<String> commend = new ArrayList<String>();
        commend.add(ffmpegPath);
        commend.add("-i");
        commend.add(mp3Path);
        commend.add("-i");
        commend.add(aviPath);
        commend.add("-acodec");
        commend.add("copy");
        commend.add("-vcodec");
        commend.add("copy");
        commend.add(outPath);
        try {
            ProcessBuilder builder = new ProcessBuilder(commend);
            builder.command(commend);
            log.info("ffmpeg commend: {}", JSON.toJSONString(commend));
            Process p = builder.start();

            // 获取外部程序标准输出流
            new Thread(new OutputHandlerRunnable(p.getInputStream(), false)).start();
            // 获取外部程序标准错误流
            new Thread(new OutputHandlerRunnable(p.getErrorStream(), true)).start();
            int code = p.waitFor();
            log.info("ffmpeg commend: {} result: {}", JSON.toJSONString(commend), code);

            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class OutputHandlerRunnable implements Runnable {
        private InputStream in;

        private boolean error;

        public OutputHandlerRunnable(InputStream in, boolean error) {
            this.in = in;
            this.error = error;
        }

        @Override
        public void run() {
            try (BufferedReader bufr = new BufferedReader(new InputStreamReader(this.in))) {
                String line = null;
                while ((line = bufr.readLine()) != null) {
                    if (error) {
                        System.out.println(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
