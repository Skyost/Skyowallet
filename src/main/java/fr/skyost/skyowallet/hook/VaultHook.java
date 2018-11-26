package fr.skyost.skyowallet.hook;

import com.google.common.base.Charsets;
import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.economy.bank.SkyowalletBankManager;
import fr.skyost.skyowallet.util.Util;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Allows to hook into Vault.
 */

public class VaultHook extends AbstractEconomy {

	/**
	 * The Skyowallet instance.
	 */
	
	private Skyowallet skyowallet;

	/**
	 * Creates a new Vault hook instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */
	
	public VaultHook(final Skyowallet skyowallet) {
		this.skyowallet = skyowallet;
	}

	/**
	 * Returns the Skyowallet instance.
	 *
	 * @return The Skyowallet instance.
	 */

	public final Skyowallet getSkyowallet() {
		return skyowallet;
	}

	/**
	 * Sets the Skyowallet instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public final void setSkyowallet(final Skyowallet skyowallet) {
		this.skyowallet = skyowallet;
	}

	/**
	 * Registers the hook.
	 */

	public void register() {
		final Logger logger = skyowallet.getLogger();
		logger.log(Level.INFO, "Registering the Vault hook...");
		Bukkit.getServicesManager().register(Economy.class, this, skyowallet, ServicePriority.Highest);
		logger.log(Level.INFO, "Finished ! Vault will now support Skyowallet !");

		/*final Method hookEconomy = vault.getClass().getDeclaredMethod("hookEconomy", String.class, Class.class, ServicePriority.class, String[].class);
		hookEconomy.setAccessible(true);
		hookEconomy.invoke(vault, "Skyowallet", VaultHook.class, ServicePriority.Normal, new String[]{"fr.skyost.skyowallet.Skyowallet"});*/
	}
	
	@Override
	public final String getName() {
		return skyowallet.getName();
	}
	
	@Override
	public final boolean isEnabled() {
		return skyowallet != null;
	}
	
	@Override
	public final boolean hasBankSupport() {
		return true;
	}
	
	@Override
	public final String currencyNamePlural() {
		return skyowallet.getPluginConfig().currencyNamePlural;
	}
	
	@Override
	public final String currencyNameSingular() {
		return skyowallet.getPluginConfig().currencyNameSingular;
	}
	
	@Override
	public final String format(final double amount) {
		return skyowallet.getEconomyOperations().round(amount) + " " + skyowallet.getPluginConfig().getCurrencyName(amount);
	}
	
	@Override
	public final boolean createPlayerAccount(final String playerName) {
		if(hasAccount(playerName)) {
			return true;
		}
		final OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
		if(player == null) {
			return false;
		}
		skyowallet.getAccountManager().add(player);
		return true;
	}
	
	@Override
	public final int fractionalDigits() {
		return skyowallet.getPluginConfig().roundingDigits;
	}
	
	@Override
	public final double getBalance(final String playerName) {
		if(!hasAccount(playerName)) {
			return -1d;
		}
		return getAccountByName(playerName).getWallet().getAmount();
	}
	
	@Override
	public final EconomyResponse withdrawPlayer(final String playerName, final double amount) {
		double balance = getBalance(playerName);
		if(amount < 0) {
			return new EconomyResponse(amount, balance, ResponseType.FAILURE, "Cannot withdraw negative funds.");
		}
		if(!has(playerName, amount)) {
			return new EconomyResponse(amount, balance, ResponseType.FAILURE, "Insufficient funds.");
		}
		if(!hasAccount(playerName)) {
			return new EconomyResponse(amount, balance, ResponseType.FAILURE, "This player does not exist or does not have an account.");
		}
		getAccountByName(playerName).getWallet().subtractAmount(amount);
		return new EconomyResponse(amount, balance, ResponseType.SUCCESS, "Success.");
	}
	
	@Override
	public final EconomyResponse depositPlayer(final String playerName, final double amount) {
		double balance = getBalance(playerName);
		if(amount < 0) {
			return new EconomyResponse(amount, balance, ResponseType.FAILURE, "Cannot withdraw deposit funds.");
		}
		if(!hasAccount(playerName)) {
			return new EconomyResponse(amount, balance, ResponseType.FAILURE, "This player does not exist or does not have an account.");
		}
		getAccountByName(playerName).getWallet().addAmount(amount);
		return new EconomyResponse(amount, balance, ResponseType.SUCCESS, "Success.");
	}
	
	@Override
	public final boolean has(final String playerName, final double amount) {
		return getBalance(playerName) >= amount;
	}
	
	@Override
	public final boolean hasAccount(final String playerName) {
		return getAccountByName(playerName) != null;
	}
	
	@Override
	public final EconomyResponse createBank(final String bankName, final String playerName) {
		if(!Util.isValidFileName(bankName)) {
			return new EconomyResponse(0d, 0d, ResponseType.FAILURE, "This is not a valid bank name.");
		}
		if(!hasAccount(playerName)) {
			return new EconomyResponse(0d, 0d, ResponseType.FAILURE, "This player does not exist or does not have an account.");
		}
		if(skyowallet.getBankManager().has(bankName)) {
			return new EconomyResponse(0d, 0d, ResponseType.FAILURE, "A bank with the same name already exists.");
		}
		final SkyowalletBank bank = skyowallet.getBankFactory().create(bankName, skyowallet.getBankManager());
		final SkyowalletAccount account = getAccountByName(playerName);
		account.setBank(bank);
		account.setBankOwner(true);
		return new EconomyResponse(0d, 0d, ResponseType.SUCCESS, "Success.");
	}
	
	@Override
	public final EconomyResponse deleteBank(final String bankName) {
		final SkyowalletBankManager bankManager = skyowallet.getBankManager();
		if(!bankManager.has(bankName)) {
			return new EconomyResponse(0d, 0d, ResponseType.FAILURE, "This bank does not exist.");
		}
		bankManager.remove(bankName);
		return new EconomyResponse(0d, 0d, ResponseType.SUCCESS, "Success.");
	}
	
	@Override
	public final EconomyResponse bankBalance(final String playerName) {
		if(!hasAccount(playerName)) {
			return new EconomyResponse(0d, 0d, ResponseType.FAILURE, "This player does not exist or does not have an account.");
		}
		final SkyowalletAccount account = getAccountByName(playerName);
		if(!account.hasBank()) {
			return new EconomyResponse(0d, 0d, ResponseType.FAILURE, "This player does not have a bank account.");
		}
		return new EconomyResponse(0d, account.getBankBalance().getAmount(), ResponseType.SUCCESS, "Success.");
	}
	
	@Override
	public final EconomyResponse bankHas(final String playerName, final double amount) {
		final EconomyResponse bankBalance = bankBalance(playerName);
		if(bankBalance.type == ResponseType.FAILURE) {
			return bankBalance;
		}
		return bankBalance.balance >= amount ? new EconomyResponse(amount, bankBalance.amount, ResponseType.SUCCESS, "This player has enough money on his bank account.") : new EconomyResponse(amount, bankBalance.amount, ResponseType.FAILURE, "This player does not have enough money on his bank account.");
	}
	
	@Override
	public final EconomyResponse bankWithdraw(final String playerName, final double amount) {
		final EconomyResponse bankBalance = bankBalance(playerName);
		if(bankBalance.type == ResponseType.FAILURE) {
			return bankBalance;
		}
		final EconomyResponse bankHas = bankHas(playerName, amount);
		if(bankHas.type == ResponseType.FAILURE) {
			return bankHas;
		}

		final SkyowalletAccount account = getAccountByName(playerName);
		account.getBankBalance().transfer(account.getWallet(), amount);
		return new EconomyResponse(amount, account.getBankBalance().getAmount(), ResponseType.SUCCESS, "Success.");
	}
	
	@Override
	public final EconomyResponse bankDeposit(final String playerName, final double amount) {
		final EconomyResponse bankBalance = bankBalance(playerName);
		if(bankBalance.type == ResponseType.FAILURE) {
			return bankBalance;
		}

		final SkyowalletAccount account = getAccountByName(playerName);
		account.getWallet().transfer(account.getBankBalance(), amount);
		return new EconomyResponse(amount, account.getBankBalance().getAmount(), ResponseType.SUCCESS, "Success.");
	}
	
	@Override
	public final EconomyResponse isBankOwner(final String bankName, final String playerName) {
		if(!skyowallet.getBankManager().has(bankName)) {
			return new EconomyResponse(0d, 0d, ResponseType.FAILURE, "This bank does not exist.");
		}
		if(!hasAccount(playerName)) {
			return new EconomyResponse(0d, 0d, ResponseType.FAILURE, "This player does not exist or does not have an account.");
		}
		return getAccountByName(playerName).isBankOwner() ? new EconomyResponse(0d, 0d, ResponseType.SUCCESS, "This player is a bank owner.") : new EconomyResponse(0d, 0d, ResponseType.FAILURE, "This player is not a bank owner.");
	}
	
	@Override
	public final EconomyResponse isBankMember(final String bankName, final String playerName) {
		if(!skyowallet.getBankManager().has(bankName)) {
			return new EconomyResponse(0d, 0d, ResponseType.FAILURE, "This bank does not exist.");
		}
		if(!hasAccount(playerName)) {
			return new EconomyResponse(0d, 0d, ResponseType.FAILURE, "This player does not exist or does not have an account.");
		}
		final SkyowalletBank playerBank = getAccountByName(playerName).getBank();
		if(playerBank == null) {
			return new EconomyResponse(0d, 0d, ResponseType.FAILURE, "This player does not have a bank account.");
		}
		return playerBank.getName().equals(bankName) ? new EconomyResponse(0d, 0d, ResponseType.SUCCESS, "This player is a member of this bank.") : new EconomyResponse(0d, 0d, ResponseType.FAILURE, "This player is not a member of this bank.");
	}
	
	@Override
	public final List<String> getBanks() {
		final List<String> banks = new ArrayList<>();
		for(final SkyowalletBank bank : skyowallet.getBankManager().list()) {
			banks.add(bank.getName());
		}
		return banks;
	}
	
	@Override
	public final boolean hasAccount(final String playerName, final String worldName) {
		return hasAccount(playerName);
	}
	
	@Override
	public final double getBalance(final String playerName, final String worldName) {
		return getBalance(playerName);
	}
	
	@Override
	public final boolean has(final String playerName, final String worldName, final double amount) {
		return has(playerName, amount);
	}
	
	@Override
	public final EconomyResponse withdrawPlayer(final String playerName, final String worldName, final double amount) {
		return withdrawPlayer(playerName, amount);
	}
	
	@Override
	public final EconomyResponse depositPlayer(final String playerName, final String worldName, final double amount) {
		return depositPlayer(playerName, amount);
	}
	
	@Override
	public final boolean createPlayerAccount(final String playerName, final String worldName) {
		return createPlayerAccount(playerName);
	}

	/**
	 * Returns the account that corresponds to the specified player name.
	 *
	 * @param playerName The player name.
	 *
	 * @return The corresponding account.
	 */
	
	private SkyowalletAccount getAccountByName(final String playerName) {
		final SkyowalletAccountManager accountManager = skyowallet.getAccountManager();
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
		if(player == null) {
			player = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(Charsets.UTF_8)));
		}
		if(player == null || !accountManager.has(player)) {
			return null;
		}
		return accountManager.get(player);
	}
		
}