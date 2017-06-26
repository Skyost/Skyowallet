package fr.skyost.skyowallet.tasks;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.SkyowalletAPI;

public class SyncTask extends Thread {
	
	private final boolean silentMode;
	private final CommandSender sender;
	
	public SyncTask(final boolean silentMode) {
		this(silentMode, Bukkit.getConsoleSender());
	}
	
	public SyncTask(final boolean silentMode, final CommandSender sender) {
		this.silentMode = silentMode;
		this.sender = sender;
	}

	@Override
	public final void run() {
		SkyowalletAPI.sync(silentMode ? null : sender);
	}

}