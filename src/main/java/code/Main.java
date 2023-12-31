package code;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import code.config.Config;
import code.config.ConfigSettings;
import code.handler.Command;
import code.handler.Handler;
import code.handler.MessageHandle;
import code.repository.EnglishDictionaryRepository;
import code.repository.GuessRepository;
import code.repository.UserSettingsRepository;
import code.util.StatsUtil;
import com.world.knife.Bot;
import com.world.knife.BotConfig;
import com.world.knife.BotRunMode;
import com.world.knife.handler.SetCommands;
import com.world.knife.steps.CommandBuilder;
import com.world.knife.telegram.TelegramProxy;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

@Slf4j
public class Main {
    public static ConfigSettings GlobalConfig = Config.readConfig();
    public static MessageHandle messageHandle = new MessageHandle();
    public final static code.repository.EnglishDictionaryRepository EnglishDictionaryRepository = new EnglishDictionaryRepository();
    public final static code.repository.UserSettingsRepository UserSettingsRepository = new UserSettingsRepository();
    public final static code.repository.GuessRepository GuessRepository = new GuessRepository();

    public static void main(String[] args) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(loggerContext);

        if (GlobalConfig.getProxy().getEnable()) {
            TelegramProxy.setProxy(GlobalConfig.getProxy().getHost(), GlobalConfig.getProxy().getPort());
        }

        Handler.init();
        StatsUtil.init();

        BotConfig botConfig = new BotConfig();
        botConfig.setBotRunMode(BotRunMode.LongPolling);

        CommandBuilder commandBuilder = Command.getCommandBuilder();
        Bot.register(botConfig, GlobalConfig.getBot().getToken(), commandBuilder);
        SetCommands.set(GlobalConfig.getBot().getToken(), commandBuilder);

        messageHandle.sendMessage(GlobalConfig.getBot().getToken(), GlobalConfig.getBot().getAdminId(), "机器人启动成功！", false);
    }
}
