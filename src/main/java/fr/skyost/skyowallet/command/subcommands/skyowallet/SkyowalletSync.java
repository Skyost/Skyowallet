package fr.skyost.skyowallet.command.subcommands.skyowallet;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;
import fr.skyost.skyowallet.sync.SyncTask;
import fr.skyost.skyowallet.sync.queue.FullSyncQueue;
import fr.skyost.skyowallet.sync.queue.SyncQueue;
import fr.skyost.skyowallet.util.Util;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.HashSet;

/**
 * Represents the <em>/skyowallet sync</em> command.
 */

public class SkyowalletSync implements CommandInterface {

	@Override
	public final String[] getNames() {
		return new String[]{"sync", "reload"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.sync";
	}

	@Override
	public final int getMinArgsLength() {
		return 0;
	}

	@Override
	public final String getUsage() {
		return "[players | uuids]";
	}

	@Override
	public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
		final Skyowallet skyowallet = command.getSkyowallet();
		final SkyowalletAccountManager accountManager = skyowallet.getAccountManager();

		final HashSet<SkyowalletAccount> accounts = new HashSet<>();
		if(args != null) {
			for(final String arg : args) {
				final OfflinePlayer player = Util.getPlayerByArgument(arg);
				if(accountManager.has(player)) {
					accounts.add(accountManager.get(player));
				}
			}
		}

		if(accounts.isEmpty()) {
			SyncTask.runDefaultSync(new FullSyncQueue(skyowallet.getSyncManager(), sender));
			return true;
		}

		final SyncQueue syncQueue = new SyncQueue(skyowallet.getSyncManager(), sender);
		syncQueue.addToQueue(accounts);
		SyncTask.runDefaultSync(syncQueue);
		return true;
	}

}