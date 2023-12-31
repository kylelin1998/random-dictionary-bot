package code.eneity;

import code.repository.base.TableEntity;
import code.repository.base.TableField;
import code.repository.base.TableName;
import lombok.Data;

@TableName(name = "user_settings_table")
@Data
public class UserSettingsTableEntity implements TableEntity {

    @TableField(name = "chat_id", sql = "chat_id text PRIMARY KEY")
    private String chatId;

    @TableField(name = "voice_type", sql = "voice_type INTEGER")
    private Integer voiceType;

    @TableField(name = "guess_success_count", sql = "guess_success_count INTEGER")
    private Integer guessSuccessCount;

}
