package code.handler;

import code.config.Config;
import code.eneity.EnglishDictionaryTableEntity;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static code.Main.*;

@Slf4j
public class Handler {

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
            return true;
        });
        Bot.setOtherUpdateCallback((StepsChatSession session) -> {
            try {
                search(session);
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
                    if (null == entity) {
                        entity = new UserSettingsTableEntity();
                        entity.setChatId(session.getFromId());
                        entity.setVoiceType(VoiceTypeEnum.US.getNum());
                        UserSettingsRepository.save(entity);
                    }
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
                                            .add("来一个单词！", StepsCenter.buildCallbackData(true, Command.RandomCard.getCommand(), ""))
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
            builder.append("%s [英音: %s  美音: %s]\n\n".formatted(word, entity.getUkSpeech(), entity.getUsSpeech()));
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
                                    .add("换一个", StepsCenter.buildCallbackData(true, Command.RandomCard.getCommand(), ""))
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
