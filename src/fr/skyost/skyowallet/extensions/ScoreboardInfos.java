package fr.skyost.skyowallet.extensions;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import com.google.common.primitives.Ints;

import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletAccount;
import fr.skyost.skyowallet.events.BankBalanceChangeEvent;
import fr.skyost.skyowallet.events.SyncEndEvent;
import fr.skyost.skyowallet.events.WalletChangeEvent;
import fr.skyost.skyowallet.utils.SimpleScoreboard;
import fr.skyost.skyowallet.utils.Skyoconfig;

public class ScoreboardInfos extends SkyowalletExtension {
	
	private ExtensionConfig config;
	
	public ScoreboardInfos(final Plugin plugin) {
		super(plugin);
	}
	
	@Override
	public final String getName() {
		return "ScoreboardInfos";
	}

	@Override
	public final HashMap<String, PermissionDefault> getPermissions() {
		return null;
	}
	
	@Override
	public final Skyoconfig getConfiguration() {
		if(config == null) {
			config = new ExtensionConfig(this.getConfigurationFile());
		}
		return config;
	}
	
	@Override
	public final boolean isEnabled() {
		return config.enable;
	}
	
	@EventHandler
	private final void onPlayerJoin(final PlayerJoinEvent event) {
		buildAndSend(event.getPlayer(), null, null);
	}
	
	@EventHandler
	private final void onPlayerQuit(final PlayerQuitEvent event) {
		event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}
	
	@EventHandler
	private final void onWalletChange(final WalletChangeEvent event) {
		final Player player = Bukkit.getPlayer(event.getAccount().getUUID());
		if(player != null) {
			buildAndSend(player, event.getNewWallet(), null);
		}
	}
	
	@EventHandler
	private final void onBankBalanceChangeEvent(final BankBalanceChangeEvent event) {
		final Player player = Bukkit.getPlayer(event.getAccount().getUUID());
		if(player != null) {
			buildAndSend(player, null, event.getNewBankBalance());
		}
	}
	
	@EventHandler
	private final void onSyncEnd(final SyncEndEvent event) {
		for(final Player player : Bukkit.getOnlinePlayers()) {
			buildAndSend(player, null, null);
		}
	}
	
	/**
	 * Build and send data to a player (in the scoreboard).
	 * 
	 * @param player The player.
	 * @param wallet His wallet.
	 * @param bankBalance His bank balance.
	 */
	
	private final void buildAndSend(final Player player, final Double wallet, final Double bankBalance) {
		final SkyowalletAccount account = SkyowalletAPI.getAccount(player);
		final SimpleScoreboard playerBoard = new SimpleScoreboard(config.sidebarTitle);
		playerBoard.add(config.sidebarWalletText, Ints.checkedCast(Math.round(wallet == null ? account.getWallet() : wallet)));
		playerBoard.add(config.sidebarBankBalanceText, Ints.checkedCast(Math.round(bankBalance == null ? account.getBankBalance() : bankBalance)));
		playerBoard.build();
		playerBoard.send(player);
	}
	
	public class ExtensionConfig extends Skyoconfig {

		@ConfigOptions(name = "scoreboardinfos.enable")
		public boolean enable = true;
		@ConfigOptions(name = "sidebar.title")
		public String sidebarTitle = ChatColor.BOLD + "Economy :";
		@ConfigOptions(name = "sidebar.wallet.display")
		public boolean sidebarWalletDisplay = true;
		@ConfigOptions(name = "sidebar.wallet.text")
		public String sidebarWalletText = ChatColor.GOLD + "Wallet:";
		@ConfigOptions(name = "sidebar.bank-balance.display")
		public boolean sidebarBankBalanceDisplay = true;
		@ConfigOptions(name = "sidebar.bank-balance.text")
		public String sidebarBankBalanceText = ChatColor.GOLD + "Bank balance:";
		
		private ExtensionConfig(final File file) {
			super(file, Arrays.asList("ScoreboardInfos Configuration"));
		}
		
	}

}
