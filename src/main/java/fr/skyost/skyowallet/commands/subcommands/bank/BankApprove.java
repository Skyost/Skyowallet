package fr.skyost.skyowallet.commands.subcommands.bank;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.SkyowalletBank;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.Utils;

public class BankApprove implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"approve"};
	}

	@Override
	public final boolean mustBePlayer() {
		return true;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.approve";
	}

	@Override
	public final int getMinArgsLength() {
		return 0;
	}

	@Override
	public final String getUsage() {
		return "[player | uuid]";
	}

	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		if(!SkyowalletAPI.hasAccount((OfflinePlayer)sender)) {
			sender.sendMessage(Skyowallet.messages.message33);
			return true;
		}
		
		final SkyowalletAccount account = SkyowalletAPI.getAccount((OfflinePlayer)sender);
		if(!account.isBankOwner() && !sender.hasPermission("skyowallet.admin")) {
			sender.sendMessage(Skyowallet.messages.message1);
			return true;
		}
		
		if(args.length == 0) {
			for(final SkyowalletAccount pendingAccount : account.getBank().getPendingMembers()) {
				final OfflinePlayer pending = Bukkit.getOfflinePlayer(pendingAccount.getUUID());
				sender.sendMessage(ChatColor.AQUA + (pending == null ? pendingAccount.getUUID().toString() : pending.getName()));
			}
			sender.sendMessage(ChatColor.GRAY + "-----------------------------------------------------");
			sender.sendMessage(Skyowallet.messages.message43);
			return true;
		}
		
		final OfflinePlayer player = Utils.getPlayerByArgument(args[0]);
		if(player == null || !SkyowalletAPI.hasAccount(player)) {
			sender.sendMessage(Skyowallet.messages.message3);
			return true;
		}
		
		final SkyowalletAccount playerAccount = SkyowalletAPI.getAccount(player);
		final SkyowalletBank bank = playerAccount.getBankRequest();
		if(bank == null) {
			sender.sendMessage(Skyowallet.messages.message37);
			return true;
		}
		
		if(account.getBank() == null || !account.getBank().equals(bank)) {
			sender.sendMessage(Skyowallet.messages.message1);
			return true;
		}
		
		playerAccount.setBankRequest(null, false);
		playerAccount.setBank(bank);
		sender.sendMessage(Skyowallet.messages.message10);
		
		if(player.isOnline()) {
			player.getPlayer().sendMessage(Skyowallet.messages.message39.replace("/player/", sender.getName()).replace("/bank/", bank.getName()));
		}
		return true;
	}

}