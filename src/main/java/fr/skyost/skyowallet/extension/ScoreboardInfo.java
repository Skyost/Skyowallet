package fr.skyost.skyowallet.extension;

import com.google.common.primitives.Ints;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.event.account.WalletChangeEvent;
import fr.skyost.skyowallet.event.bank.BankBalanceChangeEvent;
import fr.skyost.skyowallet.event.sync.SyncEndEvent;

/**
 * ScoreboardInfo extension class.
 */

public class ScoreboardInfo extends SkyowalletExtension {

	/**
	 * The extension configuration.
	 */
	
	private ExtensionConfig config;

	/**
	 * All scoreboard data (key : UUID of the player, value : the data).
	 */

	private final HashMap<UUID, ScoreboardData> scoreboardData = new HashMap<>();

	/**
	 * Creates a new scoreboard info instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param plugin The parent plugin.
	 */
	
	public ScoreboardInfo(final Skyowallet skyowallet, final JavaPlugin plugin) {
		super(skyowallet, plugin, "Displays information in the sidebar.");
	}
	
	@Override
	public final SkyowalletExtensionConfig getConfiguration() {
		return config == null ? config = new ExtensionConfig() : config;
	}

	@Override
	public final void unload() throws InvalidConfigurationException {
		super.unload();
		for(final ScoreboardData scoreboardData : scoreboardData.values()) {
			scoreboardData.reset();
		}
	}
	
	@EventHandler
	private void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		if(getSkyowallet().getAccountManager().has(player)) {
			scoreboardData.put(player.getUniqueId(), new ScoreboardData(player));
		}
	}
	
	@EventHandler
	private void onPlayerQuit(final PlayerQuitEvent event) {
		final ScoreboardData data = scoreboardData.remove(event.getPlayer().getUniqueId());
		if(data != null) {
			data.reset();
		}
	}
	
	@EventHandler
	private void onWalletChange(final WalletChangeEvent event) {
		if(event.isCancelled()) {
			return;
		}
		final ScoreboardData data = scoreboardData.get(event.getAccount().getUUID());
		if(data != null) {
			data.update(Bukkit.getPlayer(event.getAccount().getUUID()), event.getNewAmount());
		}
	}
	
	@EventHandler
	private void onBankBalanceChangeEvent(final BankBalanceChangeEvent event) {
		if(event.isCancelled()) {
			return;
		}
		final ScoreboardData data = scoreboardData.get(event.getAccount().getUUID());
		if(data != null) {
			data.update(Bukkit.getPlayer(event.getAccount().getUUID()), null, event.getNewAmount());
		}
	}
	
	@EventHandler
	private void onSyncEnd(final SyncEndEvent event) {
		for(final Map.Entry<UUID, ScoreboardData> entry : scoreboardData.entrySet()) {
			entry.getValue().update(Bukkit.getPlayer(entry.getKey()));
		}
	}

	/**
	 * Represents the extension configuration.
	 */
	
	public class ExtensionConfig extends SkyowalletExtensionConfig {

		@ConfigOptions(name = "sidebar.title")
		public String sidebarTitle = ChatColor.BOLD + "ECONOMY";
		@ConfigOptions(name = "sidebar.wallet.display")
		public boolean sidebarWalletDisplay = true;
		@ConfigOptions(name = "sidebar.wallet.text")
		public String sidebarWalletText = ChatColor.GOLD + "Wallet:";
		@ConfigOptions(name = "sidebar.bank-balance.display")
		public boolean sidebarBankBalanceDisplay = true;
		@ConfigOptions(name = "sidebar.bank-balance.text")
		public String sidebarBankBalanceText = ChatColor.GOLD + "Bank balance:";
		
	}

	/**
	 * Represents a Scoreboard data.
	 */

	public class ScoreboardData {

		/**
		 * The scoreboard objective.
		 */

		private final Objective economy;

		/**
		 * Creates a new scoreboard data instance.
		 *
		 * @param player The player.
		 */

		public ScoreboardData(final Player player) {
			final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
			economy = scoreboard.registerNewObjective(getName(), "dummy");
			economy.setDisplayName(config.sidebarTitle);
			economy.setDisplaySlot(DisplaySlot.SIDEBAR);

			player.setScoreboard(scoreboard);
			update(player);
		}

		/**
		 * Updates the scoreboard objective for the corresponding player.
		 *
		 * @param player The player.
		 */

		public void update(final Player player) {
			update(player, null);
		}

		/**
		 * Updates the scoreboard objective for the corresponding player.
		 *
		 * @param player The player.
		 * @param wallet The new wallet.
		 */

		public void update(final Player player, final Double wallet) {
			update(player, wallet, null);
		}

		/**
		 * Updates the scoreboard objective for the corresponding player.
		 *
		 * @param player The player.
		 * @param wallet The new wallet.
		 * @param bankBalance The new bank balance.
		 */

		public void update(final Player player, final Double wallet, final Double bankBalance) {
			if(player == null) {
				return;
			}

			final SkyowalletAccount account = getSkyowallet().getAccountManager().get(player);
			if(config.sidebarBankBalanceDisplay) {
				economy.getScore(config.sidebarBankBalanceText).setScore(Ints.checkedCast(Math.round(bankBalance == null ? account.getBankBalance().getAmount() : bankBalance)));
			}
			if(config.sidebarWalletDisplay) {
				economy.getScore(config.sidebarWalletText).setScore(Ints.checkedCast(Math.round(wallet == null ? account.getWallet().getAmount() : wallet)));
			}
		}

		/**
		 * Reset the player's scoreboard.
		 */

		public void reset() {
			economy.unregister();
		}

	}

}
