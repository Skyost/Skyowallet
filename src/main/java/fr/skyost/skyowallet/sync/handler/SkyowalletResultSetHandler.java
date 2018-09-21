package fr.skyost.skyowallet.sync.handler;

import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import fr.skyost.skyowallet.economy.EconomyObject;
import fr.skyost.skyowallet.sync.SyncManager;

/**
 * Represents a result set handler that depends on a synchronization manager.
 *
 * @param <T> The return type.
 */

public abstract class SkyowalletResultSetHandler<T extends EconomyObject> implements ResultSetHandler<Set<T>> {

	/**
	 * The synchronization manager.
	 */

	private SyncManager syncManager;

	/**
	 * Creates a new Skyowallet result set handler instance.
	 *
	 * @param syncManager The synchronization manager.
	 */

	public SkyowalletResultSetHandler(final SyncManager syncManager) {
		setSyncManager(syncManager);
	}

	/**
	 * Returns the synchronization manager.
	 *
	 * @return The synchronization manager.
	 */

	public SyncManager getSyncManager() {
		return syncManager;
	}

	/**
	 * Sets the synchronization manager.
	 *
	 * @param syncManager The synchronization manager.
	 */

	public void setSyncManager(final SyncManager syncManager) {
		this.syncManager = syncManager;
	}

	@Override
	public final Set<T> handle(final ResultSet resultSet) throws SQLException {
		final HashSet<T> result = new HashSet<>();
		while(resultSet.next()) {
			result.add(handleLine(resultSet));
		}

		return result;
	}

	/**
	 * Handles a single line of a result set.
	 *
	 * @param resultSet The result set.
	 *
	 * @return The handled object.
	 *
	 * @throws SQLException If any SQL error occurs.
	 */

	public abstract T handleLine(final ResultSet resultSet) throws SQLException;

}
