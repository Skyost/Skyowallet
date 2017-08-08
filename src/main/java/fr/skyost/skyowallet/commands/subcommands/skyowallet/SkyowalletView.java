package fr.skyost.skyowallet.commands.subcommands.skyowallet;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.PlaceholderFormatter;
import fr.skyost.skyowallet.utils.Utils;

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
	public boolean onCommand(final CommandSender sender, final String[] args) {
		final OfflinePlayer player = Utils.getPlayerByArgument(args[0]);
		if(player == null || !SkyowalletAPI.hasAccount(player)) {
			sender.sendMessage(Skyowallet.messages.message3);
			return true;
		}
		final double wallet = SkyowalletAPI.getAccount(player).getWallet();
		sender.sendMessage(PlaceholderFormatter.defaultFormat(Skyowallet.messages.message12, player, wallet, wallet));
		return true;
	}

}