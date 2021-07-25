package ru.mrflaxe.simplehomes.database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import ru.mrflaxe.simplehomes.database.model.HomeModel;
import ru.soknight.lib.database.Database;
import ru.soknight.lib.executable.quiet.AbstractQuietExecutor;
import ru.soknight.lib.executable.quiet.ThrowableHandler;

public final class DatabaseManager extends AbstractQuietExecutor {

    private final ConnectionSource connection;
	private final Dao<HomeModel, String> homesDao;
	
	public DatabaseManager(Plugin plugin, Database database) throws SQLException {
        super(ThrowableHandler.createForDatabase(plugin));

		this.connection = database.establishConnection();
		this.homesDao = DaoManager.createDao(connection, HomeModel.class);
	}
	
	public void shutdown() {
        try {
            if(connection != null)
                connection.close();
        } catch (IOException ignored) {}
    }
	
	public CompletableFuture<HomeModel> getHome(String holder, String homeName) {
	    return supplyQuietlyAsync(() -> homesDao.queryBuilder().where()
                .eq("holder", holder).and()
                .eq("home_name", homeName)
                .queryForFirst()
        );
    }
	
	public CompletableFuture<HomeModel> getFirstHome(String holder) {
        return supplyQuietlyAsync(() -> homesDao.queryBuilder().where()
                .eq("holder", holder)
                .queryForFirst()
        );
    }
    
    public CompletableFuture<List<HomeModel>> getHomes(String holder) {
        return supplyQuietlyAsync(() -> homesDao.queryBuilder().where()
                .eq("holder", holder)
                .query()
        );
    }
    
    public CompletableFuture<Long> getHomesAmount(String holder) {
        return supplyQuietlyAsync(() -> homesDao.queryBuilder().where()
                .eq("holder", holder)
                .countOf()
        );
    }
    
    public CompletableFuture<Boolean> hasHome(String holder, String homeName) {
        return supplyQuietlyAsync(() -> homesDao.queryBuilder().where()
                .eq("holder", holder).and()
                .eq("home_name", homeName)
                .countOf() != 0L
        );
    }
	
	public CompletableFuture<Void> removeHome(HomeModel home) {
        return runQuietlyAsync(() -> homesDao.delete(home));
	}
	
	public CompletableFuture<Void> saveHome(HomeModel home) {
        return runQuietlyAsync(() -> homesDao.createOrUpdate(home));
	}

}
