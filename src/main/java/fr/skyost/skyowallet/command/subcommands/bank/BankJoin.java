package fr.skyost.skyowallet.command.subcommands.bank;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.BankPlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.PlayerPlaceholder;

/**
 * Represents the <em>/bank join</em> command.
 */

public class BankJoin implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"join"};
	}

	@Override
	public final boolean mustBePlayer() {
		return true;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.join";
	}

	@Override
	public final int getMinArgsLength() {
		return 1;
	}

	@Override
	public final String getUsage() {
		return "<bank>";
	}

	@Override
	public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
		final Skyowallet skyowallet = command.getSkyowallet();

		final SkyowalletAccount account = skyowallet.getAccountManager().get((OfflinePlayer)sender);
		if(account == null) {
			sender.sendMessage(skyowallet.getPluginMessages().messageNoAccount);
			return true;
		}
		if(account.hasBankRequest()) {
			sender.sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageBankRequestAlreadySent, new BankPlaceholder(account.getBankRequest())));
			return true;
		}
		if(account.hasBank()) {
			sender.sendMessage(skyowallet.getPluginMessages().messageAlreadyHaveBank);
			return true;
		}
		final SkyowalletBank bank = skyowallet.getBankManager().get(args[0]);
		if(bank == null) {
			sender.sendMessage(skyowallet.getPluginMessages().messageUnexistingBank);
			return true;
		}
		if(bank.isApprovalRequired()) {
			account.setBankRequest(bank);
			sender.sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageBankRequest, new BankPlaceholder(bank)));
			
			final String message = PlaceholderFormatter.format(skyowallet.getPluginMessages().messagePlayerBankRequest, new PlayerPlaceholder(sender));
			for(final SkyowalletAccount owner : bank.getOwners()) {
				final OfflinePlayer player = Bukkit.getOfflinePlayer(owner.getUUID());
				if(player != null && player.isOnline()) {
					player.getPlayer().sendMessage(message);
				}
			}
		}
		else {
			account.setBank(bank);
			sender.sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageBankWelcome, new BankPlaceholder(bank)));
		}
		return true;
	}

}