package fr.skyost.skyowallet.sync.queue;

import fr.skyost.skyowallet.economy.EconomyObject;
import fr.skyost.skyowallet.economy.SkyowalletManager;
import fr.skyost.skyowallet.sync.SyncManager;
import fr.skyost.skyowallet.sync.connection.DatabaseConnection;
import fr.skyost.skyowallet.sync.connection.MySQLConnection;
import fr.skyost.skyowallet.sync.connection.SQLiteConnection;
import fr.skyost.skyowallet.sync.synchronizer.SkyowalletAccountSynchronizer;
import fr.skyost.skyowallet.sync.synchronizer.SkyowalletBankSynchronizer;
import fr.skyost.skyowallet.sync.synchronizer.SkyowalletSynchronizer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

/**
 * Represents a full synchronization queue.
 */

public class FullSyncQueue extends SyncQueue {

	/**
	 * Creates a new full synchronization queue instance.
	 *
	 * @param syncManager The synchronization manager.
	 * @param sender The command sender.
	 */

	public FullSyncQueue(final SyncManager syncManager, final CommandSender sender) {
		super(syncManager, sender);
	}

	@Override
	public void synchronize() throws IOException, SQLException {
		final SyncManager syncManager = this.getSyncManager();
		addToQueue(syncManager.getSkyowallet().getAccountManager().list());
		addToQueue(syncManager.getSkyowallet().getBankManager().list());

		final MySQLConnection mySQLConnection = syncManager.getMySQLConnection();
		final SQLiteConnection sqLiteConnection = syncManager.getSQLiteConnection();

		mySQLConnection.open();
		sqLiteConnection.open();

		final SkyowalletAccountSynchronizer accountSynchronizer = createAccountSynchronizer();
		final SkyowalletBankSynchronizer bankSynchronizer = createBankSynchronizer();

		loadNewObjectsFromDatabase(sqLiteConnection, accountSynchronizer, sqLiteConnection.getSelectAccountsRequest());
		loadNewObjectsFromDatabase(sqLiteConnection, bankSynchronizer, sqLiteConnection.getSelectBanksRequest());

		if(mySQLConnection.isEnabled()) {
			loadNewObjectsFromDatabase(mySQLConnection, accountSynchronizer, mySQLConnection.getSelectAccountsRequest());
			loadNewObjectsFromDatabase(mySQLConnection, bankSynchronizer, mySQLConnection.getSelectBanksRequest());
		}

		super.synchronize();
	}

	@Override
	String getStartMessage() {
		return ChatColor.GOLD + "Full synchronization started...";
	}

	/**
	 * Loads all new objects from a database.
	 *
	 * @param connection The database connection.
	 * @param synchronizer The synchronizer.
	 * @param selectQuery The query that allows to select the objects.
	 * @param <T> The type of objects to load.
	 *
	 * @throws SQLException If any SQL exception occurs.
	 */

	private <T extends EconomyObject> void loadNewObjectsFromDatabase(final DatabaseConnection connection, final SkyowalletSynchronizer<T> synchronizer, final String selectQuery) throws SQLException {
		final SkyowalletManager<T> manager = synchronizer.getManager();

		final Set<T> objects = connection.executeQuery(selectQuery, synchronizer.getResultSetHandler());
		for(final T mySQLObject : objects) {
			if(manager.has(mySQLObject.getIdentifier())) {
				continue;
			}
			manager.add(mySQLObject);
			addToQueue(mySQLObject);
		}
	}

}