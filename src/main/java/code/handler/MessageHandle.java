package code.handler;

import code.util.ExceptionUtil;
import com.world.knife.ability.MessageAbility;
import com.world.knife.telegram.TelegramMessageUtil;
import com.world.knife.telegram.TelegramUtil;
import com.world.knife.telegram.objects.chat.ChatMember;
import com.world.knife.telegram.objects.chat.GetChatMember;
import com.world.knife.telegram.objects.message.Message;
import com.world.knife.telegram.objects.message.ReplyMarkup;
import com.world.knife.telegram.objects.message.SendVideo;
import com.world.knife.telegram.response.TGResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Slf4j
public class MessageHandle extends MessageAbility {
    public TGResponse<Message> sendVideo(String token, String chatId, Integer replyToMessageId, String text, Object file, ReplyMarkup replyMarkup) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chatId);
        sendVideo.setReplyToMessageId(replyToMessageId);
        sendVideo.setCaption(text);
        sendVideo.setVideo(file);
        sendVideo.setParseMode("HTML");
        if (null != replyMarkup) {
            sendVideo.setReplyMarkup(replyMarkup);
        }

        return TelegramMessageUtil.sendVideo(token, sendVideo);
    }

    public Optional<Boolean> in(String token, String chatId, String fromId) {
        try {
            GetChatMember getChatMember = new GetChatMember();
            getChatMember.setChatId(chatId);
            getChatMember.setUserId(fromId);
            TGResponse<ChatMember> chatMember = TelegramUtil.getChatMember(token, getChatMember);
            if (chatMember.isOk()) {
                String status = chatMember.get().getStatus();
                if (StringUtils.equals(status, "left")) {
                    return Optional.of(false);
                }
                return Optional.of(true);
            }
            String description = chatMember.getDescription();
            if (StringUtils.contains(description, "user not found")) {
                return Optional.of(false);
            }
        } catch (Exception e) {
            log.error(ExceptionUtil.getStackTraceWithCustomInfoToStr(e));
        }
        return Optional.empty();
    }
}
