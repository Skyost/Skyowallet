package fr.skyost.skyowallet.command.subcommands.bank;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.account.holder.WalletHolder;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.AmountPlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.CurrencyNamePlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.util.Util;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/**
 * Represents the <em>/bank deposit</em> command.
 */

public class BankDeposit implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"deposit"};
	}

	@Override
	public final boolean mustBePlayer() {
		return true;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.deposit";
	}

	@Override
	public final int getMinArgsLength() {
		return 1;
	}

	@Override
	public final String getUsage() {
		return "<amount>";
	}

	@Override
	public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
		final Skyowallet skyowallet = command.getSkyowallet();
		final SkyowalletAccount account = skyowallet.getAccountManager().get((OfflinePlayer)sender);
		
		if(account == null) {
			sender.sendMessage(skyowallet.getPluginMessages().messageNoAccount);
			return true;
		}
		
		final SkyowalletBank bank = account.getBank();
		if(bank == null) {
			sender.sendMessage(skyowallet.getPluginMessages().messageNoBankAccount);
			return true;
		}
		
		final Double amount = skyowallet.getEconomyOperations().round(Util.doubleTryParse(args[0]));
		if(amount == null || amount < 0d) {
			sender.sendMessage(skyowallet.getPluginMessages().messageInvalidAmount);
			return true;
		}
		
		final WalletHolder wallet = account.getWallet();
		if(!wallet.canSubtract(amount)) {
			sender.sendMessage(skyowallet.getPluginMessages().messageNotEnoughMoney);
			return true;
		}

		final double balance = account.getBankBalance().getAmount() + amount;
		final double taxRate = skyowallet.getPluginConfig().taxesRateBankDeposit;

		wallet.transfer(account.getBankBalance(), amount, taxRate);
		sender.sendMessage(skyowallet.getPluginMessages().messageDone);
		
		if(taxRate > 0d) {
			final double totalAmount = amount - (balance - account.getBankBalance().getAmount());
			sender.sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageTaxesRate, new Placeholder("rate", String.valueOf(taxRate)), new AmountPlaceholder(totalAmount), new CurrencyNamePlaceholder(totalAmount)));
		}
		return true;
	}

}