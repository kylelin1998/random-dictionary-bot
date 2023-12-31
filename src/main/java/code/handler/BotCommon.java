package code.handler;

import com.world.knife.ability.InlineKeyboardButtonBuilder;
import com.world.knife.ability.InlineKeyboardButtonListBuilder;
import com.world.knife.handler.StepsCenter;

public class BotCommon {

    public static InlineKeyboardButtonListBuilder buildOkWithoutConfirm() {
        InlineKeyboardButtonListBuilder listBuilder = InlineKeyboardButtonListBuilder.create();
        listBuilder.add(
                InlineKeyboardButtonBuilder
                        .create()
                        .add("✅ OK", StepsCenter.buildCallbackData(true, Command.DeleteMessage.getCommand(), ""))
                        .build()
        );
        return listBuilder;
    }

}
