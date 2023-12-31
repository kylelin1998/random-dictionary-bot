package code.repository;

import code.config.Config;
import code.eneity.EnglishDictionaryTableEntity;
import code.eneity.UserSettingsTableEntity;
import code.repository.base.TableRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserSettingsRepository extends TableRepository<UserSettingsTableEntity> {

    public UserSettingsRepository() {
        super(Config.DBPath, true);
    }

    public synchronized void save(UserSettingsTableEntity entity) {
        UserSettingsTableEntity where = new UserSettingsTableEntity();
        where.setChatId(entity.getChatId());
        Integer count = super.selectCount(where);
        if (null != count && count.intValue() == 0) {
            super.insert(entity);
        } else {
            super.update(entity, where);
        }
    }

    public UserSettingsTableEntity get(String chatId) {
        UserSettingsTableEntity where = new UserSettingsTableEntity();
        where.setChatId(chatId);
        return super.selectOne(where);
    }

}
