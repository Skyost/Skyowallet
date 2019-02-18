package fr.skyost.skyowallet.sync.queue;

import fr.skyost.skyowallet.sync.SyncManager;
import fr.skyost.skyowallet.sync.connection.DatabaseConnection;
import fr.skyost.skyowallet.sync.connection.MySQLConnection;
import fr.skyost.skyowallet.sync.connection.SQLiteConnection;
import fr.skyost.skyowallet.sync.synchronizer.SkyowalletAccountSynchronizer;
import fr.skyost.skyowallet.sync.synchronizer.SkyowalletBankSynchronizer;
import fr.skyost.skyowallet.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.sql.SQLException;

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
		enqueue(syncManager.getSkyowallet().getAccountManager().list());
		enqueue(syncManager.getSkyowallet().getBankManager().list());

		final MySQLConnection mySQLConnection = Util.tryOpenConnection(syncManager.getMySQLConnection());
		final SQLiteConnection sqLiteConnection = Util.tryOpenConnection(syncManager.getSQLiteConnection());

		final SkyowalletBankSynchronizer bankSynchronizer = createBankSynchronizer();
		final SkyowalletAccountSynchronizer accountSynchronizer = createAccountSynchronizer();

		if(sqLiteConnection != null) {
			bankSynchronizer.loadNewObjectsFromDatabase(this, sqLiteConnection, sqLiteConnection.getSelectBanksRequest());
			accountSynchronizer.loadNewObjectsFromDatabase(this, sqLiteConnection, sqLiteConnection.getSelectAccountsRequest());
		}

		if(Util.ifNotNull(mySQLConnection, boolean.class, DatabaseConnection::isEnabled, connection -> false)) {
			bankSynchronizer.loadNewObjectsFromDatabase(this, mySQLConnection, mySQLConnection.getSelectBanksRequest());
			accountSynchronizer.loadNewObjectsFromDatabase(this, mySQLConnection, mySQLConnection.getSelectAccountsRequest());
		}

		super.synchronize();
	}

	@Override
	String getStartMessage() {
		return ChatColor.GOLD + "Full synchronization started...";
	}



}