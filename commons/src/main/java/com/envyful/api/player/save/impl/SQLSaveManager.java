package com.envyful.api.player.save.impl;

import com.envyful.api.database.Database;
import com.envyful.api.database.sql.SqlType;
import com.envyful.api.database.sql.UtilSql;
import com.envyful.api.player.PlayerManager;

public class SQLSaveManager<T> extends EmptySaveManager<T> {

    private final Database database;

    public SQLSaveManager(PlayerManager<?, ?> playerManager, Database database) {
        super(playerManager);

        this.database = database;
    }

    @Override
    public boolean delete(String name) {
        UtilSql.update(this.database).query("DELETE FROM ?;").data(SqlType.text(name)).executeAsync();
        return true;
    }
}