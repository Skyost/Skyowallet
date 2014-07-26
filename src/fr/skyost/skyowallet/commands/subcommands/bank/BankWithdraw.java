package fr.skyost.skyowallet.commands.subcommands.bank;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletAccount;
import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletBank;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
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
		final SkyowalletBank bank = account.getBank();
		if(bank == null) {
			sender.sendMessage(Skyowallet.messages.message21);
			return true;
		}
		final Double amount = Utils.doubleTryParse(args[0]);
		if(amount == null) {
			sender.sendMessage(Skyowallet.messages.message13);
			return true;
		}
		final Double balance = account.getBankBalance() - amount;
		if(balance < 0.0) {
			sender.sendMessage(Skyowallet.messages.message8);
			return true;
		}
		account.setBankBalance(balance, false);
		account.setWallet(account.getWallet() + amount);
		sender.sendMessage(Skyowallet.messages.message10);
		return true;
	}

}
