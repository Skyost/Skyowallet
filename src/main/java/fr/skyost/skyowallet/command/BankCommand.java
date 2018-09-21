package fr.skyost.skyowallet.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.AmountPlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.BankPlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.CurrencyNamePlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.util.Utils;

/**
 * Represents the <em>/bank</em> command.
 */

public class BankCommand extends SubCommandsExecutor {

	/**
	 * Creates a new bank command instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public BankCommand(final Skyowallet skyowallet) {
		super(skyowallet, "bank");
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, String[] args) {
		if(args.length <= 0) {
			if(!(sender instanceof Player)) {
				sendUsage(sender);
				return true;
			}

			final Skyowallet skyowallet = getSkyowallet();
			final SkyowalletAccountManager accountManager = skyowallet.getAccountManager();

			if(!accountManager.has((Player)sender)) {
				sender.sendMessage(skyowallet.getPluginMessages().messageNoAccount);
				return true;
			}

			final SkyowalletAccount account = accountManager.get((Player)sender);
			final SkyowalletBank bank = account.getBank();
			if(bank == null) {
				sender.sendMessage(skyowallet.getPluginMessages().messageNoBankAccount);
				return true;
			}

			if(bank.isOwner(account)) {
				sender.sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageBankOwner, new BankPlaceholder(bank)));
			}
			else {
				final String owners;
				final StringBuilder builder = new StringBuilder();
				for(final SkyowalletAccount ownerAccount : bank.getOwners()) {
					final OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerAccount.getUUID());
					builder.append((owner == null ? ownerAccount.getUUID().toString() : Utils.getName(owner))).append(", ");
				}
				owners = builder.toString();
				sender.sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageBankMember, new BankPlaceholder(bank), new Placeholder("owners", owners.length() == 0 ? "X" : owners.substring(0, owners.length() - 2))));
			}

			final double bankBalance = account.getBankBalance().getAmount();
			sender.sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageBankInfo, new AmountPlaceholder(bankBalance), new CurrencyNamePlaceholder(bankBalance)));
			return true;
		}
		return super.onCommand(sender, command, label, args);
	}

}