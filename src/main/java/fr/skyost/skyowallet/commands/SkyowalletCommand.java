package fr.skyost.skyowallet.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.utils.PlaceholderFormatter;

public class SkyowalletCommand extends SubCommandsExecutor {
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, String[] args) {
		if(args.length <= 0) {
			if(sender instanceof Player) {
				if(!SkyowalletAPI.hasAccount((Player)sender)) {
					sender.sendMessage(Skyowallet.messages.message33);
					return true;
				}
				final double wallet = SkyowalletAPI.getAccount((Player)sender).getWallet();
				sender.sendMessage(PlaceholderFormatter.defaultFormat(Skyowallet.messages.message4, sender, wallet, wallet));
			}
			else {
				sender.sendMessage(Skyowallet.messages.message2);
			}
			return true;
		}
		return super.onCommand(sender, command, label, args);
	}

}