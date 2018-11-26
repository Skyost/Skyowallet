package fr.skyost.skyowallet.economy;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

/**
 * Represents all available economy operations like taxes, rounding money, ...
 */

public class EconomyOperations {

	/**
	 * The Skyowallet instance.
	 */

	private Skyowallet skyowallet;

	/**
	 * Creates a new economy operations instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public EconomyOperations(final Skyowallet skyowallet) {
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
	 * Rounds a <b>double</b> according to the config options.
	 *
	 * @param amount The <b>double</b>.
	 *
	 * @return The rounded <b>double</b>.
	 */

	public Double round(final Double amount) {
		if(amount == null || skyowallet.getPluginConfig().roundingDigits < 0d) {
			return amount;
		}
		return new BigDecimal(amount).setScale(skyowallet.getPluginConfig().roundingDigits, RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * Taxes a specified amount of money and allocate the taxed money to the accounts specified in the configuration.
	 *
	 * @param amount Amount of money to tax.
	 * @param taxRate The tax rate.
	 *
	 * @return The remaining money, once taxed.
	 */

	public double tax(final double amount, final double taxRate) {
		final ConsoleCommandSender console = Bukkit.getConsoleSender();
		final double taxedAmount = (taxRate * amount) / 100d;
		final SkyowalletAccountManager accountManager = skyowallet.getAccountManager();

		for(final Map.Entry<String, String> entry : skyowallet.getPluginConfig().taxesAccounts.entrySet()) {
			final UUID uuid = Util.uuidTryParse(entry.getKey());

			if(uuid == null) {
				console.sendMessage("[" + skyowallet.getName() + "] " + ChatColor.RED + "Unable to give tax to \"" + entry.getKey() + "\" because it is not a valid UUID.");
				continue;
			}
			final Double accountRate = Util.doubleTryParse(entry.getValue());
			if(accountRate == null) {
				console.sendMessage("[" + skyowallet.getName() + "] " + ChatColor.RED + "Unable to give tax to \"" + uuid + "\" because the specified rate " + entry.getValue() + " is invalid.");
				continue;
			}

			SkyowalletAccount account = accountManager.get(uuid);
			if(account == null) {
				account = accountManager.add(uuid);
			}

			final double newAmount = ((accountRate * taxedAmount) / 100);
			if(newAmount == 0d) {
				continue;
			}

			(skyowallet.getPluginConfig().taxesToBank && account.hasBank() ? account.getBankBalance() : account.getWallet()).addAmount(newAmount, 0d);
			if(!skyowallet.getPluginConfig().taxesNotify) {
				continue;
			}
			final OfflinePlayer player = Bukkit.getOfflinePlayer(account.getUUID());
			if(player != null && player.isOnline()) {
				player.getPlayer().sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageTaxEarned, new PlaceholderFormatter.AmountPlaceholder(newAmount), new PlaceholderFormatter.CurrencyNamePlaceholder(newAmount)));
			}
		}
		return amount - taxedAmount;
	}

}