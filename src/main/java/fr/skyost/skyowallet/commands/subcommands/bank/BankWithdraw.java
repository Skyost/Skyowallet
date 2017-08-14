package fr.skyost.skyowallet.commands.subcommands.bank;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.SkyowalletBank;
import fr.skyost.skyowallet.SyncManager;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.PlaceholderFormatter;
import fr.skyost.skyowallet.utils.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.utils.PlaceholderFormatter.AmountPlaceholder;
import fr.skyost.skyowallet.utils.PlaceholderFormatter.CurrencyNamePlaceholder;
import fr.skyost.skyowallet.utils.Utils;

public class BankWithdraw implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"withdraw"};
	}

	@Override
	public final boolean mustBePlayer() {
		return true;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.withdraw";
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
	public boolean onCommand(final CommandSender sender, final String[] args) {
		final SkyowalletAccount account = SkyowalletAPI.getAccount((OfflinePlayer)sender);
		
		if(account == null) {
			sender.sendMessage(Skyowallet.messages.message33);
			return true;
		}
		final SkyowalletBank bank = account.getBank();
		if(bank == null) {
			sender.sendMessage(Skyowallet.messages.message21);
			return true;
		}
		
		final Double amount = SkyowalletAPI.round(Utils.doubleTryParse(args[0]));
		if(amount == null) {
			sender.sendMessage(Skyowallet.messages.message13);
			return true;
		}
		
		final Double balance = account.getBankBalance() - amount;
		if(balance < 0d) {
			sender.sendMessage(Skyowallet.messages.message8);
			return true;
		}
		
		final double wallet = account.getWallet() + amount;
		final double taxRate = SkyowalletAPI.getBankWithdrawTaxRate();
		
		account.setWallet(wallet, false, true, taxRate);
		account.setBankBalance(balance, SyncManager.shouldSyncEachModification(), true, 0d);
		sender.sendMessage(Skyowallet.messages.message10);
		
		if(taxRate > 0d) {
			sender.sendMessage(PlaceholderFormatter.format(Skyowallet.messages.message49, new Placeholder("/rate/", String.valueOf(taxRate)), new AmountPlaceholder(wallet - account.getWallet()), new CurrencyNamePlaceholder(wallet - account.getWallet())));
		}
		return true;
	}

}