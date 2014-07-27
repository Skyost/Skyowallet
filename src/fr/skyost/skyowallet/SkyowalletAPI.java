package fr.skyost.skyowallet;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import fr.skyost.skyowallet.tasks.SyncTask;
import fr.skyost.skyowallet.utils.Utils;

/**
 * SkyowalletAPI.
 * 
 * @author <a href="http://www.skyost.eu"><b>Skyost</b></a>.
 */

public class SkyowalletAPI {
	
	private static final Plugin PLUGIN = Bukkit.getPluginManager().getPlugin("Skyowallet");
	private static final String MYSQL_TABLE_ACCOUNTS = "skyowallet_accounts_v3";
	
	protected static Statement statement;
	
	private static final HashMap<UUID, SkyowalletAccount> accounts = new HashMap<UUID, SkyowalletAccount>();
	private static final HashMap<String, SkyowalletBank> banks = new HashMap<String, SkyowalletBank>();
	
	public static final Skyowallet getPlugin() {
		return (Skyowallet)PLUGIN;
	}
	
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
	 * <br><b>NOTE :</b> if the directory does not exist, this method will try to create it.
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
	 * Gets the banks directory (where the plugin stores the banks).
	 * <br><b>NOTE :</b> if the directory does not exist, this method will try to create it.
	 * 
	 * @return The banks directory.
	 */
	
	public static final File getBanksDirectory() {
		final File banksDir = new File(Skyowallet.config.banksDir);
		if(!banksDir.exists()) {
			banksDir.mkdir();
		}
		return banksDir;
	}
	
	/**
	 * Gets the extensions directory (where the plugin stores its extensions' config).
	 * <br><b>NOTE :</b> if the directory does not exist, this method will try to create it.
	 * 
	 * @return The extensions directory.
	 */
	
	public static final File getExtensionsDirectory() {
		final File extensionsDir = new File(Skyowallet.config.extensionsDir);
		if(!extensionsDir.exists()) {
			extensionsDir.mkdir();
		}
		return extensionsDir;
	}
	
	/**
	 * Gets the accounts directory's name. Used in loops.
	 * 
	 * @return The accounts directory's name.
	 */
	
	public static final String getAccountsDirectoryName() {
		return Skyowallet.config.accountsDir;
	}
	
	/**
	 * Gets the banks directory's name. Used in loops.
	 * 
	 * @return The banks directory's name.
	 */
	
	public static final String getBanksDirectoryName() {
		return Skyowallet.config.banksDir;
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
		return hasAccount(player.getUniqueId());
	}
	
	/**
	 * Checks if the specified UUID has an account.
	 * 
	 * @param uuid The UUID.
	 * 
	 * @return <b>true</b> If the UUID has an account.
	 * <br><b>false</b> Otherwise.
	 */
	
	public static final boolean hasAccount(final UUID uuid) {
		return accounts.containsKey(uuid);
	}
	
	/**
	 * Checks if the specified bank exists.
	 * 
	 * @param name The bank's name.
	 * 
	 * @return <b>true</b> If the bank exists.
	 * <br><b>false</b> Otherwise.
	 */
	
	public static final boolean isBankExists(final String name) {
		return banks.containsKey(name);
	}
	
	/**
	 * Gets the player's account.
	 * 
	 * @param player The player.
	 * 
	 * @return The account of the specified player if found (null otherwise).
	 */
	
	public static final SkyowalletAccount getAccount(final OfflinePlayer player) {
		return getAccount(player.getUniqueId());
	}
	
	/**
	 * Gets an account by its UUID.
	 * 
	 * @param uuid The UUID.
	 * 
	 * @return The account if found (null otherwise).
	 */
	
	public static final SkyowalletAccount getAccount(final UUID uuid) {
		return accounts.get(uuid);
	}
	
	/**
	 * Gets a bank by its name.
	 * 
	 * @param name The name.
	 * 
	 * @return The bank if found (null otherwise).
	 */
	
	public static final SkyowalletBank getBank(final String name) {
		return banks.get(name);
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
	 * Gets all banks.
	 * 
	 * @return All banks.
	 */
	
	public static final SkyowalletBank[] getBanks() {
		final List<SkyowalletBank> banks = new ArrayList<SkyowalletBank>();
		for(final SkyowalletBank bank : SkyowalletAPI.banks.values()) {
			if(bank != null) {
				banks.add(bank);
			}
		}
		return banks.toArray(new SkyowalletBank[banks.size()]);
	}
	
	/**
	 * Register a new account for the specified UUID.
	 * 
	 * @param uuid The UUID.
	 * 
	 * @return The new account.
	 */
	
	public static final SkyowalletAccount registerAccount(final UUID uuid) {
		final SkyowalletAccount account = new SkyowalletAccount(uuid);
		accounts.put(uuid, account);
		return account;
	}
	
	/**
	 * Creates a new bank with the specified name.
	 * 
	 * @param name The name.
	 * 
	 * @return The new bank.
	 */
	
	public static final SkyowalletBank createBank(final String name) {
		final SkyowalletBank bank = new SkyowalletBank(name);
		banks.put(name, bank);
		return bank;
	}
	
	/**
	 * Deletes a bank.
	 * 
	 * @param bank The bank.
	 * 
	 * @return An HashMap containing deleted accounts.
	 * <br><b>Key :</b> The account.
	 * <br><b>Value :</b> The account's bank balance.
	 */
	
	public static final HashMap<SkyowalletAccount, Double> deleteBank(final SkyowalletBank bank) {
		final HashMap<SkyowalletAccount, Double> members = bank.getMembers();
		for(final SkyowalletAccount account : members.keySet()) {
			account.setBank(null, false);
		}
		banks.put(bank.name, null);
		return members;
	}
	
	/**
	 * Synchronizes the accounts' databases.
	 * <br>NOTE : files are created after the synchronization is done. So, if you manually create files, they will not be loaded.
	 * <br>Thanks to http://stackoverflow.com/a/10951183/3608831.
	 * 
	 * @param sender Used to send some informations, you can obtain the console with <b>Bukkit.getConsoleSender()</b>.
	 */
	
	public static final void sync(final CommandSender sender) {
		final String prefix = sender instanceof Player ? "" : "[Skyowallet] ";
		sender.sendMessage(prefix + ChatColor.GOLD + "Synchronization started...");
		if(accounts.size() == 0) {
			try {
				sender.sendMessage(prefix + ChatColor.AQUA + "Loading accounts...");
				for(final File localAccount : getAccountsDirectory().listFiles()) {
					if(localAccount.isFile()) {
						final SkyowalletAccount account = SkyowalletAccount.fromJson(Files.readFirstLine(localAccount, Charsets.UTF_8));
						accounts.put(account.uuid, account);
					}
				}
				sender.sendMessage(prefix + ChatColor.GREEN + "Accounts loaded.");
			}
			catch(final Exception ex) {
				ex.printStackTrace();
				sender.sendMessage(prefix + ChatColor.RED + "Failed to load accounts.");
			}
			try {
				sender.sendMessage(prefix + ChatColor.AQUA + "Loading banks...");
				for(final File localBank : getBanksDirectory().listFiles()) {
					if(localBank.isFile()) {
						final SkyowalletBank bank = SkyowalletBank.fromJSON(Files.readFirstLine(localBank, Charsets.UTF_8));
						banks.put(bank.name, bank);
					}
				}
				sender.sendMessage(prefix + ChatColor.GREEN + "Banks loaded.");
			}
			catch(final Exception ex) {
				ex.printStackTrace();
				sender.sendMessage(prefix + ChatColor.RED + "Failed to load banks.");
			}
		}
		if(Skyowallet.config.mySQLEnable) {
			try {
				sender.sendMessage(prefix + ChatColor.AQUA + "Synchronization with the MySQL database...");
				if(statement == null) {
					sender.sendMessage(prefix + ChatColor.AQUA + "Logging in to the specified MySQL server...");
					statement = DriverManager.getConnection("jdbc:mysql://" + Skyowallet.config.mySQLHost + ":" + Skyowallet.config.mySQLPort + "/" + Skyowallet.config.mySQLDB, Skyowallet.config.mySQLUser, Skyowallet.config.mySQLPassword).createStatement();
					statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + MYSQL_TABLE_ACCOUNTS + " (uuid BINARY(16) NOT NULL, wallet DOUBLE NOT NULL DEFAULT 0.0, bank VARCHAR(30), bank_balance DOUBLE NOT NULL DEFAULT 0.0, is_bank_owner BOOLEAN NOT NULL DEFAULT false, last_modification_time BIGINT NOT NULL, PRIMARY KEY(uuid))");
					sender.sendMessage(prefix + ChatColor.GREEN + "Done !");
				}
				final HashMap<UUID, SkyowalletAccount> remoteAccounts = new HashMap<UUID, SkyowalletAccount>();
				final ResultSet result = statement.executeQuery("SELECT HEX(uuid) AS uuid, wallet, bank, bank_balance, is_bank_owner, last_modification_time FROM " + MYSQL_TABLE_ACCOUNTS);
				while(result.next()) {
					final UUID uuid = Utils.uuidTryParse(Utils.uuidAddDashes(result.getString("uuid")));
					if(uuid == null) {
						continue;
					}
					remoteAccounts.put(uuid, new SkyowalletAccount(uuid, result.getDouble("wallet"), result.getString("bank"), result.getDouble("bank_balance"), result.getBoolean("is_bank_owner"), result.getLong("last_modification_time")));
				}
				for(final SkyowalletAccount account : remoteAccounts.values()) {
					final SkyowalletAccount localAccount = accounts.get(account.uuid);
					if(localAccount == null || localAccount.lastModificationTime < account.lastModificationTime) {
						accounts.put(account.uuid, account);
					}
				}
				for(final SkyowalletAccount account : accounts.values()) {
					final SkyowalletAccount remoteAccount = remoteAccounts.get(account.uuid);
					if(remoteAccount == null || remoteAccount.lastModificationTime < account.lastModificationTime) {
						statement.executeUpdate("INSERT INTO " + MYSQL_TABLE_ACCOUNTS + "(uuid, wallet, bank, bank_balance, is_bank_owner, last_modification_time) VALUES(UNHEX('" + account.uuid.toString().replace("-", "") + "'), " + account.wallet + ", \"" + account.bank + "\", " + account.bankBalance + ", " + account.isBankOwner + ", " + account.lastModificationTime + ") ON DUPLICATE KEY UPDATE wallet=" + account.wallet + ", bank=\"" + account.bank + "\", bank_balance=" + account.bankBalance + ", is_bank_owner=" + account.isBankOwner + ", last_modification_time=" + account.lastModificationTime);
					}
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
				Files.write(account.toString(), new File(getAccountsDirectoryName(), account.uuid.toString()), Charsets.UTF_8);
			}
			sender.sendMessage(prefix + ChatColor.GREEN + "Accounts saved with success.");
		}
		catch(final Exception ex) {
			sender.sendMessage(prefix + ChatColor.RED + "Failed to save accounts !");
		}
		try {
			sender.sendMessage(prefix + ChatColor.AQUA + "Saving banks...");
			final List<String> removedBanks = new ArrayList<String>();
			for(final Entry<String, SkyowalletBank> entry : banks.entrySet()) {
				final String bankName = entry.getKey();
				final File bankFile = new File(getBanksDirectoryName(), bankName);
				final SkyowalletBank bank = entry.getValue();
				if(bank == null) {
					removedBanks.add(bankName);
					if(bankFile.exists() && bankFile.isFile()) {
						bankFile.delete();
					}
					continue;
				}
				Files.write(bank.toString(), bankFile, Charsets.UTF_8);
			}
			for(final String removedBank : removedBanks) {
				banks.remove(removedBank);
			}
			sender.sendMessage(prefix + ChatColor.GREEN + "Banks saved with success.");
		}
		catch(final Exception ex) {
			ex.printStackTrace();
			sender.sendMessage(prefix + ChatColor.RED + "Failed to save banks !");
		}
		sender.sendMessage(prefix + ChatColor.GOLD + "Synchronization finished.");
	}
	
	/**
	 * Used to manage players' accounts.
	 */
	
	public static class SkyowalletAccount {
		
		private final UUID uuid;
		private double wallet;
		private String bank;
		private double bankBalance;
		private boolean isBankOwner;
		private long lastModificationTime;
		
		/**
		 * Constructs a new Skyowallet's account.
		 * 
		 * @param uuid The uuid.
		 */
		
		public SkyowalletAccount(final UUID uuid) {
			this(uuid, 0.0, null, 0.0, false, System.currentTimeMillis());
		}
		
		/**
		 * Private constructor used to synchronize accounts.
		 * 
		 * @param uuid The uuid.
		 * @param wallet The account's wallet.
		 * @param bank The account's bank.
		 * @param bankBalance The account's bank balance.
		 * @param isBankOwner If the player is an owner of his bank.
		 * @param lastModificationTime The last modification time of the specified account.
		 */
		
		private SkyowalletAccount(final UUID uuid, final double wallet, final String bank, final double bankBalance, final boolean isBankOwner, final long lastModificationTime) {
			this.uuid = uuid;
			this.wallet = wallet;
			this.bank = bank;
			this.bankBalance = bankBalance;
			this.isBankOwner = isBankOwner;
			this.lastModificationTime = lastModificationTime;
		}
		
		/**
		 * Gets the UUID (as String).
		 * 
		 * @return The UUID.
		 */
		
		public final UUID getUUID() {
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
		 * Sets the wallet. The database will be synchronized if the user has enabled "sync-each-modification" in the config.
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
		 * @param sync If you want to synchronizes the database (asynchronously).
		 */
		
		public final void setWallet(final double wallet, final boolean sync) {
			this.wallet = wallet;
			lastModificationTime = System.currentTimeMillis();
			if(sync) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(PLUGIN, new SyncTask());
			}
		}
		
		/**
		 * Gets the account's bank.
		 * 
		 * @return The bank.
		 */
		
		public final SkyowalletBank getBank() {
			if(bank == null) {
				return null;
			}
			return banks.get(bank);
		}
		
		/**
		 * Checks if the account has a bank.
		 * 
		 * @return <b>true</b> If the account has a bank.
		 * <b>false</b> Otherwise.
		 */
		
		public final boolean hasBank() {
			return bank != null;
		}
		
		/**
		 * Sets the bank of the account. <b>null</b> if you want to clear the account's bank.
		 * <br>The database will be synchronized if the user has enabled "sync-each-modification" in the config.
		 * 
		 * @param bank The bank. <b>null</b> if you want to clear the account's bank.
		 * 
		 * @return The old bank balance.
		 */
		
		public final double setBank(final SkyowalletBank bank) {
			return setBank(bank, Skyowallet.config.syncEachModification);
		}
		
		/**
		 * Sets the bank of the account. <b>null</b> if you want to clear the account's bank.
		 * 
		 * @param bank The bank. <b>null</b> if you want to clear the account's bank.
		 * @param sync If you want to synchronizes the database (asynchronously).
		 * 
		 * @return The old bank balance.
		 */
		
		public final double setBank(final SkyowalletBank bank, final boolean sync) {
			final double balance = bankBalance;
			if(bank == null) {
				if(isBankOwner) {
					isBankOwner = false;
				}
				this.bank = null;
			}
			else {
				this.bank = bank.getName();
			}
			bankBalance = 0.0;
			wallet += balance;
			lastModificationTime = System.currentTimeMillis();
			if(sync) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(PLUGIN, new SyncTask());
			}
			return balance;
		}
		
		/**
		 * Gets the account's bank balance.
		 */
		
		public final double getBankBalance() {
			return bankBalance;
		}
		
		/**
		 * Sets the account's bank balance. The database will be synchronized if the user has enabled "sync-each-modification" in the config.
		 * 
		 * @param bank The bank. <b>null</b> if you want to clear the account's bank.
		 */
		
		public final void setBankBalance(final double bankBalance) {
			setBankBalance(bankBalance, Skyowallet.config.syncEachModification);
		}
		
		/**
		 * Sets the account's bank balance.
		 * 
		 * @param bank The bank. <b>null</b> if you want to clear the account's bank.
		 * @param sync If you want to synchronizes the database (asynchronously).
		 */
		
		public final void setBankBalance(final double bankBalance, final boolean sync) {
			this.bankBalance = bankBalance;
			if(sync) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(PLUGIN, new SyncTask());
			}
		}
		
		/**
		 * Checks if the specified account is an owner of its bank.
		 * 
		 * @param account The account.
		 * 
		 * @return <b>true</b> If the specified account is an owner of its bank.
		 * <br><b>false</b> Otherwise.
		 */
		
		public final boolean isBankOwner() {
			return isBankOwner;
		}
		
		/**
		 * Sets if this account should be an owner of its bank.
		 * <br>The database will be synchronized if the user has enabled "sync-each-modification" in the config.
		 * 
		 * @param isOwner <b>true</b> If this account should be an owner of its bank.
		 * <br><b>false</b> Otherwise.
		 */
		
		public final void setBankOwner(final boolean isOwner) {
			setBankOwner(isOwner, Skyowallet.config.syncEachModification);
		}
		
		/**
		 * Sets if this account should be an owner of its bank.
		 * <br>The database will be synchronized if the user has enabled "sync-each-modification" in the config.
		 * 
		 * @param isOwner <b>true</b> If this account should be an owner of its bank.
		 * <br><b>false</b> Otherwise.
		 * @param sync If you want to synchronizes the database (asynchronously).
		 */
		
		public final void setBankOwner(final boolean isOwner, final boolean sync) {
			if(bank == null) {
				return;
			}
			if(isBankOwner != isOwner) {
				isBankOwner = isOwner;
				lastModificationTime = System.currentTimeMillis();
				if(sync) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(PLUGIN, new SyncTask());
				}
			}
		}
		
		/**
		 * Gets the last modification time in millis of the account.
		 * 
		 * @return The last modification time.
		 */
		
		public final long getLastModificationTime() {
			return lastModificationTime;
		}
		
		@Override
		public final String toString() {
			final JSONObject json = new JSONObject();
			json.put("uuid", uuid.toString());
			json.put("wallet", wallet);
			json.put("bank", bank);
			json.put("bankBalance", bankBalance);
			json.put("isBankOwner", isBankOwner);
			json.put("lastModificationTime", lastModificationTime);
			return json.toJSONString();
		}
		
		/**
		 * Constructs an instance from a JSON String.
		 * 
		 * @param json The JSON String.
		 * 
		 * @return A new instance of this class.
		 * 
		 * @throws ParseException If an error occurred while parsing the data.
		 */
		
		public static final SkyowalletAccount fromJson(final String json) throws ParseException {
			final JSONObject jsonObject = (JSONObject)JSONValue.parseWithException(json);
			Object uuid = jsonObject.get("uuid");
			final Object lastModificationTime = jsonObject.get("lastModificationTime");
			if(uuid == null || lastModificationTime == null) {
				throw new NullPointerException("UUID / Last modification is null.");
			}
			uuid = Utils.uuidTryParse(uuid.toString());
			if(uuid == null) {
				throw new IllegalArgumentException("This is not a true UUID !");
			}
			Object wallet = jsonObject.get("wallet");
			if(wallet == null || Utils.doubleTryParse(wallet.toString()) == null) {
				wallet = 0.0;
			}
			final Object bank = jsonObject.get("bank");
			Object bankBalance = jsonObject.get("bankBalance");
			if(bankBalance == null || Utils.doubleTryParse(bankBalance.toString()) == null) {
				bankBalance = 0.0;
			}
			final Object isBankOwner = jsonObject.get("isBankOwner");
			return new SkyowalletAccount((UUID)uuid, Double.parseDouble(wallet.toString()), bank == null ? null : bank.toString(), Double.parseDouble(bankBalance.toString()), isBankOwner == null ? false : Boolean.valueOf(isBankOwner.toString()), Long.parseLong(lastModificationTime.toString()));
		}
		
	}
	
	/**
	 * Used to handle accounts' bank.
	 */
	
	public static class SkyowalletBank {
		
		private String name;
		
		/**
		 * Creates a new bank.
		 * 
		 * @param name The bank's name.
		 */
		
		public SkyowalletBank(final String name) {
			this.name = name;
		}
		
		/**
		 * Gets the bank's name.
		 * 
		 * @return The bank's name.
		 */
		
		public final String getName() {
			return name;
		}
		
		/**
		 * Checks if the specified account is an owner of this bank.
		 * 
		 * @param account The account.
		 * 
		 * @return <b>true</b> If the specified account is an owner of this bank.
		 * <b>false</b> Otherwise.
		 */
		
		public final boolean isOwner(final SkyowalletAccount account) {
			return account.bank != null && account.bank.equals(name) && account.isBankOwner;
		}
		
		/**
		 * Gets the owners of this bank.
		 * 
		 * @return The owners.
		 */
		
		public final SkyowalletAccount[] getOwners() {
			final List<SkyowalletAccount> owners = new ArrayList<SkyowalletAccount>();
			for(final SkyowalletAccount account : accounts.values()) {
				if(isOwner(account)) {
					owners.add(account);
				}
			}
			return owners.toArray(new SkyowalletAccount[owners.size()]);
		}
		
		/**
		 * Checks if the specified account is a member of this bank.
		 * 
		 * @param account The account.
		 * 
		 * @return <b>true</b> If the account is a member of this bank.
		 * <br><b>false</b> Otherwise.
		 */
		
		public final boolean isMember(final SkyowalletAccount account) {
			return account.bank != null && account.bank.equals(name);
		}
		
		/**
		 * Gets the bank's members.
		 * 
		 * @return An HashMap containing the bank's members.
		 * <br><b>Key :</b> The member's account.
		 * <br><b>Value :</b> The member's bank balance.
		 */
		
		public final HashMap<SkyowalletAccount, Double> getMembers() {
			final HashMap<SkyowalletAccount, Double> members = new HashMap<SkyowalletAccount, Double>();
			for(final SkyowalletAccount account : accounts.values()) {
				if(account.bank != null && account.bank.equals(name)) {
					members.put(account, account.bankBalance);
				}
			}
			return members;
		}
		
		@Override
		public final String toString() {
			final JSONObject json = new JSONObject();
			json.put("name", name);
			return json.toJSONString();
		}
		
		/**
		 * Constructs an instance from a JSON String.
		 * 
		 * @param json The JSON String.
		 * 
		 * @return A new instance of this class.
		 * 
		 * @throws ParseException If an error occurred while parsing the data.
		 */
		
		public static final SkyowalletBank fromJSON(final String json) throws ParseException {
			final JSONObject jsonObject = (JSONObject)JSONValue.parseWithException(json);
			final Object name = jsonObject.get("name");
			if(name == null) {
				throw new IllegalArgumentException("Name cannot be null.");
			}
			return new SkyowalletBank(name.toString());
		}
		
	}
	
}