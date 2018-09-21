package fr.skyost.skyowallet.sync.synchronizer;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import fr.skyost.skyowallet.economy.EconomyObject;
import fr.skyost.skyowallet.economy.SkyowalletFactory;
import fr.skyost.skyowallet.economy.SkyowalletManager;
import fr.skyost.skyowallet.sync.SyncManager;
import fr.skyost.skyowallet.sync.handler.SkyowalletResultSetHandler;

/**
 * Represents a synchronizer that allows to synchronize a certain type of objects.
 *
 * @param <T> The object type.
 */

public abstract class SkyowalletSynchronizer<T extends EconomyObject> {

	/**
	 * The local objects directory.
	 */

	private File directory;

	/**
	 * The identifier handler.
	 */

	private Function<T, String> identifierHandler;

	/**
	 * The SELECT query builder.
	 */

	private Function<Set<String>, String> selectQueryBuilder;

	/**
	 * The UPDATE query builder.
	 */

	private Function<T, MySQLQuery> updateQueryBuilder;

	/**
	 * The DELETE query builder.
	 */

	private Function<Set<String>, MySQLQuery> deleteQueryBuilder;

	/**
	 * The result set handler.
	 */

	private SkyowalletResultSetHandler<T> resultSetHandler;

	/**
	 * The manager.
	 */

	private SkyowalletManager<T> manager;

	/**
	 * The factory.
	 */

	private SkyowalletFactory<T, ?> factory;

	/**
	 * Creates a new Skyowallet synchronizer instance.
	 *
	 * @param directory The local objects directory.
	 * @param identifierHandler The identifier handler.
	 * @param selectQueryBuilder The SELECT query builder.
	 * @param updateQueryBuilder The UPDATE query builder.
	 * @param deleteQueryBuilder The DELETE query builder.
	 * @param resultSetHandler The result set handler.
	 * @param manager The manager.
	 * @param factory The factory.
	 */

	public SkyowalletSynchronizer(final File directory, final Function<T, String> identifierHandler, final Function<Set<String>, String> selectQueryBuilder, final Function<T, MySQLQuery> updateQueryBuilder, final Function<Set<String>, MySQLQuery> deleteQueryBuilder, final SkyowalletResultSetHandler<T> resultSetHandler, final SkyowalletManager<T> manager, final SkyowalletFactory<T, ?> factory) {
		this.directory = directory;
		this.identifierHandler = identifierHandler;
		this.selectQueryBuilder = selectQueryBuilder;
		this.updateQueryBuilder = updateQueryBuilder;
		this.deleteQueryBuilder = deleteQueryBuilder;
		this.resultSetHandler = resultSetHandler;
		this.manager = manager;
		this.factory = factory;
	}

	/**
	 * Synchronizes the specified queue object.
	 *
	 * @param syncManager The synchronization manager.
	 * @param queue The queue of objects.
	 *
	 * @throws IOException If any I/O exception occurs.
	 * @throws SQLException If any SQL exception occurs.
	 */

	public void synchronizeQueue(final SyncManager syncManager, final HashMap<String, T> queue) throws IOException, SQLException {
		loadObjectsFromFiles(queue);
		if(syncManager.getSkyowallet().getPluginConfig().mySQLEnable) {
			syncObjectsWithMySQL(syncManager, queue);
		}
		deleteRemovedObjects(syncManager, queue);
		saveObjectsToFiles(queue);
	}

	/**
	 * Loads objects from the files.
	 *
	 * @param queue The queue of objects.
	 *
	 * @throws IOException If any I/O exception occurs.
	 */

	protected void loadObjectsFromFiles(final HashMap<String, T> queue) throws IOException {
		for(final T object : queue.values()) {
			final File file = new File(directory, object.getIdentifier());
			if(file.isFile()) {
				final EconomyObject localObject = factory.createFromJSON(file);
				if(localObject.getLastModificationTime() > object.getLastModificationTime()) {
					object.applyFromObject(localObject);
				}
			}
			else {
				file.delete();
			}
		}
	}

	/**
	 * Synchronizes objects with MySQL.
	 *
	 * @param syncManager The synchronization manager.
	 * @param queue The queue of objects.
	 *
	 * @throws SQLException If any SQL exception occurs.
	 */

	protected void syncObjectsWithMySQL(final SyncManager syncManager, final HashMap<String, T> queue) throws SQLException {
		final HashMap<String, T> queueCopy = new HashMap<>(queue);

		final HashSet<String> whereClause = new HashSet<>();
		for(final T object : queue.values()) {
			whereClause.add(identifierHandler.apply(object));
		}

		if(whereClause.isEmpty()) {
			return;
		}

		final Set<T> objects = syncManager.executeQuery(selectQueryBuilder.apply(whereClause), resultSetHandler);
		for(final T mySQLObject : objects) {
			final T memoryObject = queue.get(mySQLObject.getIdentifier());
			queueCopy.remove(memoryObject.getIdentifier());
			if(memoryObject.getLastModificationTime() == mySQLObject.getLastModificationTime()) {
				continue;
			}

			if(memoryObject.getLastModificationTime() < mySQLObject.getLastModificationTime()) {
				memoryObject.applyFromObject(mySQLObject);
			}
			else {
				final MySQLQuery update = updateQueryBuilder.apply(memoryObject);
				syncManager.executeUpdate(update.query, update.arguments);
			}
		}

		for(final T object : queueCopy.values()) {
			final MySQLQuery update = updateQueryBuilder.apply(object);
			syncManager.executeUpdate(update.query, update.arguments);
		}
	}

	/**
	 * Deletes the removed objects.
	 *
	 * @param syncManager The synchronization manager.
	 * @param queue The queue of objects.
	 *
	 * @throws SQLException If any SQL exception occurs.
	 */

	protected void deleteRemovedObjects(final SyncManager syncManager, final HashMap<String, T> queue) throws SQLException {
		final HashSet<String> whereClause = new HashSet<>();
		for(final T object : queue.values()) {
			if(!object.isDeleted()) {
				continue;
			}

			whereClause.add(identifierHandler.apply(object));
			manager.getData().remove(object.getIdentifier());
			queue.remove(object);

			final File accountFile = new File(directory, object.getIdentifier());
			if(accountFile.exists() && accountFile.isFile()) {
				accountFile.delete();
			}
		}

		if(!syncManager.getSkyowallet().getPluginConfig().mySQLEnable || whereClause.isEmpty()) {
			return;
		}

		final MySQLQuery delete = deleteQueryBuilder.apply(whereClause);
		syncManager.executeUpdate(delete.query, delete.arguments);
	}

	/**
	 * Saves objects to the files.
	 *
	 * @param queue The objects queue.
	 *
	 * @throws IOException If any I/O exception occurs.
	 */

	protected void saveObjectsToFiles(final HashMap<String, T> queue) throws IOException {
		for(final T object : queue.values()) {
			final File file = new File(directory, object.getIdentifier());
			Files.write(object.toString(), file, StandardCharsets.UTF_8);
		}
	}

	/**
	 * Returns the directory.
	 *
	 * @return The directory.
	 */

	public File getDirectory() {
		return directory;
	}

	/**
	 * Sets the directory.
	 *
	 * @param directory The directory.
	 */

	public void setDirectory(final File directory) {
		this.directory = directory;
	}

	/**
	 * Returns the identifier handler.
	 *
	 * @return The identifier handler.
	 */

	public Function<T, String> getIdentifierHandler() {
		return identifierHandler;
	}

	/**
	 * Sets the identifier handler.
	 *
	 * @param identifierHandler The identifier handler.
	 */

	public void setIdentifierHandler(final Function<T, String> identifierHandler) {
		this.identifierHandler = identifierHandler;
	}

	/**
	 * Returns the SELECT query builder.
	 *
	 * @return The SELECT query builder.
	 */

	public Function<Set<String>, String> getSelectQueryBuilder() {
		return selectQueryBuilder;
	}

	/**
	 * Sets the SELECT query builder.
	 *
	 * @param selectQueryBuilder The SELECT query builder.
	 */

	public void setSelectQueryBuilder(final Function<Set<String>, String> selectQueryBuilder) {
		this.selectQueryBuilder = selectQueryBuilder;
	}

	/**
	 * Returns the UPDATE query builder.
	 *
	 * @return The UPDATE query builder.
	 */

	public Function<T, MySQLQuery> getUpdateQueryBuilder() {
		return updateQueryBuilder;
	}

	/**
	 * Sets the UPDATE query builder.
	 *
	 * @param updateQueryBuilder The UPDATE query builder.
	 */

	public void setUpdateQueryBuilder(final Function<T, MySQLQuery> updateQueryBuilder) {
		this.updateQueryBuilder = updateQueryBuilder;
	}

	/**
	 * Returns the DELETE query builder.
	 *
	 * @return The DELETE query builder.
	 */

	public Function<Set<String>, MySQLQuery> getDeleteQueryBuilder() {
		return deleteQueryBuilder;
	}

	/**
	 * Sets the DELETE query builder.
	 *
	 * @param deleteQueryBuilder The DELETE query builder.
	 */

	public void setDeleteQueryBuilder(final Function<Set<String>, MySQLQuery> deleteQueryBuilder) {
		this.deleteQueryBuilder = deleteQueryBuilder;
	}

	/**
	 * Returns the result set handler.
	 *
	 * @return The result set handler.
	 */

	public SkyowalletResultSetHandler<T> getResultSetHandler() {
		return resultSetHandler;
	}

	/**
	 * Sets the result set handler.
	 *
	 * @param resultSetHandler The result set handler.
	 */

	public void setResultSetHandler(final SkyowalletResultSetHandler<T> resultSetHandler) {
		this.resultSetHandler = resultSetHandler;
	}

	/**
	 * Returns the manager.
	 *
	 * @return The manager.
	 */

	public SkyowalletManager<T> getManager() {
		return manager;
	}

	/**
	 * Sets the manager.
	 *
	 * @param manager The manager.
	 */

	public void setManager(final SkyowalletManager<T> manager) {
		this.manager = manager;
	}

	/**
	 * Returns the factory.
	 *
	 * @return The factory.
	 */

	public SkyowalletFactory<T, ?> getFactory() {
		return factory;
	}

	/**
	 * Sets the factory.
	 *
	 * @param factory The factory.
	 */

	public void setFactory(final SkyowalletFactory<T, ?> factory) {
		this.factory = factory;
	}

	/**
	 * Represents a MySQL query with arguments.
	 */

	public static class MySQLQuery {

		/**
		 * The query.
		 */

		private final String query;

		/**
		 * The arguments.
		 */

		private final Object[] arguments;

		/**
		 * Creates a new MySQL query instance.
		 *
		 * @param query The query.
		 * @param arguments The arguments.
		 */

		public MySQLQuery(final String query, final Object... arguments) {
			this.query = query;
			this.arguments = arguments;
		}

		/**
		 * Returns the query.
		 *
		 * @return The query.
		 */

		public final String getQuery() {
			return query;
		}

		/**
		 * Returns the arguments.
		 *
		 * @return The arguments.
		 */

		public final Object[] getArguments() {
			return arguments;
		}

	}

}
