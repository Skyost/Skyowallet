package fr.skyost.skyowallet.event.sync;

import fr.skyost.skyowallet.event.SkyowalletEvent;
import fr.skyost.skyowallet.sync.queue.FullSyncQueue;
import fr.skyost.skyowallet.sync.queue.SyncQueue;

/**
 * Represents a synchronization event.
 */

public abstract class SyncEvent extends SkyowalletEvent {

	/**
	 * The synchronization queue.
	 */

	private final SyncQueue syncQueue;

	/**
	 * Creates a new synchronization event instance
	 *
	 * @param syncQueue The synchronization queue.
	 */

	protected SyncEvent(final SyncQueue syncQueue) {
		this.syncQueue = syncQueue;
	}

	/**
	 * Returns the synchronization queue.
	 *
	 * @return The synchronization queue.
	 */

	public SyncQueue getSyncQueue() {
		return syncQueue;
	}

	/**
	 * Returns whether we are running a full synchronization.
	 *
	 * @return Whether we are running a full synchronization.
	 */

	public boolean isFullSync() {
		return syncQueue instanceof FullSyncQueue;
	}

}
