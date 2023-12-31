package code.eneity;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum VoiceTypeEnum {

    UK(1),
    US(2),

    ;

    private int num;

    VoiceTypeEnum(int num) {
        this.num = num;
    }

    public static Optional<VoiceTypeEnum> get(int num) {
        for (VoiceTypeEnum value : values()) {
            if (value.getNum() == num) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

}
