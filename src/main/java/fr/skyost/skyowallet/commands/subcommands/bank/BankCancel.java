package fr.skyost.skyowallet.commands.subcommands.bank;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.SkyowalletBank;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.PlaceholderFormatter;
import fr.skyost.skyowallet.utils.PlaceholderFormatter.BankPlaceholder;

public class BankCancel implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"cancel"};
	}

	@Override
	public final boolean mustBePlayer() {
		return true;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.cancel";
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
	public boolean onCommand(final CommandSender sender, final String[] args) {
		final SkyowalletAccount account = SkyowalletAPI.getAccount((OfflinePlayer)sender);
		if(account == null) {
			sender.sendMessage(Skyowallet.messages.message33);
			return true;
		}
		final SkyowalletBank bank = account.getBankRequest();
		if(bank == null) {
			sender.sendMessage(Skyowallet.messages.message35);
			return true;
		}
		account.setBankRequest(null);
		sender.sendMessage(PlaceholderFormatter.format(Skyowallet.messages.message38, new BankPlaceholder(bank)));
		return true;
	}

}