package fr.skyost.skyowallet.command.subcommands.skyowallet;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;
import fr.skyost.skyowallet.economy.account.holder.WalletHolder;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.AmountPlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.CurrencyNamePlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.util.Utils;

/**
 * Represents the <em>/skyowallet pay</em> command.
 */

public class SkyowalletPay implements CommandInterface {

	@Override
	public final String[] getNames() {
		return new String[]{"pay", "give"};
	}

	@Override
	public final boolean mustBePlayer() {
		return true;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.pay";
	}

	@Override
	public final int getMinArgsLength() {
		return 2;
	}

	@Override
	public final String getUsage() {
		return "<amount> <player | uuid>";
	}

	@Override
	public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
		final Skyowallet skyowallet = command.getSkyowallet();
		final SkyowalletAccountManager accountManager = skyowallet.getAccountManager();

		if(!accountManager.has((OfflinePlayer)sender)) {
			sender.sendMessage(skyowallet.getPluginMessages().messageNoAccount);
			return true;
		}
		
		final OfflinePlayer player = Utils.getPlayerByArgument(args[1]);
		if(player == null || !accountManager.has(player)) {
			sender.sendMessage(skyowallet.getPluginMessages().messagePlayerNoAccount);
			return true;
		}
		
		final Double amount = skyowallet.getEconomyOperations().round(Utils.doubleTryParse(args[0]));
		if(amount == null || amount < 0d) {
			sender.sendMessage(skyowallet.getPluginMessages().messageInvalidAmount);
			return true;
		}
		if(!accountManager.has((OfflinePlayer)sender)) {
			sender.sendMessage(skyowallet.getPluginMessages().messageNoAccount);
			return true;
		}
		
		final SkyowalletAccount playerAccount = accountManager.get((OfflinePlayer)sender);
		final WalletHolder wallet = playerAccount.getWallet();
		if(!wallet.canSubtract(amount)) {
			sender.sendMessage(skyowallet.getPluginMessages().messageNotEnoughMoney);
			return true;
		}
		
		final SkyowalletAccount targetAccount = accountManager.get(player);
		
		final double targetWallet = targetAccount.getWallet().getAmount() + amount;
		final double taxRate = skyowallet.getPluginConfig().taxesRateSkyowalletPay;

		wallet.transfer(targetAccount.getWallet(), amount, taxRate);
		final double totalAmount = amount - (targetWallet - targetAccount.getWallet().getAmount());

		if(player.isOnline()) {
			player.getPlayer().sendMessage(PlaceholderFormatter.defaultFormat(skyowallet.getPluginMessages().messageAmountPaid, sender, totalAmount));
		}
		sender.sendMessage(skyowallet.getPluginMessages().messageDone);
		
		if(taxRate > 0d) {
			sender.sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageTaxesRate, new Placeholder("rate", String.valueOf(taxRate)), new AmountPlaceholder(totalAmount), new CurrencyNamePlaceholder(totalAmount)));
		}
		return true;
	}

}