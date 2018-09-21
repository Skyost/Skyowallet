package fr.skyost.skyowallet.event.sync;

import fr.skyost.skyowallet.sync.queue.SyncQueue;

/**
 * Represents an event triggered when a synchronization ends.
 */

public class SyncEndEvent extends SyncEvent {

	/**
	 * Creates a new synchronization end event instance
	 *
	 * @param syncQueue The synchronization queue.
	 */

	public SyncEndEvent(final SyncQueue syncQueue) {
		super(syncQueue);
	}

	/**
	 * Always returns <em>false</em>.
	 *
	 * @return <em>false</em>.
	 */

	@Deprecated
	@Override
	public final boolean isCancelled() {
		return false;
	}

	/**
	 * This event cannot be cancelled.
	 *
	 * @param isCancelled The isCancelled parameter.
	 */

	@Deprecated
	@Override
	public final void setCancelled(final boolean isCancelled) {}

}