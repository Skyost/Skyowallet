package fr.skyost.skyowallet.command.subcommands.skyowallet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Set;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.AmountPlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.CurrencyNamePlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.PlayerPlaceholder;

/**
 * Represents the <em>/skyowallet info</em> command.
 */

public class SkyowalletInfo implements CommandInterface {

	@Override
	public final String[] getNames() {
		return new String[]{"info"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.info";
	}

	@Override
	public final int getMinArgsLength() {
		return 0;
	}

	@Override
	public final String getUsage() {
		return null;
	}

	@Override
	public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
		final Skyowallet skyowallet = command.getSkyowallet();
		sender.sendMessage(ChatColor.GOLD + skyowallet.getName() + " v" + skyowallet.getDescription().getVersion());
		
		final Set<SkyowalletAccount> accounts = skyowallet.getAccountManager().list();
		sender.sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageTotalAccounts, new Placeholder("total-accounts", String.valueOf(accounts.size()))));
		if(accounts.isEmpty()) {
			return true;
		}
		
		double totalMoney = 0d;
		SkyowalletAccount bestAccount = null;
		for(final SkyowalletAccount account : accounts) {
			final double amount = account.getWallet().getAmount() + account.getBankBalance().getAmount();
			totalMoney += amount;
			if(bestAccount == null || bestAccount.getWallet().getAmount() + bestAccount.getBankBalance().getAmount() < amount) {
				bestAccount = account;
			}
		}
		totalMoney = skyowallet.getEconomyOperations().round(totalMoney);
		
		final double bestAccountAmount = bestAccount.getWallet().getAmount() + bestAccount.getBankBalance().getAmount();
		final OfflinePlayer player = Bukkit.getOfflinePlayer(bestAccount.getUUID());
		sender.sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageTotalMoney, new AmountPlaceholder(totalMoney), new CurrencyNamePlaceholder(totalMoney)));
		sender.sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageBestAmount, (player == null ? new PlayerPlaceholder(bestAccount.getUUID()) : new PlayerPlaceholder(player)), new AmountPlaceholder(bestAccountAmount), new CurrencyNamePlaceholder(bestAccountAmount)));
		return true;
	}

}