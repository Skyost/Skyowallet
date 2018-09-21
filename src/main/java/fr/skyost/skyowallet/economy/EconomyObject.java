package fr.skyost.skyowallet.economy;

import org.json.simple.JSONObject;

import fr.skyost.skyowallet.Skyowallet;

/**
 * Represents an economy object (bank / account).
 */

public abstract class EconomyObject {

	/**
	 * The Skyowallet instance.
	 */

	private final Skyowallet skyowallet;

	/**
	 * Whether this object will be deleted at the next synchronization.
	 */

	protected boolean isDeleted;

	/**
	 * The object last modification time.
	 */
	
	protected long lastModificationTime;
	
	/**
	 * Creates a new instance of this object.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	protected EconomyObject(final Skyowallet skyowallet) {
		this(skyowallet, false, System.currentTimeMillis());
	}

	/**
	 * Creates a new instance of this object.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param json A JSON string.
	 */

	protected EconomyObject(final Skyowallet skyowallet, final String json) {
		this.skyowallet = skyowallet;
		fromJSON(json);
	}
	
	/**
	 * Creates a new instance of this object.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param isDeleted Whether this object will be deleted at the next synchronization.
	 * @param lastModificationTime The last modification time of the specified object.
	 */

	protected EconomyObject(final Skyowallet skyowallet, final boolean isDeleted, final long lastModificationTime) {
		this.skyowallet = skyowallet;
		this.isDeleted = isDeleted;
		this.lastModificationTime = lastModificationTime;
	}
	
	/**
	 * Returns the identifier of this class.
	 * 
	 * @return The identifier of this class.
	 */
	
	public abstract String getIdentifier();

	/**
	 * Returns the Skyowallet instance.
	 *
	 * @return The Skyowallet instance.
	 */

	public final Skyowallet getSkyowallet() {
		return skyowallet;
	}

	/**
	 * Returns whether this object will be deleted at the next synchronization.
	 *
	 * @return Whether this object will be deleted at the next synchronization.
	 */

	public boolean isDeleted() {
		return isDeleted;
	}

	/**
	 * Sets whether this object will be deleted at the next synchronization.
	 *
	 * @param isDeleted Whether this object will be deleted at the next synchronization.
	 */

	public void setDeleted(final boolean isDeleted) {
		this.isDeleted = isDeleted;
		updateLastModificationTime();
	}

	/**
	 * Returns the last modification time in millis of this object.
	 *
	 * @return The last modification time.
	 */

	public final long getLastModificationTime() {
		return lastModificationTime;
	}

	/**
	 * Sets the last modification time.
	 *
	 * @param lastModificationTime The last modification time.
	 */

	protected void setLastModificationTime(final long lastModificationTime) {
		this.lastModificationTime = lastModificationTime;
	}

	/**
	 * Updates the last modification time.
	 */
	
	public void updateLastModificationTime() {
		setLastModificationTime(System.currentTimeMillis());
		skyowallet.getSyncManager().getMainSyncQueue().addToQueue(this);
	}

	/**
	 * Applies all fields from the specified object.
	 *
	 * @param object The object.
	 */

	public void applyFromObject(final EconomyObject object) {
		isDeleted = object.isDeleted;
		lastModificationTime = object.lastModificationTime;
	}

	/**
	 * Applies all fields from a JSON string.
	 *
	 * @param json The JSON.
	 */

	public abstract void fromJSON(final String json);

	/**
	 * Returns the JSON object.
	 *
	 * @return The JSON object.
	 */

	public JSONObject toJSON() {
		final JSONObject object = new JSONObject();
		object.put("isDeleted", isDeleted);
		object.put("lastModificationTime", lastModificationTime);
		return object;
	}
	
	@Override
	public final String toString() {
		return toJSON().toJSONString();
	}
	
}