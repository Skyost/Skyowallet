package fr.skyost.skyowallet.command.subcommands.bank;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.util.PlaceholderFormatter;

/**
 * Represents the <em>/bank leave</em> command.
 */

public class BankLeave implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"leave"};
	}

	@Override
	public final boolean mustBePlayer() {
		return true;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.leave";
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
		if(!account.hasBank()) {
			sender.sendMessage(skyowallet.getPluginMessages().messageNoBankAccount);
			return true;
		}
		
		final double amount = account.setBank(null);
		sender.sendMessage(amount > 0 ? PlaceholderFormatter.defaultFormat(skyowallet.getPluginMessages().messageBankLeft, sender, amount) : skyowallet.getPluginMessages().messageDone);
		return true;
	}

}