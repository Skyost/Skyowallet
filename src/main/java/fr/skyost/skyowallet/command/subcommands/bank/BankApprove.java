package fr.skyost.skyowallet.command.subcommands.bank;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.BankPlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.PlayerPlaceholder;
import fr.skyost.skyowallet.util.Utils;

/**
 * Represents the <em>/bank approve</em> command.
 */

public class BankApprove implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"approve"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
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
	public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
		final Skyowallet skyowallet = command.getSkyowallet();
		if(sender instanceof Player && !playerTests(skyowallet, (Player)sender, args)) {
			return true;
		}
		else if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Console : " + getUsage().replace("[", "<").replace("]", ">"));
			return true;
		}

		final SkyowalletAccountManager accountManager = skyowallet.getAccountManager();

		final OfflinePlayer player = Utils.getPlayerByArgument(args[0]);
		if(player == null || !accountManager.has(player)) {
			sender.sendMessage(skyowallet.getPluginMessages().messagePlayerNoAccount);
			return true;
		}

		final SkyowalletAccount playerAccount = accountManager.get(player);
		final SkyowalletBank bank = playerAccount.getBankRequest();
		if(bank == null) {
			sender.sendMessage(skyowallet.getPluginMessages().messagePlayerBankNotRequested);
			return true;
		}

		playerAccount.setBankRequest(null);
		playerAccount.setBank(bank);
		sender.sendMessage(skyowallet.getPluginMessages().messageDone);
		
		if(player.isOnline()) {
			player.getPlayer().sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageBankRequestAccepted, new PlayerPlaceholder(sender), new BankPlaceholder(bank)));
		}
		return true;
	}

	/**
	 * Runs a series of tests that allow check if the specified player can approve (or deny) another specified player in its bank.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param player The player.
	 * @param args Command arguments.
	 *
	 * @return Whether the specified player can approve (or deny) the target in its bank.
	 */

	static boolean playerTests(final Skyowallet skyowallet, final Player player, final String... args) {
		return playerTests(skyowallet, player, args.length == 0 ? null : Utils.getPlayerByArgument(args[0]));
	}

	/**
	 * Runs a series of tests that allow check if the specified player can approve (or deny) another specified player in its bank.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param player The player.
	 * @param target The target player.
	 *
	 * @return Whether the specified player can approve (or deny) the target in its bank.
	 */

	static boolean playerTests(final Skyowallet skyowallet, final Player player, final OfflinePlayer target) {
		final SkyowalletAccountManager accountManager = skyowallet.getAccountManager();
		if(!accountManager.has(player)) {
			player.sendMessage(skyowallet.getPluginMessages().messageNoAccount);
			return false;
		}

		final SkyowalletAccount account = accountManager.get(player);
		if(!account.isBankOwner() && !player.hasPermission("skyowallet.admin")) {
			player.sendMessage(skyowallet.getPluginMessages().messageBankNoPermission);
			return false;
		}

		if(target == null) {
			for(final SkyowalletAccount pendingAccount : account.getBank().getPendingMembers()) {
				final OfflinePlayer pending = Bukkit.getOfflinePlayer(pendingAccount.getUUID());
				player.sendMessage(ChatColor.AQUA + (pending == null ? pendingAccount.getUUID().toString() : Utils.getName(pending)));
			}
			player.sendMessage(Utils.SEPARATOR);
			player.sendMessage(skyowallet.getPluginMessages().messageBankRequestCount);
			return false;
		}

		if(!accountManager.has(target)) {
			player.sendMessage(skyowallet.getPluginMessages().messagePlayerNoAccount);
			return false;
		}

		final SkyowalletAccount playerAccount = accountManager.get(target);
		final SkyowalletBank bank = playerAccount.getBankRequest();
		if(bank == null) {
			player.sendMessage(skyowallet.getPluginMessages().messagePlayerBankNotRequested);
			return false;
		}

		if(account.getBank() == null || !account.getBank().equals(bank)) {
			player.sendMessage(skyowallet.getPluginMessages().messageBankNoPermission);
			return false;
		}

		return true;
	}

}