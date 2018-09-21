package fr.skyost.skyowallet.sync;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.sync.queue.SyncQueue;

/**
 * The synchronize that allows to synchronize the plugin's data.
 */

public class SyncTask extends BukkitRunnable {

	/**
	 * The Skyowallet instance.
	 */

	private Skyowallet skyowallet;

	/**
	 * The synchronization queue.
	 */

	private SyncQueue syncQueue;

	/**
	 * Creates a new synchronization synchronize instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param syncQueue The synchronization queue.
	 */

	public SyncTask(final Skyowallet skyowallet, final SyncQueue syncQueue) {
		this.skyowallet = skyowallet;
		this.syncQueue = syncQueue;
	}

	/**
	 * Returns the Skyowallet instance.
	 *
	 * @return The Skyowallet instance.
	 */

	public final Skyowallet getSkyowallet() {
		return skyowallet;
	}

	/**
	 * Sets the Skyowallet instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public final void setSkyowallet(final Skyowallet skyowallet) {
		this.skyowallet = skyowallet;
	}

	/**
	 * Returns the synchronization queue.
	 *
	 * @return The synchronization queue.
	 */

	public final SyncQueue getSyncQueue() {
		return syncQueue;
	}

	/**
	 * Sets the synchronization queue.
	 *
	 * @param syncQueue The synchronization queue.
	 */

	public final void setSyncQueue(final SyncQueue syncQueue) {
		this.syncQueue = syncQueue;
	}

	@Override
	public final void run() {
		try {
			final SyncManager syncManager = skyowallet.getSyncManager();
			syncManager.synchronize(syncQueue);
		}
		catch(final Exception ex) {
			syncQueue.logMessage(ChatColor.DARK_RED + ex.getClass().getName());
			ex.printStackTrace();
		}
	}

	/**
	 * Runs a default synchronization.
	 *
	 * @param syncQueue The synchronization queue.
	 */

	public static void runDefaultSync(final SyncQueue syncQueue) {
		if(syncQueue == null) {
			return;
		}

		final Skyowallet skyowallet = Skyowallet.getInstance();
		runDefaultSync(new SyncTask(skyowallet, syncQueue));
	}

	/**
	 * Runs a default synchronization.
	 *
	 * @param syncTask The synchronization task.
	 */

	private static void runDefaultSync(final SyncTask syncTask) {
		syncTask.runTask(Skyowallet.getInstance());
	}

}