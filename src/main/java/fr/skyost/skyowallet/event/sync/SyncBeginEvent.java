package fr.skyost.skyowallet.event.sync;

import fr.skyost.skyowallet.sync.queue.SyncQueue;

/**
 * Represents an event triggered when a synchronization begins.
 */

public class SyncBeginEvent extends SyncEvent {

	/**
	 * Creates a new synchronization begin event instance
	 *
	 * @param syncQueue The synchronization queue.
	 */

	public SyncBeginEvent(final SyncQueue syncQueue) {
		super(syncQueue);
	}

}