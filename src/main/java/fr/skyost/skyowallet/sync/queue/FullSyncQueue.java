package fr.skyost.skyowallet.sync.queue;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;

import fr.skyost.skyowallet.economy.EconomyObject;
import fr.skyost.skyowallet.economy.SkyowalletFactory;
import fr.skyost.skyowallet.economy.SkyowalletManager;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.sync.SyncManager;
import fr.skyost.skyowallet.sync.synchronizer.SkyowalletAccountSynchronizer;
import fr.skyost.skyowallet.sync.synchronizer.SkyowalletBankSynchronizer;
import fr.skyost.skyowallet.sync.synchronizer.SkyowalletSynchronizer;

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

		addToQueue(syncManager.getSkyowallet().getAccountManager().list());
		addToQueue(syncManager.getSkyowallet().getBankManager().list());
	}

	@Override
	SkyowalletAccountSynchronizer createAccountSynchronizer() {
		return new SkyowalletAccountSynchronizer(getSyncManager().getSkyowallet()) {

			@Override
			public void loadObjectsFromFiles(final HashMap<String, SkyowalletAccount> queue) throws IOException {
				super.loadObjectsFromFiles(queue);
				loadNewObjectsFromDirectory(this);
			}

			@Override
			public void syncObjectsWithMySQL(final SyncManager syncManager, final HashMap<String, SkyowalletAccount> queue) throws SQLException {
				super.syncObjectsWithMySQL(syncManager, queue);

				if(getSyncManager().getSkyowallet().getPluginConfig().mySQLEnable) {
					loadNewObjectsFromMySQL(this, SyncManager.MYSQL_SELECT_ACCOUNTS);
				}
			}

		};
	}

	@Override
	SkyowalletBankSynchronizer createBankSynchronizer() {
		return new SkyowalletBankSynchronizer(getSyncManager().getSkyowallet()) {

			@Override
			public void loadObjectsFromFiles(final HashMap<String, SkyowalletBank> queue) throws IOException {
				super.loadObjectsFromFiles(queue);
				loadNewObjectsFromDirectory(this);
			}

			@Override
			public void syncObjectsWithMySQL(final SyncManager syncManager, final HashMap<String, SkyowalletBank> queue) throws SQLException {
				super.syncObjectsWithMySQL(syncManager, queue);

				if(getSyncManager().getSkyowallet().getPluginConfig().mySQLEnable) {
					loadNewObjectsFromMySQL(this, SyncManager.MYSQL_SELECT_BANKS);
				}
			}

		};
	}

	@Override
	String getStartMessage() {
		return ChatColor.GOLD + "Full synchronization started...";
	}

	/**
	 * Loads all new objects from the directory.
	 *
	 * @param synchronizer The synchronizer.
	 * @param <T> The type of objects to load.
	 *
	 * @throws IOException If any I/O exception occurs.
	 */

	private <T extends EconomyObject> void loadNewObjectsFromDirectory(final SkyowalletSynchronizer<T> synchronizer) throws IOException {
		final SkyowalletManager<T> manager = synchronizer.getManager();
		final SkyowalletFactory<T, ?> factory = synchronizer.getFactory();
		final File directory = synchronizer.getDirectory();

		for(final File file : directory.listFiles()) {
			if(!file.isFile() || manager.has(file.getName())) {
				continue;
			}

			final T object = factory.createFromJSON(file);
			manager.add(object);
			addToQueue(object);
		}
	}

	/**
	 * Loads all new objects from MySQL.
	 *
	 * @param synchronizer The synchronizer.
	 * @param selectQuery The query that allows to select the objects.
	 * @param <T> The type of objects to load.
	 *
	 * @throws SQLException If any SQL exception occurs.
	 */

	private <T extends EconomyObject> void loadNewObjectsFromMySQL(final SkyowalletSynchronizer<T> synchronizer, final String selectQuery) throws SQLException {
		final SyncManager syncManager = getSyncManager();
		final SkyowalletManager<T> manager = synchronizer.getManager();

		final Set<T> objects = syncManager.executeQuery(selectQuery, synchronizer.getResultSetHandler());
		for(final T mySQLObject : objects) {
			if(manager.has(mySQLObject.getIdentifier())) {
				continue;
			}
			manager.add(mySQLObject);
			addToQueue(mySQLObject);
		}
	}

}