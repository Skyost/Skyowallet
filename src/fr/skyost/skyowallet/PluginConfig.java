package fr.skyost.skyowallet;

import java.io.File;
import java.util.Arrays;

import fr.skyost.skyowallet.utils.Skyoconfig;

public class PluginConfig extends Skyoconfig {
	
	@ConfigOptions(name = "economy.currency-name.singular")
	public String currencyNameSingular = "skyodollar";
	@ConfigOptions(name = "economy.currency-name.plural")
	public String currencyNamePlural = "skyodollars";
	@ConfigOptions(name = "economy.default-wallet")
	public double defaultWallet = 0d;
	
	@ConfigOptions(name = "options.enable-updater")
	public boolean enableUpdater = true;
	@ConfigOptions(name = "options.enable-metrics")
	public boolean enableMetrics = true;
	@ConfigOptions(name = "options.accounts-directory")
	public String accountsDir;
	@ConfigOptions(name = "options.banks-directory")
	public String banksDir;
	@ConfigOptions(name = "options.extensions-directory")
	public String extensionsDir;
	@ConfigOptions(name = "options.auto-sync-interval")
	public int autoSyncInterval = 300;
	@ConfigOptions(name = "options.warn-offline-mode")
	public boolean warnOfflineMode = true;
	@ConfigOptions(name = "options.sync-each-modification")
	public boolean syncEachModification = false;
	@ConfigOptions(name = "options.silent-sync")
	public boolean silentSync = false;
	
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

	protected PluginConfig(final File dataFolder) {
		super(new File(dataFolder, "config.yml"), Arrays.asList("Skyowallet Configuration"));
		accountsDir = new File(dataFolder + File.separator + "accounts").getPath();
		banksDir = new File(dataFolder + File.separator + "banks").getPath();
		extensionsDir = new File(dataFolder + File.separator + "extensions").getPath();
	}

}
