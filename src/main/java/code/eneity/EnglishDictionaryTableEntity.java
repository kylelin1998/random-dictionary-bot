package code.eneity;

import code.repository.base.TableEntity;
import code.repository.base.TableField;
import code.repository.base.TableName;
import lombok.Data;

@TableName(name = "english_dictionary_table")
@Data
public class EnglishDictionaryTableEntity implements TableEntity {

    @TableField(name = "id", sql = "id INTEGER PRIMARY KEY AUTOINCREMENT")
    private Integer id;

    @TableField(name = "word", sql = "word text COLLATE NOCASE")
    private String word;

    @TableField(name = "book_id", sql = "book_id text")
    private String bookId;

    @TableField(name = "word_id", sql = "word_id text")
    private String wordId;

    @TableField(name = "sentence_json", sql = "sentence_json text")
    private String sentenceJson;

    @TableField(name = "uk_speech", sql = "uk_speech text")
    private String ukSpeech;
    @TableField(name = "us_speech", sql = "us_speech text")
    private String usSpeech;

    @TableField(name = "synos_json", sql = "synos_json text")
    private String synosJson;

}
