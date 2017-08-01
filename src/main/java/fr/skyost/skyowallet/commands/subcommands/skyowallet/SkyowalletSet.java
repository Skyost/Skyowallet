package fr.skyost.skyowallet.commands.subcommands.skyowallet;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.Utils;

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
	public boolean onCommand(final CommandSender sender, final String[] args) {
		final OfflinePlayer player;
		if(args.length < 2) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Console : " + getUsage().replace("[", "<").replace("]", ">"));
				return true;
			}
			player = (Player)sender;
			if(!SkyowalletAPI.hasAccount(player)) {
				sender.sendMessage(Skyowallet.messages.message33);
				return true;
			}
		}
		else {
			player = Utils.getPlayerByArgument(args[1]);
			if(player == null || !SkyowalletAPI.hasAccount(player)) {
				sender.sendMessage(Skyowallet.messages.message3);
				return true;
			}
		}
		final Double parsedDouble = Utils.doubleTryParse(args[0]);
		if(parsedDouble == null) {
			sender.sendMessage(Skyowallet.messages.message13);
			return true;
		}
		final SkyowalletAccount account = SkyowalletAPI.getAccount(player);
		account.setWallet(parsedDouble);
		if(player.isOnline()) {
			final double wallet = account.getWallet();
			player.getPlayer().sendMessage(Skyowallet.messages.message11.replace("/player/", sender.getName()).replace("/amount/", String.valueOf(wallet)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(wallet)));
		}
		sender.sendMessage(Skyowallet.messages.message10);
		return true;
	}

}