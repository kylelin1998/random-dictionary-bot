package code.handler;

import code.config.Config;
import code.eneity.EnglishDictionaryTableEntity;
import code.eneity.GuessTableEntity;
import code.eneity.UserSettingsTableEntity;
import code.eneity.VoiceTypeEnum;
import code.util.DicVoiceUtil;
import code.util.ExceptionUtil;
import code.util.StatsUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.world.knife.Bot;
import com.world.knife.BotInfo;
import com.world.knife.ability.InlineKeyboardButtonBuilder;
import com.world.knife.ability.InlineKeyboardButtonListBuilder;
import com.world.knife.handler.StepsCenter;
import com.world.knife.steps.StepResult;
import com.world.knife.steps.StepsBuilder;
import com.world.knife.steps.StepsChatSession;
import com.world.knife.telegram.TelegramMessageUtil;
import com.world.knife.telegram.objects.Update;
import com.world.knife.telegram.objects.chat.Chat;
import com.world.knife.telegram.objects.message.Message;
import com.world.knife.telegram.objects.message.SendAudio;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static code.Main.*;

@Slf4j
public class Handler {

    private static final String[] successTextArr = new String[] {
            "\uD83D\uDC4D 猜对啦！ 火眼金睛，就像磁铁一样吸引住正确答案！",
            "\uD83D\uDC4D 猜对啦！ 当你猜对答案时，天空中会有一颗闪闪发光的星星为你点赞！",
            "\uD83D\uDC4D 猜对啦！ 猜对答案，你就像是拥有了一块看透一切的魔镜！",
            "\uD83D\uDC4D 猜对啦！ 猜对答案就如同在迷宫中发现了暗藏的宝藏一样，让人充满惊喜！",
            "\uD83D\uDC4D 猜对啦！ 你的答案猜得如此准确，就像是放了一枚可以对抗时间的时间停止器！",
            "\uD83D\uDC4D 猜对啦！ 每次你猜对答案，一只可爱的小精灵就会在心中为你欢呼雀跃！",
            "\uD83D\uDC4D 猜对啦！ 猜对答案就像是打开了通向知识世界的大门，带来了无尽的可能性！",
            "\uD83D\uDC4D 猜对啦！ 你的猜中率高得让人怀疑你是不是超能力者！",
            "\uD83D\uDC4D 猜对啦！ 每次你猜对答案，世界上的彩虹都会变得更加灿烂多彩！",
            "\uD83D\uDC4D 猜对啦！ 你的答案就像是谜题中缺失的一块拼图，完美地填补了整个场景！",
            "\uD83D\uDC4D 猜对啦！ 每当你猜对答案，宇宙中的星辰都会争相闪耀，为你鼓掌欢呼！",
            "\uD83D\uDC4D 猜对啦！ 你的猜测真是比挖宝还刺激，每次都能揭开隐藏在谜底下的珍贵宝藏！",
            "\uD83D\uDC4D 猜对啦！ 猜对答案，你就像是掌握了时间的旅行者，能够预知未来的一切！"
    };
    private static final Map<String, UserSettingsTableEntity> USER_SETTINGS_CACHE = ExpiringMap.builder()
            .expiration(1, TimeUnit.HOURS)
            .build();
    private static final Map<String, Optional<GuessTableEntity>> GUESS_CACHE = ExpiringMap.builder()
            .expiration(10, TimeUnit.MINUTES)
            .build();

    public static boolean isAdmin(StepsChatSession session) {
        try {
            if (session.getFromId().equals(GlobalConfig.getBot().getAdminId())) {
                return true;
            }
        } catch (Exception e) {
            log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
        }
        return false;
    }

    public static void init() {
        Bot.addBeforeAdviceUpdateCallback((BotInfo var1, Update var2) -> {
            StatsUtil.submit(GlobalConfig.getStats().getDomain(), var1.getToken(), GlobalConfig.getStats().getApiKey(), var2);

            if (null != var2.getMessage()) {
                String id = String.valueOf(var2.getMessage().getFrom().getId());
                if (!USER_SETTINGS_CACHE.containsKey(id)) {
                    UserSettingsTableEntity entity = UserSettingsRepository.get(id);
                    if (null == entity) {
                        entity = new UserSettingsTableEntity();
                        entity.setChatId(id);
                        entity.setVoiceType(VoiceTypeEnum.US.getNum());
                        entity.setGuessSuccessCount(0);
                        UserSettingsRepository.save(entity);
                    } else {
                        USER_SETTINGS_CACHE.put(id, entity);
                    }
                }
            }
            return true;
        });
        Bot.setOtherUpdateCallback((StepsChatSession session) -> {
            try {
                if (!guess(session)) {
                    search(session);
                }
            } catch (Exception e) {
                log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
            }
        });

        StepsBuilder
                .create()
                .bindCommand(Command.Start.getCmd(), Command.Help.getCmd())
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    messageHandle.sendMessage(session.getBotInfo().getToken(), session.getChatId(), "系统发生未知错误！", false);
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    UserSettingsTableEntity entity = UserSettingsRepository.get(session.getFromId());

                    int voiceType = entity.getVoiceType().intValue();
                    InlineKeyboardButtonListBuilder listBuilder = InlineKeyboardButtonListBuilder
                            .create()
                            .add(
                                    InlineKeyboardButtonBuilder
                                            .create()
                                            .add((voiceType == 1 ? "✅ " : "") + "英音", StepsCenter.buildCallbackData(true, session, Command.ChooseVoiceType.getCommand(), "1"))
                                            .add((voiceType == 2 ? "✅ " : "") + "美音", StepsCenter.buildCallbackData(true, session, Command.ChooseVoiceType.getCommand(), "2"))
                                            .build()
                            )
                            .add(
                                    InlineKeyboardButtonBuilder
                                            .create()
                                            .add("\uD83D\uDCD6 来一个单词！", StepsCenter.buildCallbackData(true, Command.RandomCard.getCommand(), ""))
                                            .build()
                            );

                    messageHandle.sendMessage(session.getBotInfo().getToken(), session.getChatId(), GlobalConfig.getBot().getHelpText(), false, listBuilder.buildReplyMarkup());

                    sendRandomCard(session);

                    return StepResult.end();
                })
                .build();
        StepsBuilder
                .create()
                .bindCommand(Command.DeleteMessage.getCmd())
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    messageHandle.sendMessage(session.getBotInfo().getToken(), session.getChatId(), "系统发生未知错误！", false);
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    if (null != session.getCallbackQuery()) {
                        messageHandle.deleteMessage(session.getBotInfo().getToken(), session.getCallbackQuery().getMessage());
                    }
                    return StepResult.end();
                })
                .build();
        StepsBuilder
                .create()
                .bindCommand(Command.ChooseVoiceType.getCmd())
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    messageHandle.sendMessage(session.getBotInfo().getToken(), session.getChatId(), "系统发生未知错误！", false);
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    String text = session.getText();
                    if (StringUtils.isNotBlank(text)) {
                        Optional<VoiceTypeEnum> optional = VoiceTypeEnum.get(NumberUtils.toInt(text, 1));
                        if (optional.isPresent()) {
                            VoiceTypeEnum voiceTypeEnum = optional.get();

                            UserSettingsTableEntity entity = new UserSettingsTableEntity();
                            entity.setChatId(session.getFromId());
                            entity.setVoiceType(voiceTypeEnum.getNum());
                            UserSettingsRepository.save(entity);

                            InlineKeyboardButtonListBuilder listBuilder = InlineKeyboardButtonListBuilder
                                    .create()
                                    .add(
                                            InlineKeyboardButtonBuilder
                                                    .create()
                                                    .add((voiceTypeEnum.getNum() == 1 ? "✅ " : "") + "英音", StepsCenter.buildCallbackData(true, session, Command.ChooseVoiceType.getCommand(), "1"))
                                                    .add((voiceTypeEnum.getNum() == 2 ? "✅ " : "") + "美音", StepsCenter.buildCallbackData(true, session, Command.ChooseVoiceType.getCommand(), "2"))
                                                    .build()
                                    )
                                    .add(
                                            InlineKeyboardButtonBuilder
                                                    .create()
                                                    .add("来一个单词！", StepsCenter.buildCallbackData(true, Command.RandomCard.getCommand(), ""))
                                                    .build()
                                    );
                            messageHandle.editMessageReplyMarkup(session.getBotInfo().getToken(), session.getCallbackQuery().getMessage(), listBuilder.buildInlineKeyboardMarkup());
                        }
                    }
                    return StepResult.end();
                })
                .build();
        StepsBuilder
                .create()
                .bindCommand(Command.RandomCard.getCmd())
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    messageHandle.sendMessage(session.getBotInfo().getToken(), session.getChatId(), "系统发生未知错误！", false);
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    sendRandomCard(session);
                    return StepResult.end();
                })
                .build();
        StepsBuilder
                .create()
                .bindCommand(Command.Guess.getCmd())
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    messageHandle.sendMessage(session.getBotInfo().getToken(), session.getChatId(), "系统发生未知错误！", false);
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    startGuess(session);
                    return StepResult.end();
                })
                .build();
        StepsBuilder
                .create()
                .bindCommand(Command.StopGuess.getCmd())
                .debug(GlobalConfig.getDebug())
                .error((Exception e, StepsChatSession session) -> {
                    log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
                    messageHandle.sendMessage(session.getBotInfo().getToken(), session.getChatId(), "系统发生未知错误！", false);
                })
                .steps((StepsChatSession session, int index, List<String> list, Map<String, Object> context) -> {
                    stopGuess(session);
                    return StepResult.end();
                })
                .build();
    }

    private static void startGuess(StepsChatSession session) {
        UserSettingsTableEntity userSettingsTableEntity = UserSettingsRepository.get(session.getFromId());
        String voiceType = "美音";
        int voice = 2;
        if (null != userSettingsTableEntity) {
            if (userSettingsTableEntity.getVoiceType().intValue() == 1) {
                voiceType = "英音";
                voice = 1;
            }
        }

        GuessRepository.delete(session.getChatId());
        GUESS_CACHE.remove(session.getChatId());

        EnglishDictionaryTableEntity dic = EnglishDictionaryRepository.random();
        String voicePath = Config.TempDir + File.separator + UUID.randomUUID().toString() + ".mp3";
        Optional<File> voiceFile = DicVoiceUtil.speak(voicePath, dic.getWord(), voice);
        if (voiceFile.isPresent()) {
            InlineKeyboardButtonListBuilder listBuilder = InlineKeyboardButtonListBuilder
                    .create()
                    .add(
                            InlineKeyboardButtonBuilder
                                    .create()
                                    .add("\uD83D\uDE12 我不想猜了", StepsCenter.buildCallbackData(true, Command.StopGuess.getCommand(), ""))
                                    .add("\uD83D\uDE10 来个新的", StepsCenter.buildCallbackData(true, Command.Guess.getCommand(), ""))
                                    .build()
                    )
                    ;

            SendAudio sendAudio = new SendAudio();
            sendAudio.setAudio(voiceFile.get());
            sendAudio.setChatId(session.getChatId());
            sendAudio.setCaption("\uD83D\uDE0B 猜猜这个单词是什么吧， 请向我发送单词...");
            sendAudio.setTitle(voiceType);
            sendAudio.setReplyMarkup(listBuilder.buildReplyMarkup());
            Message message = TelegramMessageUtil.sendAudio(session.getBotInfo().getToken(), sendAudio).get();

            GuessTableEntity guessTableEntity = new GuessTableEntity();
            guessTableEntity.setChatId(session.getChatId());
            guessTableEntity.setWord(dic.getWord());
            guessTableEntity.setMessageId(String.valueOf(message.getMessageId()));
            GuessRepository.save(guessTableEntity);
            GUESS_CACHE.put(session.getChatId(), Optional.of(guessTableEntity));
        }
    }
    private static void stopGuess(StepsChatSession session) {
        GuessTableEntity guessTableEntity = GuessRepository.get(session.getChatId());
        if (null != guessTableEntity) {
            GuessRepository.delete(session.getChatId());
            GUESS_CACHE.remove(session.getChatId());

            messageHandle.sendMessage(session.getBotInfo().getToken(), session.getChatId(), session.getReplyToMessageId(), "答案是: %s".formatted(guessTableEntity.getWord()), false);
            EnglishDictionaryTableEntity entity = EnglishDictionaryRepository.search(guessTableEntity.getWord());
            if (null != entity) {
                sendCard(session, entity);
            }
        }
    }
    private static boolean guess(StepsChatSession session) {
        if (StringUtils.isBlank(session.getText())) {
            return false;
        }
        boolean containsKey = GUESS_CACHE.containsKey(session.getChatId());
        if (!containsKey) {
            GuessTableEntity guessTableEntity = GuessRepository.get(session.getChatId());
            GUESS_CACHE.put(session.getChatId(), Optional.ofNullable(guessTableEntity));
        }
        if (!GUESS_CACHE.get(session.getChatId()).isPresent()) {
            return false;
        }
        GuessTableEntity guessTableEntity = GUESS_CACHE.get(session.getChatId()).get();

        String text = session.getText().toLowerCase();
        String word = guessTableEntity.getWord().toLowerCase();
        if (text.contains(word)) {
            GuessRepository.delete(session.getChatId());
            GUESS_CACHE.remove(session.getChatId());

            int index = RandomUtils.nextInt(0, successTextArr.length);
            UserSettingsTableEntity userSettingsTableEntity = UserSettingsRepository.get(session.getFromId());
            userSettingsTableEntity.setGuessSuccessCount(userSettingsTableEntity.getGuessSuccessCount().intValue() + 1);
            UserSettingsRepository.save(userSettingsTableEntity);

            messageHandle.sendMessage(session.getBotInfo().getToken(), session.getChatId(), session.getReplyToMessageId(), successTextArr[index] + "\n\n✅ 已累计回答正确: %s 个单词".formatted(userSettingsTableEntity.getGuessSuccessCount()), false);
        } else {
            messageHandle.sendMessage(session.getBotInfo().getToken(), session.getChatId(), session.getReplyToMessageId(), "猜错了， 给你一点提示， 开头字母是: %s".formatted(StringUtils.substring(word, 0, 1)), false);
        }
        return true;
    }

    private static void sendRandomCard(StepsChatSession session) {
        EnglishDictionaryTableEntity entity = EnglishDictionaryRepository.random();
        sendCard(session, entity);
    }

    private static void search(StepsChatSession session) {
        Message message = session.getMessage();
        if (null != message) {
            Chat chat = message.getChat();
            if (chat.getType().equals("private")) {
                String text = session.getText();
                if (StringUtils.isNotBlank(text)) {
                    EnglishDictionaryTableEntity entity = EnglishDictionaryRepository.search(text);
                    if (null != entity) {
                        sendCard(session, entity);
                    } else {
                        messageHandle.sendMessage(session.getBotInfo().getToken(), session.getChatId(), "未查询到相关单词", false);
                    }
                }
            }
        }
    }

    private static void sendCard(StepsChatSession session, EnglishDictionaryTableEntity entity) {
        UserSettingsTableEntity userSettingsTableEntity = UserSettingsRepository.get(session.getFromId());
        String voiceType = "美音";
        int voice = 2;
        if (null != userSettingsTableEntity) {
            if (userSettingsTableEntity.getVoiceType().intValue() == 1) {
                voiceType = "英音";
                voice = 1;
            }
        }
        String word = entity.getWord();
        String voicePath = Config.TempDir + File.separator + UUID.randomUUID().toString() + ".mp3";
        Optional<File> voiceFile = DicVoiceUtil.speak(voicePath, word, voice);
        if (voiceFile.isPresent()) {
            StringBuilder builder = new StringBuilder();
            builder.append("%s\n[英音: %s  美音: %s]\n\n".formatted(word, StringUtils.defaultIfBlank(entity.getUkSpeech(), "暂无"), StringUtils.defaultIfBlank(entity.getUsSpeech(), "暂无")));
            if (!"[]".equals(entity.getSynosJson())) {
                JSONArray objects = JSON.parseArray(entity.getSynosJson());
                for (int count = 0; count < objects.size(); count++) {
                    JSONObject object = objects.getJSONObject(count);
                    builder.append("%s. %s\n".formatted(object.getString("pos"), object.getString("tran")));
                }
                builder.append("\n");
            }
            if (!"[]".equals(entity.getSentenceJson())) {
                JSONArray objects = JSON.parseArray(entity.getSentenceJson());
                builder.append("例句: \n");
                for (int count = 0; count < objects.size(); count++) {
                    if (count > 5) {
                        break;
                    }
                    JSONObject object = objects.getJSONObject(count);
                    builder.append("%s\n%s\n\n".formatted(object.getString("sContent"), object.getString("sCn")));
                }
                builder.append("\n");
            }

            InlineKeyboardButtonListBuilder listBuilder = InlineKeyboardButtonListBuilder
                    .create()
                    .add(
                            InlineKeyboardButtonBuilder
                                    .create()
                                    .add("\uD83D\uDD04 换一个", StepsCenter.buildCallbackData(true, Command.RandomCard.getCommand(), ""))
                                    .build()
                    );

            SendAudio sendAudio = new SendAudio();
            sendAudio.setAudio(voiceFile.get());
            sendAudio.setChatId(session.getChatId());
            sendAudio.setCaption(builder.toString());
            sendAudio.setTitle(voiceType);
            sendAudio.setReplyMarkup(listBuilder.buildReplyMarkup());
            TelegramMessageUtil.sendAudio(session.getBotInfo().getToken(), sendAudio);
        }
    }

}
