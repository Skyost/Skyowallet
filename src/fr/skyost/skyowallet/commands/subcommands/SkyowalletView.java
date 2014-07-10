package fr.skyost.skyowallet.commands.subcommands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.Utils;

public class SkyowalletView implements CommandInterface {

	@Override
	public final String[] names() {
		return new String[]{"view"};
	}

	@Override
	public final boolean forcePlayer() {
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
		final OfflinePlayer player;
		final UUID uuid = Utils.uuidTryParse(args[0]);
		if(uuid == null) {
			player = Bukkit.getOfflinePlayer(args[0]);
		}
		else {
			player = Bukkit.getOfflinePlayer(uuid);
		}
		if(player == null || !SkyowalletAPI.hasAccount(player)) {
			sender.sendMessage(Skyowallet.messages.message3);
			return true;
		}
		final double wallet = SkyowalletAPI.getAccount(player).getWallet();
		sender.sendMessage(Skyowallet.messages.message12.replace("/player/", player.getName()).replace("/amount/", String.valueOf(wallet)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(wallet)));
		return true;
	}

}