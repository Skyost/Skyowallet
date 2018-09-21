package fr.skyost.skyowallet.command.subcommands.bank;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.BankPlaceholder;

/**
 * Represents the <em>/bank cancel</em> command.
 */

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
	public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
		final Skyowallet skyowallet = command.getSkyowallet();

		final SkyowalletAccount account = skyowallet.getAccountManager().get((OfflinePlayer)sender);
		if(account == null) {
			sender.sendMessage(skyowallet.getPluginMessages().messageNoAccount);
			return true;
		}
		final SkyowalletBank bank = account.getBankRequest();
		if(bank == null) {
			sender.sendMessage(skyowallet.getPluginMessages().messageBankNotRequested);
			return true;
		}
		account.setBankRequest(null);
		sender.sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageBankRequestCancelled, new BankPlaceholder(bank)));
		return true;
	}

}