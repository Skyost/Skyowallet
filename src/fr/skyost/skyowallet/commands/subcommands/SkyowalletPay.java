package fr.skyost.skyowallet.commands.subcommands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletAccount;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.Utils;

public class SkyowalletPay implements CommandInterface {

	@Override
	public final String[] names() {
		return new String[]{"pay", "give"};
	}

	@Override
	public final boolean forcePlayer() {
		return true;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.pay";
	}

	@Override
	public final int getMinArgsLength() {
		return 2;
	}

	@Override
	public final String getUsage() {
		return "<amount> <player | uuid>";
	}

	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		UUID uuid = Utils.uuidTryParse(args[1]);
		final OfflinePlayer player;
		if(uuid == null) {
			player = Bukkit.getOfflinePlayer(args[1]);
			uuid = player.getUniqueId();
		}
		else {
			player = Bukkit.getOfflinePlayer(uuid);
		}
		if(player == null || !SkyowalletAPI.hasAccount(player)) {
			sender.sendMessage(Skyowallet.messages.message3);
			return true;
		}
		final Double amount = Utils.doubleTryParse(args[0]);
		if(amount == null) {
			sender.sendMessage(Skyowallet.messages.message13);
			return true;
		}
		final SkyowalletAccount playerAccount = SkyowalletAPI.getAccount(uuid.toString());
		final double wallet = playerAccount.getWallet() - amount;
		if(wallet < 0.0) {
			sender.sendMessage(Skyowallet.messages.message8);
			return true;
		}
		playerAccount.setWallet(wallet);
		final SkyowalletAccount targetAccount = SkyowalletAPI.getAccount(player.getUniqueId().toString());
		targetAccount.setWallet(targetAccount.getWallet() + amount);
		if(player.isOnline()) {
			player.getPlayer().sendMessage(Skyowallet.messages.message9.replace("/player/", sender.getName()).replace("/amount/", String.valueOf(amount)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(amount)));
		}
		sender.sendMessage(Skyowallet.messages.message10);
		return true;
	}

}