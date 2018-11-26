package fr.skyost.skyowallet.command.subcommands.bank;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.economy.bank.SkyowalletBankManager;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.util.Util;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/**
 * Represents the <em>/bank create</em> command.
 */

public class BankCreate implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"create"};
	}

	@Override
	public final boolean mustBePlayer() {
		return true;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.create";
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
		final SkyowalletAccountManager accountManager = skyowallet.getAccountManager();
		final SkyowalletBankManager bankManager = skyowallet.getBankManager();

		if(!accountManager.has((OfflinePlayer)sender)) {
			sender.sendMessage(skyowallet.getPluginMessages().messageNoAccount);
			return true;
		}
		if(bankManager.has(args[0])) {
			sender.sendMessage(skyowallet.getPluginMessages().messageBankAlreadyExists);
			return true;
		}
		if(!Util.isValidFileName(args[0])) {
			sender.sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageInvalidName, new Placeholder("name", args[0])));
			return true;
		}
		final SkyowalletAccount account = accountManager.get((OfflinePlayer)sender);
		if(account.hasBank()) {
			sender.sendMessage(skyowallet.getPluginMessages().messageAlreadyHaveBank);
			return true;
		}

		final SkyowalletBank bank = skyowallet.getBankFactory().create(args[0], bankManager);
		account.setBank(bank);
		account.setBankOwner(true);
		sender.sendMessage(skyowallet.getPluginMessages().messageDone);
		return true;
	}

}