package fr.skyost.skyowallet.commands.subcommands.bank;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletAccount;
import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletBank;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.Utils;

public class BankInfos implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"infos"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.infos";
	}

	@Override
	public final int getMinArgsLength() {
		return 0;
	}

	@Override
	public final String getUsage() {
		return "[bank]";
	}

	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		SkyowalletAccount account = null;
		final SkyowalletBank bank;
		if(args.length < 1) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Console : " + getUsage().replace("[", "<").replace("]", ">"));
				return true;
			}
			account = SkyowalletAPI.getAccount((Player)sender);
			bank = account.getBank();
		}
		else {
			if(sender instanceof Player) {
				account = SkyowalletAPI.getAccount((Player)sender);
			}
			bank = SkyowalletAPI.getBank(args[0]);
		}
		if(bank == null) {
			sender.sendMessage(ChatColor.RED + (args.length < 1 ? Skyowallet.messages.message21 : Skyowallet.messages.message19));
			return true;
		}
		if(sender.hasPermission("skyowallet.admin") || account == null ? !(sender instanceof Player) : bank.isOwner(account)) {
			final HashMap<SkyowalletAccount, Double> members = bank.getMembers();
			for(final Entry<SkyowalletAccount, Double> entry : members.entrySet()) {
				final SkyowalletAccount bankAccount = entry.getKey();
				final String uuid = Utils.uuidAddDashes(bankAccount.getUUID());
				final OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
				final Double balance = entry.getValue();
				sender.sendMessage((player == null ? uuid : player.getName()) + " " + balance + " " + SkyowalletAPI.getCurrencyName(balance));
			}
			final StringBuilder separator = new StringBuilder();
			for(int i = 0; i != ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH; i++) {
				separator.append("-");
			}
			sender.sendMessage(ChatColor.GRAY + separator.toString());
			sender.sendMessage((args.length < 1 ? Skyowallet.messages.message22 : Skyowallet.messages.message23).replace("/members/", String.valueOf(members.size())));
		}
		else {
			sender.sendMessage(Skyowallet.messages.message28);
		}
		return true;
	}

}
