package fr.skyost.skyowallet.sync;

import com.google.common.io.Files;
import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.sync.queue.FullSyncQueue;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Allows to import old accounts and banks.
 */

public class LegacyImportTask extends BukkitRunnable {

	/**
	 * The Skyowallet instance.
	 */

	private final Skyowallet skyowallet;

	/**
	 * Creates a new legacy import task.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public LegacyImportTask(final Skyowallet skyowallet) {
		this.skyowallet = skyowallet;
	}

	@Override
	public void run() {
		final ConsoleCommandSender console = Bukkit.getConsoleSender();

		try {
			console.sendMessage(ChatColor.AQUA + "Starting to import legacy accounts and banks...");

			final File accountsDirectory = skyowallet.getPluginConfig().getAccountsDirectory();
			for(final File accountFile : accountsDirectory.listFiles()) {
				final SkyowalletAccount account = skyowallet.getAccountFactory().createFromJSON(Files.readFirstLine(accountFile, StandardCharsets.UTF_8));
				skyowallet.getAccountManager().add(account);
			}

			final File banksDirectory =  skyowallet.getPluginConfig().getBanksDirectory();
			for(final File bankFile : banksDirectory.listFiles()) {
				final SkyowalletBank bank = skyowallet.getBankFactory().createFromJSON(Files.readFirstLine(bankFile, StandardCharsets.UTF_8));
				skyowallet.getBankManager().add(bank);
			}

			console.sendMessage(ChatColor.AQUA + "Running a full synchronization...");

			skyowallet.getSyncManager().synchronize(new FullSyncQueue(skyowallet.getSyncManager(), null));
			deleteDirectory(accountsDirectory);
			deleteDirectory(banksDirectory);

			console.sendMessage(ChatColor.AQUA + "Import finished !");
		}
		catch(final Exception ex) {
			console.sendMessage(ChatColor.RED + "Unable to import old accounts and banks !");
			ex.printStackTrace();
		}
	}

	/**
	 * Deletes a directory.
	 *
	 * @param directory The directory.
	 */

	private static void deleteDirectory(final File directory) {
		final File[] files = directory.listFiles();
		if(files != null) {
			for(final File file : files) {
				deleteDirectory(file);
			}
		}

		directory.delete();
	}

}