package code.eneity;

import code.repository.base.TableEntity;
import code.repository.base.TableField;
import code.repository.base.TableName;
import lombok.Data;

@TableName(name = "guess_table")
@Data
public class GuessTableEntity implements TableEntity {

    @TableField(name = "chat_id", sql = "chat_id text PRIMARY KEY")
    private String chatId;

    @TableField(name = "word", sql = "word text")
    private String word;

    @TableField(name = "message_id", sql = "message_id text")
    private String messageId;

}
