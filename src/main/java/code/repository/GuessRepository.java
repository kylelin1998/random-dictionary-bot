package code.repository;

import code.config.Config;
import code.eneity.GuessTableEntity;
import code.eneity.UserSettingsTableEntity;
import code.repository.base.TableRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GuessRepository extends TableRepository<GuessTableEntity> {

    public GuessRepository() {
        super(Config.DBPath, false);
    }

    public synchronized void save(GuessTableEntity entity) {
        GuessTableEntity where = new GuessTableEntity();
        where.setChatId(entity.getChatId());
        Integer count = super.selectCount(where);
        if (null != count && count.intValue() == 0) {
            super.insert(entity);
        } else {
            super.update(entity, where);
        }
    }

    public void delete(String chatId) {
        GuessTableEntity where = new GuessTableEntity();
        where.setChatId(chatId);
        super.delete(where);
    }

    public GuessTableEntity get(String chatId) {
        GuessTableEntity where = new GuessTableEntity();
        where.setChatId(chatId);
        return super.selectOne(where);
    }

}
