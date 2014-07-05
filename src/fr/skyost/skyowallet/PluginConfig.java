package fr.skyost.skyowallet;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Material;

import fr.skyost.skyowallet.utils.Skyoconfig;

public class PluginConfig extends Skyoconfig {
	
	@ConfigOptions(name = "economy.currency-name.singular")
	public String currencyNameSingular = "skyodollar";
	@ConfigOptions(name = "economy.currency-name.plural")
	public String currencyNamePlural = "skyodollars";
	@ConfigOptions(name = "economy.default-wallet")
	public double defaultWallet = 0.0;
	
	@ConfigOptions(name = "options.enable-updater")
	public boolean enableUpdater = true;
	@ConfigOptions(name = "options.enable-metrics")
	public boolean enableMetrics = true;
	@ConfigOptions(name = "options.accounts-directory")
	public String accountsDir;
	@ConfigOptions(name = "options.auto-sync-interval")
	public int autoSyncInterval = 300;
	@ConfigOptions(name = "options.warn-offline-mode")
	public boolean warnOfflineMode = true;
	
	@ConfigOptions(name = "extensions.mine4cash.enable")
	public boolean mine4CashEnable = false;
	@ConfigOptions(name = "extensions.mine4cash.data")
	public HashMap<String, String> mine4CashData = new HashMap<String, String>();
	@ConfigOptions(name = "extensions.mine4cash.auto-drop-item")
	public boolean mine4CashAutoDropItem = false;
	@ConfigOptions(name = "extensions.commands-costs.enable")
	public boolean commandsCostsEnable = false;
	@ConfigOptions(name = "extensions.commands-costs.data")
	public HashMap<String, String> commandsCostsData = new HashMap<String, String>();
	
	@ConfigOptions(name = "mysql.enable")
	public boolean mySQLEnable = false;
	@ConfigOptions(name = "mysql.host")
	public String mySQLHost = "localhost";
	@ConfigOptions(name = "mysql.port")
	public short mySQLPort = 3306;
	@ConfigOptions(name = "mysql.database")
	public String mySQLDB = "skyowallet_data";
	@ConfigOptions(name = "mysql.user")
	public String mySQLUser = "root";
	@ConfigOptions(name = "mysql.password")
	public String mySQLPassword = "password";

	protected PluginConfig(final File dataFolder) {
		super(new File(dataFolder, "config.yml"), Arrays.asList("Skyowallet Configuration"));
		accountsDir = dataFolder.getPath() + File.separator + "accounts";
		mine4CashData.put(Material.GOLD_ORE.name(), "100.0");
		mine4CashData.put(Material.DIAMOND_ORE.name(), "150.0");
		mine4CashData.put(Material.EMERALD_ORE.name(), "200.0");
		commandsCostsData.put("pl", "10.0");
	}

}
