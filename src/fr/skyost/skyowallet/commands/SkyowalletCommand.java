package fr.skyost.skyowallet.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;

public class SkyowalletCommand extends SubCommandsExecutor {
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, String[] args) {
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
		return super.onCommand(sender, command, label, args);
	}

}
