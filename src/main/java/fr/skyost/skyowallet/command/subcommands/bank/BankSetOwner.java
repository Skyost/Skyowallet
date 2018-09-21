package fr.skyost.skyowallet.command.subcommands.bank;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.BankPlaceholder;
import fr.skyost.skyowallet.util.Utils;

/**
 * Represents the <em>/bank setowner</em> command.
 */

public class BankSetOwner implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"setowner", "set-owner"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.setowner";
	}

	@Override
	public final int getMinArgsLength() {
		return 1;
	}

	@Override
	public final String getUsage() {
		return "<player | uuid>";
	}

	@Override
	public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
		final Skyowallet skyowallet = command.getSkyowallet();
		final SkyowalletAccountManager accountManager = skyowallet.getAccountManager();

		final OfflinePlayer player = Utils.getPlayerByArgument(args[0]);
		if(player == null || !accountManager.has(player)) {
			sender.sendMessage(skyowallet.getPluginMessages().messagePlayerNoAccount);
			return true;
		}
		final SkyowalletAccount account = accountManager.get(player);
		final SkyowalletBank bank = account.getBank();
		if(bank == null) {
			sender.sendMessage(skyowallet.getPluginMessages().messagePlayerNoBank);
			return true;
		}
		if(!sender.hasPermission("skyowallet.admin") && (sender instanceof Player && !bank.isOwner(accountManager.get((OfflinePlayer)sender)))) {
			sender.sendMessage(skyowallet.getPluginMessages().messageBankNoPermission);
			return true;
		}
		if(!bank.isOwner(account)) {
			account.setBankOwner(true);
			if(player.isOnline()) {
				player.getPlayer().sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageBankOwnerAdded, new BankPlaceholder(bank)));
			}
		}
		sender.sendMessage(skyowallet.getPluginMessages().messageDone);
		return true;
	}

}