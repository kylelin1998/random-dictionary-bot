package code.repository;

import code.config.Config;
import code.eneity.EnglishDictionaryTableEntity;
import code.repository.base.TableRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class EnglishDictionaryRepository extends TableRepository<EnglishDictionaryTableEntity> {

    public EnglishDictionaryRepository() {
        super(Config.DictionaryPath, false);
    }

    public void save(EnglishDictionaryTableEntity entity) {
        EnglishDictionaryTableEntity where = new EnglishDictionaryTableEntity();
        where.setWord(entity.getWord());
        where.setBookId(entity.getBookId());
        Integer count = super.selectCount(where);
        if (null != count && count.intValue() == 0) {
            super.insert(entity);
        }
    }

    public EnglishDictionaryTableEntity search(String word) {
        EnglishDictionaryTableEntity where = new EnglishDictionaryTableEntity();
        where.setWord(word);
        return super.selectOne(where);
    }

    public EnglishDictionaryTableEntity random() {
        EnglishDictionaryTableEntity where = new EnglishDictionaryTableEntity();
        return super.selectRandomOne(where);
    }

}
