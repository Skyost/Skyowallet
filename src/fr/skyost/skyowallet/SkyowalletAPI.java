package fr.skyost.skyowallet;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import fr.skyost.skyowallet.tasks.SyncTask;
import fr.skyost.skyowallet.utils.Utils;

/**
 * SkyowalletAPI.
 * 
 * @author <a href="http://www.skyost.eu"><b>Skyost</b></a>.
 */

public class SkyowalletAPI {
	
	private static final String MYSQL_TABLE = "skyowallet_accounts_v2";
	
	private static final HashMap<String, SkyowalletAccount> accounts = new HashMap<String, SkyowalletAccount>();
	protected static Statement statement;
	
	/**
	 * Gets the currency name for numbers inferior than 2.
	 * 
	 * @return The currency name.
	 */
	
	public static final String getCurrencyNameSingular() {
		return Skyowallet.config.currencyNameSingular;
	}
	
	/**
	 * Gets the currency name for numbers superior than 2.
	 * 
	 * @return The currency name.
	 */
	
	public static final String getCurrencyNamePlural() {
		return Skyowallet.config.currencyNamePlural;
	}
	
	/**
	 * Gets the currency name for the specified number.
	 * 
	 * @param amount The number.
	 * 
	 * @return The currency name.
	 */
	
	public static final String getCurrencyName(final double amount) {
		return amount < 2 ? Skyowallet.config.currencyNameSingular : Skyowallet.config.currencyNamePlural;
	}
	
	/**
	 * Gets the accounts directory (where the plugin stores the accounts).
	 * <br>NOTE : if the directory does not exist, this method will try to create it.
	 * 
	 * @return The accounts directory.
	 */
	
	public static final File getAccountsDirectory() {
		final File accountsDir = new File(Skyowallet.config.accountsDir);
		if(!accountsDir.exists()) {
			accountsDir.mkdir();
		}
		return accountsDir;
	}
	
	/**
	 * Gets the accounts directory's name. For loops usage.
	 * 
	 * @return The accounts directory's name.
	 */
	
	public static final String getAccountsDirectoryName() {
		return Skyowallet.config.accountsDir;
	}
	
	/**
	 * Checks if the specified player has an account.
	 * 
	 * @param player The player.
	 * 
	 * @return <b>true</b> If the player has an account.
	 * <br><b>false</b> Otherwise.
	 */
	
	public static final boolean hasAccount(final OfflinePlayer player) {
		if(!player.hasPlayedBefore()) {
			return false;
		}
		return hasAccount(player.getUniqueId().toString());
	}
	
	/**
	 * Checks if the specified UUID has an account.
	 * 
	 * @param uuid The UUID.
	 * 
	 * @return <b>true</b> If the UUID has an account.
	 * <br><b>false</b> Otherwise.
	 */
	
	public static final boolean hasAccount(final String uuid) {
		return accounts.containsKey(uuid);
	}
	
	/**
	 * Gets the player's account.
	 * 
	 * @param player The player.
	 * 
	 * @return The account of the specified player if found (null otherwise).
	 */
	
	public static final SkyowalletAccount getAccount(final OfflinePlayer player) {
		return getAccount(player.getUniqueId().toString());
	}
	
	/**
	 * Gets the account of the UUID.
	 * 
	 * @param uuid The UUID.
	 * 
	 * @return The account if found (null otherwise).
	 */
	
	public static final SkyowalletAccount getAccount(final String uuid) {
		return accounts.get(uuid);
	}
	
	/**
	 * Gets all accounts.
	 * 
	 * @return All accounts.
	 */
	
	public static final SkyowalletAccount[] getAccounts() {
		final Collection<SkyowalletAccount> accounts = SkyowalletAPI.accounts.values();
		return accounts.toArray(new SkyowalletAccount[accounts.size()]);
	}
	
	/**
	 * Register a new account for the specified UUID.
	 * 
	 * @param uuid The UUID.
	 * 
	 * @return The new account.
	 */
	
	public static final SkyowalletAccount registerAccount(final String uuid) {
		final SkyowalletAccount account = new SkyowalletAccount(uuid);
		accounts.put(uuid, account);
		return account;
	}
	
	/**
	 * Sync Database with Memory.
	 * <br>NOTE : files are created after the sync is done. So, if you manually create files, they will not be loaded.
	 * <br>Thanks to http://stackoverflow.com/a/10951183/3608831.
	 * 
	 * @param sender Used to send informations, you can obtain the console with <b>Bukkit.getConsoleSender()</b>.
	 */
	
	public static final void sync(final CommandSender sender) {
		final String prefix = sender instanceof Player ? "" : "[Skyowallet] ";
		sender.sendMessage(prefix + ChatColor.GOLD + "Synchronization started...");
		if(accounts.size() == 0) {
			try {
				sender.sendMessage(prefix + ChatColor.AQUA + "Loading accounts...");
				final File accountsDir = getAccountsDirectory();
				for(final File localAccount : accountsDir.listFiles()) {
					final SkyowalletAccount account = SkyowalletAccount.fromJson(Utils.getFileContent(localAccount, null));
					accounts.put(account.getUUID(), account);
				}
				sender.sendMessage(prefix + ChatColor.GREEN + "Accounts loaded...");
			}
			catch(final Exception ex) {
				ex.printStackTrace();
				sender.sendMessage(prefix + ChatColor.RED + "Failed to load accounts.");
			}
		}
		if(Skyowallet.config.mySQLEnable) {
			try {
				sender.sendMessage(prefix + ChatColor.AQUA + "Synchronization with the MySQL database...");
				if(statement == null) {
					sender.sendMessage(prefix + ChatColor.AQUA + "Logging in to the specified MySQL server...");
					statement = DriverManager.getConnection("jdbc:mysql://" + Skyowallet.config.mySQLHost + ":" + Skyowallet.config.mySQLPort + "/" + Skyowallet.config.mySQLDB, Skyowallet.config.mySQLUser, Skyowallet.config.mySQLPassword).createStatement();
					statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + MYSQL_TABLE + " (UUID BINARY(16) NOT NULL, Wallet DOUBLE NOT NULL, LastModificationTime BIGINT NOT NULL, PRIMARY KEY (UUID))");
					sender.sendMessage(prefix + ChatColor.GREEN + "Done !");
				}
				final HashMap<String, SkyowalletAccount> remoteAccounts = new HashMap<String, SkyowalletAccount>();
				final ResultSet result = statement.executeQuery("SELECT HEX(UUID) AS UUID, Wallet, LastModificationTime FROM " + MYSQL_TABLE);
				while(result.next()) {
					final String uuid = result.getString("UUID");
					final Double wallet = result.getDouble("Wallet");
					final Long lastModificationTime = result.getLong("LastModificationTime");
					if(uuid == null || wallet == null || lastModificationTime == null) {
						continue;
					}
					remoteAccounts.put(uuid, new SkyowalletAccount(UUID.fromString(Utils.uuidAddDashes(uuid)).toString(), wallet, lastModificationTime));
				}
				final List<SkyowalletAccount> toSync = new ArrayList<SkyowalletAccount>();
				for(final SkyowalletAccount account : remoteAccounts.values()) {
					final String uuid = account.getUUID();
					final SkyowalletAccount localAccount = accounts.get(uuid);
					if(localAccount == null || localAccount.getLastModificationTime() < account.getLastModificationTime()) {
						accounts.put(uuid, account);
					}
				}
				for(final SkyowalletAccount account : accounts.values()) {
					final String uuid = account.getUUID();
					final SkyowalletAccount remoteAccount = remoteAccounts.get(uuid);
					if(remoteAccount == null || remoteAccount.getLastModificationTime() < account.getLastModificationTime()) {
						toSync.add(account);
					}
				}
				for(final SkyowalletAccount account : toSync) {
					final double wallet = account.getWallet();
					final long lastModificationTime = account.getLastModificationTime();
					statement.executeUpdate("INSERT INTO " + MYSQL_TABLE + "(UUID, Wallet, LastModificationTime) VALUES(UNHEX('" + account.getUUID().replace("-", "") + "'), " + wallet + ", " + lastModificationTime + ") ON DUPLICATE KEY UPDATE Wallet=" + wallet + ", LastModificationTime=" + lastModificationTime);
				}
				sender.sendMessage(prefix + ChatColor.GREEN + "Successfully synchronized MySQL database.");
			}
			catch(final Exception ex) {
				ex.printStackTrace();
				sender.sendMessage(prefix + ChatColor.RED + "Error in a MySQL statement !");
			}
		}
		try {
			sender.sendMessage(prefix + ChatColor.AQUA + "Saving accounts...");
			for(final SkyowalletAccount account : accounts.values()) {
				final String uuid = account.getUUID();
				Utils.writeToFile(new File(getAccountsDirectoryName(), uuid), account.toString());
			}
			sender.sendMessage(prefix + ChatColor.GREEN + "Accounts saved with success.");
		}
		catch(final Exception ex) {
			sender.sendMessage(prefix + ChatColor.RED + "Failed to save accounts !");
		}
		sender.sendMessage(prefix + ChatColor.GOLD + "Synchronization finished.");
	}
	
	/**
	 * Used to manage players' accounts.
	 */
	
	public static class SkyowalletAccount {
		
		private final String uuid;
		private double wallet;
		private long lastModificationTime;
		
		/**
		 * Constructs a new Skyowallet's account.
		 * 
		 * @param uuid The uuid.
		 */
		
		public SkyowalletAccount(final String uuid) {
			this(uuid, 0.0, Utils.getCurrentTimeInMillis());
		}
		
		/**
		 * Private constructor used to sync accounts.
		 * 
		 * @param uuid The uuid.
		 * @param wallet The account's wallet.
		 * @param lastModificationTime The last modification time of the specified account.
		 */
		
		private SkyowalletAccount(final String uuid, final double wallet, final long lastModificationTime) {
			this.uuid = uuid;
			this.wallet = wallet;
			this.lastModificationTime = lastModificationTime;
		}
		
		/**
		 * Gets the UUID (as String).
		 * 
		 * @return The UUID.
		 */
		
		public final String getUUID() {
			return uuid;
		}
		
		/**
		 * Gets the wallet.
		 * 
		 * @return The wallet.
		 */
		
		public final double getWallet() {
			return wallet;
		}
		
		/**
		 * Sets the wallet. The database will be sync if the user has enabled "sync-each-modification" in the config.
		 * <br>NOTE : the last modification time field will be updated too.
		 * 
		 * @param wallet The wallet.
		 */
		
		public final void setWallet(final double wallet) {
			setWallet(wallet, Skyowallet.config.syncEachModification);
		}
		
		/**
		 * Sets the wallet.
		 * 
		 * @param wallet The wallet.
		 * @param sync If you want to sync the database (asynchronously).
		 */
		
		public final void setWallet(final double wallet, final boolean sync) {
			this.wallet = wallet;
			lastModificationTime = Utils.getCurrentTimeInMillis();
			if(sync) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("Skyowallet"), new SyncTask());
			}
		}
		
		/**
		 * Gets the last modification time in millis.
		 * 
		 * @return The last modification time.
		 */
		
		public final long getLastModificationTime() {
			return lastModificationTime;
		}
		
		@Override
		public final String toString() {
			final JSONObject json = new JSONObject();
			json.put("uuid", uuid);
			json.put("wallet", wallet);
			json.put("lastModificationTime", lastModificationTime);
			return json.toJSONString();
		}
		
		/**
		 * Constructs an instance from a Json String.
		 * 
		 * @param json The Json String.
		 * 
		 * @return A new instance of this class.
		 */
		
		public static final SkyowalletAccount fromJson(final String json) {
			final JSONObject array = (JSONObject)JSONValue.parse(json);
			final String uuid = array.get("uuid").toString();
			final Long lastModificationTime = Long.valueOf(array.get("lastModificationTime").toString());
			if(uuid == null || lastModificationTime == null) {
				throw new IllegalArgumentException("UUID / Last Modification Timme nnot be null.");
			}
			final Double wallet = Double.parseDouble(array.get("wallet").toString());
			return new SkyowalletAccount(uuid, wallet == null ? 0.0 : wallet, lastModificationTime);
		}
		
	}
	
}