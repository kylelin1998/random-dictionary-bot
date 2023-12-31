package code.handler;

import com.world.knife.steps.CommandBuilder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum Command {

    Start("start", "启动机器人", true),
    RandomCard("random", "随机单词卡片", true),
    Help("help", "帮助", true),

    DeleteMessage("0988PLQ", "", false),

    ChooseVoiceType("DIC001", "", false),

    ;

    private String cmd;
    private String desc;
    private boolean stable;

    Command(String cmd, String desc, boolean stable) {
        this.cmd = cmd;
        this.desc = desc;
        this.stable = stable;
    }


    public static Command toCmd(String cmd) {
        for (Command value : Command.values()) {
            if (value.getCmd().equals(cmd)) {
                return value;
            }
        }
        return null;
    }

    public static List<Command> getStableList() {
        ArrayList<Command> list = new ArrayList<>();
        for (Command value : Command.values()) {
            if (value.isStable()) {
                list.add(value);
            }
        }
        return list;
    }

    public static CommandBuilder getCommandBuilder() {
        CommandBuilder builder = CommandBuilder.create();
        for (Command command : values()) {
            builder.addCommand(command.getCmd(), command.getDesc(), command.isStable());
        }
        return builder;
    }

    public com.world.knife.steps.Command getCommand() {
        return new com.world.knife.steps.Command(getCmd(), getDesc(), isStable());
    }
    public static boolean exist(String cmd) {
        return null != toCmd(cmd);
    }

}
