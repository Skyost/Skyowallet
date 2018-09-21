package fr.skyost.skyowallet.config;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;

import fr.skyost.skyowallet.util.Skyoconfig;

/**
 * Represents the plugin configuration.
 */

public class PluginConfig extends Skyoconfig {
	
	@ConfigOptions(name = "economy.currency-name.singular")
	public String currencyNameSingular = "skyodollar";
	@ConfigOptions(name = "economy.currency-name.plural")
	public String currencyNamePlural = "skyodollars";
	@ConfigOptions(name = "economy.default-wallet")
	public double defaultWallet = 0d;
	@ConfigOptions(name = "economy.rounding-digits")
	public int roundingDigits = -1;
	
	@ConfigOptions(name = "options.enable-updater")
	public boolean enableUpdater = true;
	@ConfigOptions(name = "options.enable-metrics")
	public boolean enableMetrics = true;
	@ConfigOptions(name = "options.directory.accounts")
	public String directoryAccounts;
	@ConfigOptions(name = "options.directory.banks")
	public String directoryBanks;
	@ConfigOptions(name = "options.directory.extension")
	public String directoryExtensions;
	@ConfigOptions(name = "options.synchronization.interval")
	public int syncInterval = 900;
	@ConfigOptions(name = "options.synchronization.silent")
	public boolean syncSilent = false;
	@ConfigOptions(name = "options.banks-default-require-approval")
	public boolean banksRequireApproval = true;
	@ConfigOptions(name = "options.warn-offline-mode")
	public boolean warnOfflineMode = true;
	
	@ConfigOptions(name = "taxes.rate.global")
	public double taxesRateGlobal = 0d;
	@ConfigOptions(name = "taxes.rate.skyowallet-pay")
	public double taxesRateSkyowalletPay = 0d;
	@ConfigOptions(name = "taxes.rate.bank-deposit")
	public double taxesRateBankDeposit = 0d;
	@ConfigOptions(name = "taxes.rate.bank-withdraw")
	public double taxesRateBankWithdraw = 0d;
	@ConfigOptions(name = "taxes.notify")
	public boolean taxesNotify = true;
	@ConfigOptions(name = "taxes.to-bank")
	public boolean taxesToBank = true;
	@ConfigOptions(name = "taxes.accounts")
	public HashMap<String, String> taxesAccounts = new HashMap<>();
	
	@ConfigOptions(name = "mysql.enable")
	public boolean mySQLEnable = false;
	@ConfigOptions(name = "mysql.host")
	public String mySQLHost = "localhost";
	@ConfigOptions(name = "mysql.port")
	public int mySQLPort = 3306;
	@ConfigOptions(name = "mysql.database")
	public String mySQLDB = "skyowallet_data";
	@ConfigOptions(name = "mysql.user")
	public String mySQLUser = "root";
	@ConfigOptions(name = "mysql.password")
	public String mySQLPassword = "password";

	/**
	 * Creates a new plugin configuration instance.
	 *
	 * @param dataFolder The plugin's data folder.
	 */

	public PluginConfig(final File dataFolder) {
		super(new File(dataFolder, "config.yml"), Collections.singletonList("Skyowallet Configuration"));

		directoryAccounts = new File(dataFolder + File.separator + "accounts").getPath();
		directoryBanks = new File(dataFolder + File.separator + "banks").getPath();
		directoryExtensions = new File(dataFolder + File.separator + "extensions").getPath();
		
		taxesAccounts.put("2a74ab4f-8294-46af-af5b-0a9cd65fc1aa", "60.5");
		taxesAccounts.put("4f3b1387-6967-403d-a648-5feb796ec997", "39.5");
	}

	/**
	 * Returns the currency name for the specified number.
	 *
	 * @param amount The number.
	 *
	 * @return The currency name.
	 */

	public String getCurrencyName(final Double amount) {
		return amount == null || amount < 2d ? currencyNameSingular : currencyNamePlural;
	}

	/**
	 * Returns the accounts directory (where the plugin stores the accounts).
	 * <br><b>NOTE :</b> if the directory does not exist, this method will try to create it.
	 *
	 * @return The accounts directory.
	 */

	public File getAccountsDirectory() {
		final File accountsDir = new File(this.directoryAccounts);
		if(!accountsDir.exists()) {
			accountsDir.mkdirs();
		}
		return accountsDir;
	}

	/**
	 * Returns the banks directory (where the plugin stores the banks).
	 * <br><b>NOTE :</b> if the directory does not exist, this method will try to create it.
	 *
	 * @return The banks directory.
	 */

	public File getBanksDirectory() {
		final File banksDir = new File(this.directoryBanks);
		if(!banksDir.exists()) {
			banksDir.mkdirs();
		}
		return banksDir;
	}

	/**
	 * Returns the extensions directory (where the plugin stores the banks).
	 * <br><b>NOTE :</b> if the directory does not exist, this method will try to create it.
	 *
	 * @return The extensions directory.
	 */

	public File getExtensionsDirectory() {
		final File extensionsDir = new File(this.directoryExtensions);
		if(!extensionsDir.exists()) {
			extensionsDir.mkdirs();
		}
		return extensionsDir;
	}

}