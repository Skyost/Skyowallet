package fr.skyost.skyowallet.tasks;

import org.bukkit.Bukkit;

import fr.skyost.skyowallet.SkyowalletAPI;

public class SyncTask implements Runnable {

	@Override
	public final void run() {
		SkyowalletAPI.sync(Bukkit.getConsoleSender());
	}

}
