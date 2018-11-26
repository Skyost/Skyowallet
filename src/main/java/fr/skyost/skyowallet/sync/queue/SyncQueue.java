package fr.skyost.skyowallet.sync.queue;

import fr.skyost.skyowallet.economy.EconomyObject;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.event.sync.SyncBeginEvent;
import fr.skyost.skyowallet.event.sync.SyncEndEvent;
import fr.skyost.skyowallet.sync.SyncManager;
import fr.skyost.skyowallet.sync.synchronizer.SkyowalletAccountSynchronizer;
import fr.skyost.skyowallet.sync.synchronizer.SkyowalletBankSynchronizer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Represents a synchronization queue.
 */

public class SyncQueue implements Iterable<EconomyObject> {

	/**
	 * Accounts queue.
	 */

	private final HashMap<String, SkyowalletAccount> accountsQueue = new HashMap<>();

	/**
	 * Banks queue.
	 */

	private final HashMap<String, SkyowalletBank> banksQueue = new HashMap<>();

	/**
	 * The synchronization manager.
	 */

	private SyncManager syncManager;

	/**
	 * The command sender.
	 */

	private CommandSender sender;

	/**
	 * Creates a new synchronization queue instance.
	 *
	 * @param syncManager The synchronization manager.
	 * @param sender The command sender.
	 */

	public SyncQueue(final SyncManager syncManager, final CommandSender sender) {
		this.syncManager = syncManager;
		this.sender = sender;
	}

	/**
	 * Adds the specified objects to the queue.
	 *
	 * @param objects The objects.
	 * @param <T> The economy object class.
	 */

	@SafeVarargs
	public final <T extends EconomyObject> void addToQueue(final T... objects) {
		addToQueue(Arrays.asList(objects));
	}

	/**
	 * Adds the specified objects to the queue.
	 *
	 * @param objects The objects.
	 * @param <T> The economy object class.
	 */

	public <T extends EconomyObject> void addToQueue(final Collection<T> objects) {
		for(final T object : objects) {
			if(object == null) {
				continue;
			}

			if(object instanceof SkyowalletAccount) {
				accountsQueue.put(object.getIdentifier(), (SkyowalletAccount)object);
				continue;
			}

			banksQueue.put(object.getIdentifier(), (SkyowalletBank)object);
		}
	}

	/**
	 * Removes the specified objects from the queue.
	 *
	 * @param objects The objects.
	 * @param <T> The economy object class.
	 */

	@SafeVarargs
	public final <T extends EconomyObject> void removeFromQueue(final T... objects) {
		removeFromQueue(Arrays.asList(objects));
	}

	/**
	 * Removes the specified objects from the queue.
	 *
	 * @param objects The objects.
	 * @param <T> The economy object class.
	 */

	public <T extends EconomyObject> void removeFromQueue(final Collection<T> objects) {
		for(final T object : objects) {
			if(object instanceof SkyowalletAccount) {
				accountsQueue.remove(object.getIdentifier());
				continue;
			}

			banksQueue.remove(object.getIdentifier());
		}
	}

	/**
	 * Clears the queue.
	 */

	public void clearQueue() {
		accountsQueue.clear();
		banksQueue.clear();
	}

	/**
	 * Synchronizes the queue.
	 *
	 * @throws IOException If any I/O exception occurs.
	 * @throws SQLException If any SQL exception occurs.
	 */

	public void synchronize() throws SQLException, IOException {
		logMessage(getStartMessage());

		final SyncBeginEvent syncBeginEvent = new SyncBeginEvent(this);
		Bukkit.getPluginManager().callEvent(syncBeginEvent);
		if(syncBeginEvent.isCancelled()) {
			logMessage(ChatColor.DARK_RED + "Synchronization cancelled !");
			return;
		}

		if(size() > 0) {
			createBankSynchronizer().synchronizeQueue(syncManager, banksQueue);
			createAccountSynchronizer().synchronizeQueue(syncManager, accountsQueue);
		}

		logMessage(ChatColor.GOLD + "Synchronization finished.");
		Bukkit.getPluginManager().callEvent(new SyncEndEvent(this));
	}

	/**
	 * Logs a message to the command sender.
	 *
	 * @param message The message.
	 */

	public void logMessage(final String message) {
		if(sender != null) {
			final String prefix = sender instanceof Player ? "" : "[" + syncManager.getSkyowallet().getName() + "] ";
			sender.sendMessage(prefix + message);
		}
	}

	/**
	 * Returns the message which tells the sender that te synchronization has started.
	 *
	 * @return The message which tells the sender that te synchronization has started.
	 */

	String getStartMessage() {
		return ChatColor.GOLD + "Synchronizing " + (accountsQueue.size() + banksQueue.size()) + " object(s)...";
	}

	/**
	 * Creates a new account synchronizer instance.
	 *
	 * @return The account synchronizer instance.
	 */

	SkyowalletAccountSynchronizer createAccountSynchronizer() {
		return new SkyowalletAccountSynchronizer(getSyncManager().getSkyowallet());
	}

	/**
	 * Creates a new bank synchronizer instance.
	 *
	 * @return The bank synchronizer instance.
	 */

	SkyowalletBankSynchronizer createBankSynchronizer() {
		return new SkyowalletBankSynchronizer(getSyncManager().getSkyowallet());
	}

	/**
	 * Returns the synchronization manager.
	 *
	 * @return The synchronization manager.
	 */

	public final SyncManager getSyncManager() {
		return syncManager;
	}

	/**
	 * Sets the synchronization manager.
	 *
	 * @param syncManager The synchronization manager.
	 */

	public final void setSyncManager(final SyncManager syncManager) {
		this.syncManager = syncManager;
	}

	/**
	 * Returns the command sender.
	 *
	 * @return The command sender.
	 */

	public final CommandSender getSender() {
		return sender;
	}

	/**
	 * Sets the command sender.
	 *
	 * @param sender The command sender.
	 */

	public final void setSender(final CommandSender sender) {
		this.sender = sender;
	}

	/**
	 * Returns the queue size.
	 *
	 * @return The queue size.
	 */

	public int size() {
		return accountsQueue.size() + banksQueue.size();
	}

	@Override
	public Iterator<EconomyObject> iterator() {
		final Iterator<SkyowalletAccount> accountsIterator = accountsQueue.values().iterator();
		final Iterator<SkyowalletBank> banksIterator = banksQueue.values().iterator();
		return new Iterator<EconomyObject>() {

			@Override
			public boolean hasNext() {
				return accountsIterator.hasNext() || banksIterator.hasNext();
			}

			@Override
			public EconomyObject next() {
				return accountsIterator.hasNext() ? accountsIterator.next() : banksIterator.next();
			}

		};
	}

}