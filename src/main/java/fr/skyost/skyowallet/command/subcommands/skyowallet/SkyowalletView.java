package fr.skyost.skyowallet.command.subcommands.skyowallet;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.Utils;

/**
 * Represents the <em>/skyowallet view</em> command.
 */

public class SkyowalletView implements CommandInterface {

	@Override
	public final String[] getNames() {
		return new String[]{"view"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.view";
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
		if(!accountManager.has(player)) {
			sender.sendMessage(skyowallet.getPluginMessages().messagePlayerNoAccount);
			return true;
		}
		final double wallet = accountManager.get(player).getWallet().getAmount();
		sender.sendMessage(PlaceholderFormatter.defaultFormat(skyowallet.getPluginMessages().messageWalletInfo, player, wallet));
		return true;
	}

}