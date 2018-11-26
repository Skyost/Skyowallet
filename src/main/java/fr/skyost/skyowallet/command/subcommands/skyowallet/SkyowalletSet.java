package fr.skyost.skyowallet.command.subcommands.skyowallet;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Represents the <em>/skyowallet set</em> command.
 */

public class SkyowalletSet implements CommandInterface {

	@Override
	public final String[] getNames() {
		return new String[]{"set"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.set";
	}

	@Override
	public final int getMinArgsLength() {
		return 1;
	}

	@Override
	public final String getUsage() {
		return "<amount> [player | uuid]";
	}

	@Override
	public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
		final Skyowallet skyowallet = command.getSkyowallet();
		final OfflinePlayer player;
		
		if(args.length < 2) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Console : " + getUsage().replace("[", "<").replace("]", ">"));
				return true;
			}
			player = (Player)sender;
			if(!skyowallet.getAccountManager().has(player)) {
				sender.sendMessage(skyowallet.getPluginMessages().messageNoAccount);
				return true;
			}
		}
		else {
			player = Util.getPlayerByArgument(args[1]);
			if(player == null || !skyowallet.getAccountManager().has(player)) {
				sender.sendMessage(skyowallet.getPluginMessages().messagePlayerNoAccount);
				return true;
			}
		}
		
		final Double parsedDouble = Util.doubleTryParse(args[0]);
		if(parsedDouble == null || parsedDouble < 0d) {
			sender.sendMessage(skyowallet.getPluginMessages().messageInvalidAmount);
			return true;
		}
		
		final SkyowalletAccount account = skyowallet.getAccountManager().get(player);
		account.getWallet().setAmount(parsedDouble, 0d);
		if(player.isOnline()) {
			player.getPlayer().sendMessage(PlaceholderFormatter.defaultFormat(skyowallet.getPluginMessages().messageWalletSet, sender, parsedDouble));
		}
		sender.sendMessage(skyowallet.getPluginMessages().messageDone);
		return true;
	}

}