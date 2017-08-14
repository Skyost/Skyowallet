package fr.skyost.skyowallet;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

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
	@ConfigOptions(name = "options.banks-default-require-approval")
	public boolean banksRequireApproval = true;
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
	@ConfigOptions(name = "options.rounding-digits")
	public int roundingDigits = -1;
	
	@ConfigOptions(name = "taxes.amount.global")
	public int taxesAmountGlobal = 0;
	@ConfigOptions(name = "taxes.amount.skyowallet-pay")
	public int taxesAmountSkyowalletPay = 0;
	@ConfigOptions(name = "taxes.amount.bank-deposit")
	public int taxesAmountBankDeposit = 0;
	@ConfigOptions(name = "taxes.amount.bank-withdraw")
	public int taxesAmountBankWithdraw = 0;
	@ConfigOptions(name = "taxes.accounts")
	public HashMap<String, String> taxesAccounts = new HashMap<String, String>();
	
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
		
		taxesAccounts.put("2a74ab4f-8294-46af-af5b-0a9cd65fc1aa", "60");
		taxesAccounts.put("4f3b1387-6967-403d-a648-5feb796ec997", "40");
	}

}