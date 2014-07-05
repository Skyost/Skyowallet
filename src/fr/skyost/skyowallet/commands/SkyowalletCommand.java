package fr.skyost.skyowallet.commands;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;

public class SkyowalletCommand extends SubCommandsExecutor {
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, String[] args) {
		try {
			if(args.length <= 0) {
				if(sender instanceof Player) {
					final double wallet = SkyowalletAPI.getAccount(((Player)sender).getUniqueId().toString()).getWallet();
					sender.sendMessage(Skyowallet.messages.message4.replace("/amount/", String.valueOf(wallet)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(wallet)).replace("/n/", "\n"));
				}
				else {
					sender.sendMessage(Skyowallet.messages.message2);
				}
				return true;
			}
			final CommandInterface commandInterface = this.getExecutor(args[0]);
			if(commandInterface == null) {
				return false;
			}
			if(commandInterface.forcePlayer() && !(sender instanceof Player)) {
				sender.sendMessage(Skyowallet.messages.message2);
				return true;
			}
			final String permission = commandInterface.getPermission();
			if(permission != null && !sender.hasPermission(permission)) {
				sender.sendMessage(Skyowallet.messages.message1);
				return true;
			}
			args = Arrays.copyOfRange(args, 1, args.length);
			if(args.length < commandInterface.getMinArgsLength()) {
				sender.sendMessage(ChatColor.RED + "/" + label + " " + commandInterface.getUsage());
				return true;
			}
			return commandInterface.onCommand(sender, args);
		}
		catch(final Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

}
