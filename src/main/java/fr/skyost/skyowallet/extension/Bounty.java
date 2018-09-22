package fr.skyost.skyowallet.extension;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.CurrencyNamePlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.PlayerPlaceholder;
import fr.skyost.skyowallet.util.Utils;

/**
 * Bounty extension class.
 */

public class Bounty extends SkyowalletExtension {

	/**
	 * The extension configuration.
	 */
	
	private ExtensionConfig config;

	/**
	 * All bounties (key : player's UUID, value : the corresponding bounty).
	 */

	private final HashMap<UUID, BountyTarget> bounties = new HashMap<>();

	/**
	 * Creates a new bounty instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param plugin The parent plugin.
	 */
	
	public Bounty(final Skyowallet skyowallet, final JavaPlugin plugin) {
		super(skyowallet, plugin, "Place bounties on the head of other players !");
	}
	
	@Override
	public void load() throws InvalidConfigurationException {
		super.load();
		try {
			for(final String bountyData : config.bountiesData) {
				final BountyTarget bountyTarget = bountyFromJson(bountyData);
				bounties.put(bountyTarget.getTarget(), bountyTarget);
			}
			config.bountiesData.clear();
		}
		catch(final Exception ex) {
			throw new InvalidConfigurationException(ex);
		}
	}

	@Override
	public final Map<String, PermissionDefault> getPermissions() {
		final Map<String, PermissionDefault> permissions = new HashMap<>();
		permissions.put("skyowallet.bounties.add", PermissionDefault.TRUE);
		permissions.put("skyowallet.bounties.remove", PermissionDefault.TRUE);
		permissions.put("skyowallet.bounties.delete", PermissionDefault.OP);
		permissions.put("skyowallet.bounties.get", PermissionDefault.TRUE);
		permissions.put("skyowallet.bounties.get.player", PermissionDefault.TRUE);
		permissions.put("skyowallet.bounties.list", PermissionDefault.TRUE);
		return permissions;
	}
	
	@Override
	public Map<String, CommandExecutor> getCommands() {
		final BountyCommand bountyCmd = new BountyCommand(getSkyowallet());
		for(final CommandInterface command : new CommandInterface[]{new BountyAdd(), new BountyRemove(), new BountyDelete(), new BountyGet(), new BountyList()}) {
			bountyCmd.registerSubCommand(command);
		}
		final Map<String, CommandExecutor> commands = new HashMap<>();
		commands.put("bounty", bountyCmd);
		return commands;
	}
	
	@Override
	public final SkyowalletExtensionConfig getConfiguration() {
		return config == null ? config = new ExtensionConfig() : config;
	}
	
	@Override
	public final void unload() throws InvalidConfigurationException {
		for(final BountyTarget bountyTarget : bounties.values()) {
			final String json = bountyTarget.toString();
			if(json == null) {
				continue;
			}
			config.bountiesData.add(json);
		}
		bounties.clear();
		super.unload();
	}
	
	@EventHandler
	public final void onPlayerJoin(final PlayerJoinEvent event) {
		getBounty(event.getPlayer().getUniqueId()).refreshPlayerListName();
	}
	
	@EventHandler
	public final void onPlayerDeath(final PlayerDeathEvent event) {
		final Player player = event.getEntity();
		final Player killer = player.getKiller();
		if(killer == null) {
			return;
		}
		final BountyTarget bountyTarget = getBounty(player.getUniqueId());
		final double amount = bountyTarget.getTotalBounty();
		if(amount != 0d) {
			final SkyowalletAccountManager accountManager = getSkyowallet().getAccountManager();
			if(!accountManager.has(killer)) {
				killer.sendMessage(PlaceholderFormatter.defaultFormat(config.messageNoAccount, (OfflinePlayer)player));
				return;
			}
			final SkyowalletAccount account = accountManager.get(killer);
			account.getWallet().addAmount(amount);
			if(config.bountyNotify) {
				killer.sendMessage(PlaceholderFormatter.defaultFormat(config.messageBountyAwarded, (OfflinePlayer)player, amount));
			}
		}
		bountyTarget.clearBounties();
	}
	
	/**
	 * Returns the bounty for the specified UUID.
	 * 
	 * @param target The UUID.
	 * 
	 * @return The bounty corresponding to the specified UUID or a blank bounty if the specified target UUID does not exist.
	 */
	
	public final BountyTarget getBounty(final UUID target) {
		return bounties.containsKey(target) ? bounties.get(target) : new BountyTarget(target);
	}
	
	/**
	 * Constructs an instance from a JSON String.
	 * 
	 * @param json The JSON String.
	 * 
	 * @return A new instance of this class.
	 * 
	 * @throws ParseException If an error occurred while parsing the data.
	 */
	
	private BountyTarget bountyFromJson(final String json) throws ParseException {
		final JSONObject jsonObject = (JSONObject)JSONValue.parseWithException(json);
		Object target = jsonObject.get("target");
		if(target == null) {
			throw new NullPointerException("Target player is null.");
		}
		target = Utils.uuidTryParse(target.toString());
		if(target == null) {
			throw new IllegalArgumentException("This is not a true UUID !");
		}
		final Object jsonBounties = jsonObject.get("bounties");
		if(jsonBounties == null) {
			throw new NullPointerException("BountyTarget data is null.");
		}
		final BountyTarget currentBountyTarget = new BountyTarget((UUID)target);
		for(final Object bounty : (JSONArray)jsonBounties) {
			final JSONObject jsonBounty = (JSONObject)bounty;
			Object uuid = jsonBounty.get("uuid");
			if(uuid == null) {
				throw new NullPointerException("BountyTarget UUID is null.");
			}
			uuid = Utils.uuidTryParse(uuid.toString());
			if(uuid == null) {
				throw new IllegalArgumentException("This is not a true UUID !");
			}
			final Object amount = jsonBounty.get("amount");
			if(amount == null) {
				throw new NullPointerException("BountyTarget amount is null.");
			}
			currentBountyTarget.setBounty((UUID)uuid, Double.parseDouble(amount.toString()));
		}
		return currentBountyTarget;
	}

	/**
	 * Represents the <em>/bounty</em> command.
	 */
	
	public class BountyCommand extends SubCommandsExecutor {

		/**
		 * Creates a new bounty command instance.
		 *
		 * @param skyowallet The Skyowallet instance.
		 */

		BountyCommand(final Skyowallet skyowallet) {
			super(skyowallet, "bounty");
		}

		@Override
		public boolean onCommand(final CommandSender sender, final Command command, final String label, String[] args) {
			if(args.length == 0) {
				return false;
			}
			return super.onCommand(sender, command, label, args);
		}
		
	}

	/**
	 * Represents the <em>/bounty add</em> command.
	 */
	
	public class BountyAdd implements CommandInterface {
		
		@Override
		public final String[] getNames() {
			return new String[]{"add", "put"};
		}

		@Override
		public final boolean mustBePlayer() {
			return true;
		}

		@Override
		public final String getPermission() {
			return "skyowallet.bounties.add";
		}

		@Override
		public final int getMinArgsLength() {
			return 2;
		}

		@Override
		public final String getUsage() {
			return "<player> <amount>";
		}

		@Override
		public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
			final OfflinePlayer player = Utils.getPlayerByArgument(args[0]);
			if(player == null) {
				sender.sendMessage(config.messageUnexistingPlayer);
				return true;
			}
			final UUID uuid = player.getUniqueId();
			final Double amount = Utils.doubleTryParse(args[1]);
			if(amount == null) {
				sender.sendMessage(getSkyowallet().getPluginMessages().messageInvalidAmount);
				return true;
			}
			if(amount < config.bountyMin || amount > config.bountyMax) {
				sender.sendMessage(PlaceholderFormatter.format(config.messageBountyBounds, new Placeholder("bountyTarget-min", String.valueOf(config.bountyMin)), new Placeholder("bountyTarget-max", String.valueOf(config.bountyMax)), new CurrencyNamePlaceholder(config.bountyMax)));
				return true;
			}
			final SkyowalletAccount account = command.getSkyowallet().getAccountManager().get((OfflinePlayer)sender);
			if(!account.getWallet().canSubtract(amount)) {
				sender.sendMessage(config.messageNotEnoughMoney);
				return true;
			}
			account.getWallet().subtractAmount(amount);
			final BountyTarget bountyTarget = getBounty(uuid);
			if(!bounties.containsKey(uuid)) {
				bounties.put(uuid, bountyTarget);
			}
			bountyTarget.setBounty(account.getUUID(), amount);
			sender.sendMessage(PlaceholderFormatter.defaultFormat(PlaceholderFormatter.format(config.messageBountyValidated, new Placeholder("bounty", String.valueOf(bountyTarget.getTotalBounty()))), player, amount));
			if(config.bountyNotify && player.isOnline()) {
				player.getPlayer().sendMessage(PlaceholderFormatter.defaultFormat(config.messagePlayerBountyPut, sender, amount));
			}
			return true;
		}
		
	}

	/**
	 * Represents the <em>/bounty remove</em> command.
	 */
	
	public class BountyRemove implements CommandInterface {
		
		@Override
		public final String[] getNames() {
			return new String[]{"remove"};
		}

		@Override
		public final boolean mustBePlayer() {
			return true;
		}

		@Override
		public final String getPermission() {
			return "skyowallet.bounties.remove";
		}

		@Override
		public final int getMinArgsLength() {
			return 1;
		}

		@Override
		public final String getUsage() {
			return "<player>";
		}

		@Override
		public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
			final OfflinePlayer player = Utils.getPlayerByArgument(args[0]);
			if(player == null) {
				sender.sendMessage(config.messageUnexistingPlayer);
				return true;
			}
			final UUID uuid = ((OfflinePlayer)sender).getUniqueId();
			final BountyTarget bountyTarget = getBounty(player.getUniqueId());
			if(!bountyTarget.bounties.containsKey(uuid)) {
				sender.sendMessage(PlaceholderFormatter.defaultFormat(config.messageNoBounty, player));
				return true;
			}
			final SkyowalletAccount account = command.getSkyowallet().getAccountManager().get(uuid);
			account.getWallet().addAmount(bountyTarget.bounties.get(uuid));
			bountyTarget.removeBounty(uuid);
			sender.sendMessage(PlaceholderFormatter.format(config.messageBountyRemoved, new PlayerPlaceholder(player)));
			if(config.bountyNotify && player.isOnline()) {
				player.getPlayer().sendMessage(PlaceholderFormatter.defaultFormat(config.messagePlayerBountyRemoved, sender));
			}
			return true;
		}
		
	}

	/**
	 * Represents the <em>/bounty delete</em> command.
	 */
	
	public class BountyDelete implements CommandInterface {
		
		@Override
		public final String[] getNames() {
			return new String[]{"delete"};
		}

		@Override
		public final boolean mustBePlayer() {
			return false;
		}

		@Override
		public final String getPermission() {
			return "skyowallet.bounties.delete";
		}

		@Override
		public final int getMinArgsLength() {
			return 1;
		}

		@Override
		public final String getUsage() {
			return "<player>";
		}

		@Override
		public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
			final OfflinePlayer player = Utils.getPlayerByArgument(args[0]);
			if(player == null) {
				sender.sendMessage(config.messageUnexistingPlayer);
				return true;
			}
			final BountyTarget bountyTarget = getBounty(player.getUniqueId());
			if(bountyTarget.getTotalBounty() == 0d) {
				sender.sendMessage(PlaceholderFormatter.defaultFormat(config.messagePlayerNoBounty, player));
				return true;
			}
			if(config.bountyGiveBackIfDeleted) {
				for(final Entry<UUID, Double> entry : bountyTarget.bounties.entrySet()) {
					final OfflinePlayer offline = Bukkit.getOfflinePlayer(entry.getKey());
					final SkyowalletAccount account = command.getSkyowallet().getAccountManager().get(offline);
					if(account == null) {
						continue;
					}
					final double amount = entry.getValue();
					account.getWallet().addAmount(amount);
					if(offline.isOnline()) {
						((Player)offline).sendMessage(PlaceholderFormatter.defaultFormat(PlaceholderFormatter.format(config.messageBountiesCleared, new Placeholder("target", Utils.getName(player))), sender, amount));
					}
				}
			}
			bountyTarget.clearBounties();
			sender.sendMessage(PlaceholderFormatter.defaultFormat(config.messageBountiesRemoved, player));
			if(config.bountyNotify && player.isOnline()) {
				player.getPlayer().sendMessage(PlaceholderFormatter.defaultFormat(config.messagePlayerBountiesRemoved, sender));
			}
			return true;
		}
		
	}

	/**
	 * Represents the <em>/bounty get</em> command.
	 */
	
	public class BountyGet implements CommandInterface {
		
		@Override
		public final String[] getNames() {
			return new String[]{"get"};
		}

		@Override
		public final boolean mustBePlayer() {
			return false;
		}

		@Override
		public final String getPermission() {
			return "skyowallet.bounties.get";
		}

		@Override
		public final int getMinArgsLength() {
			return 0;
		}

		@Override
		public final String getUsage() {
			return "[player]";
		}

		@Override
		public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
			OfflinePlayer player;
			if(args.length >= 1) {
				if(!sender.hasPermission(getPermission() + ".player")) {
					sender.sendMessage(command.getSkyowallet().getPluginMessages().messageNoPermission);
					return true;
				}
				player = Utils.getPlayerByArgument(args[0]);
				if(player == null) {
					sender.sendMessage(config.messageUnexistingPlayer);
					return true;
				}
			}
			else {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Console : " + getUsage().replace("[", "<").replace("]", ">"));
					return true;
				}
				player = (OfflinePlayer)sender;
			}
			final double amount = getBounty(player.getUniqueId()).getTotalBounty();
			sender.sendMessage(PlaceholderFormatter.defaultFormat(config.messageBountyReward, player, amount));
			return true;
		}
		
	}

	/**
	 * Represents the <em>/bounty list</em> command.
	 */
	
	public class BountyList implements CommandInterface {
		
		@Override
		public final String[] getNames() {
			return new String[]{"list"};
		}

		@Override
		public final boolean mustBePlayer() {
			return false;
		}

		@Override
		public final String getPermission() {
			return "skyowallet.bounties.list";
		}

		@Override
		public final int getMinArgsLength() {
			return 0;
		}

		@Override
		public final String getUsage() {
			return null;
		}

		@Override
		public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
			if(bounties.size() == 0) {
				sender.sendMessage(config.messageNoBountyServer);
				return true;
			}
			final List<BountyTarget> bounties = new ArrayList<>(Bounty.this.bounties.values());
			bounties.sort(Comparator.comparingDouble(BountyTarget::getTotalBounty));
			for(final BountyTarget bountyTarget : bounties) {
				final double amount = bountyTarget.getTotalBounty();
				final OfflinePlayer player = Bukkit.getOfflinePlayer(bountyTarget.getTarget());
				if(player == null) {
					sender.sendMessage(PlaceholderFormatter.defaultFormat(config.messageBountyRanking, bountyTarget.getTarget(), amount));
				}
				else {
					sender.sendMessage(PlaceholderFormatter.defaultFormat(config.messageBountyRanking, player, amount));
				}
			}
			sender.sendMessage(Utils.SEPARATOR);
			sender.sendMessage(PlaceholderFormatter.format(config.messageAvailableBounties, new Placeholder("bounties", String.valueOf(bounties.size()))));
			return true;
		}
		
	}

	/**
	 * Represents the extension configuration.
	 */
	
	public class ExtensionConfig extends SkyowalletExtensionConfig {

		@ConfigOptions(name = "bounty.min")
		public double bountyMin = 10.0;
		@ConfigOptions(name = "bounty.max")
		public double bountyMax = 100000.0;
		@ConfigOptions(name = "bounty.notify")
		public boolean bountyNotify = true;
		@ConfigOptions(name = "bounty.give-back-if-deleted")
		public boolean bountyGiveBackIfDeleted = true;
		
		@ConfigOptions(name = "player-list.enable")
		public boolean playerListEnable = true;
		@ConfigOptions(name = "player-list.display")
		public String playerListDisplay = "/player/ " + ChatColor.GRAY + "> " + ChatColor.RED + "BOUNTY: " + ChatColor.GOLD + "/amount/ /currency-name/";
		
		@ConfigOptions(name = "messages.unexisting-player")
		public String messageUnexistingPlayer = ChatColor.RED + "This player does not exist.";
		@ConfigOptions(name = "messages.bounty-bounds")
		public String messageBountyBounds = ChatColor.RED + "The bounty must be between /bounty-min/ and /bounty-max/ /currency-name/.";
		@ConfigOptions(name = "messages.not-enough-money")
		public String messageNotEnoughMoney = ChatColor.RED + "You do not have enough money in your wallet !";
		@ConfigOptions(name = "messages.bounty-validated")
		public String messageBountyValidated = ChatColor.GOLD + "You just put /amount/ /currency-name/ on the head of /player/. The total bounty on his head is /bounty/ /currency-name/.";
		@ConfigOptions(name = "messages.player-bounty-put")
		public String messagePlayerBountyPut = ChatColor.DARK_RED + "/player/ just put /amount/ /currency-name/ on your head !";
		@ConfigOptions(name = "messages.no-bounty")
		public String messageNoBounty = ChatColor.RED + "You did not put any bounty on the head of /player/ !";
		@ConfigOptions(name = "messages.bounty-removed")
		public String messageBountyRemoved = ChatColor.GREEN + "You just removed the bounty you put on the head of /player/ !";
		@ConfigOptions(name = "messages.player-bounty-removed")
		public String messagePlayerBountyRemoved = ChatColor.DARK_GREEN + "/player/ just removed the bounty he puts on your head.";
		@ConfigOptions(name = "messages.player-no-bounty")
		public String messagePlayerNoBounty = ChatColor.RED + "/player/ did not have any bounty on his head.";
		@ConfigOptions(name = "messages.bounties-removed")
		public String messageBountiesRemoved = ChatColor.GREEN + "You just removed the bounties that were targeting /player/.";
		@ConfigOptions(name = "messages.player-bounties-removed")
		public String messagePlayerBountiesRemoved = ChatColor.DARK_GREEN + "/player/ just removed all bounties on your head.";
		@ConfigOptions(name = "messages.bounties-cleared")
		public String messageBountiesCleared = ChatColor.GOLD + "/player/ just removed all bounties on the head of /target/. Therefore, /amount/ /currency-name/ are added back to your wallet.";
		@ConfigOptions(name = "messages.bounty-reward")
		public String messageBountyReward = "The reward to kill " + ChatColor.GOLD + "/player/" + ChatColor.RESET + " is " + ChatColor.GOLD + "/amount/ /currency-name/" + ChatColor.RESET + ".";
		@ConfigOptions(name = "messages.no-bounty-server")
		public String messageNoBountyServer = ChatColor.RED + "There is currently no bounty on this server.";
		@ConfigOptions(name = "messages.bounty-ranking")
		public String messageBountyRanking = ChatColor.GOLD + "- /player/" + ChatColor.RESET + " is wanted for " + ChatColor.GOLD + "/amount/ /currency-name/" + ChatColor.RESET + ".";
		@ConfigOptions(name = "messages.available-bounties")
		public String messageAvailableBounties = "There are currently " + ChatColor.GOLD + "/bounties/" + ChatColor.RESET +  " available bounties on this server.";
		@ConfigOptions(name = "messages.bounty-awarded")
		public String messageBountyAwarded = ChatColor.GOLD + "You just earned /amount/ /currency-name/ for killing /player/.";
		@ConfigOptions(name = "messages.no-account")
		public String messageNoAccount = ChatColor.RED + "You do not have an economy account on this server. Therefore, the bounty is still on the head of /player/.";
		
		@ConfigOptions(name = "bounties-data")
		public List<String> bountiesData = new ArrayList<>();
		
	}
	
	/**
	 * Inner class that represents a bounty.
	 */
	
	public class BountyTarget {

		/**
		 * The target.
		 */

		private final UUID target;

		/**
		 * All bounties placed on the target (key : the UUID of the player who put the bounty, value : the bounty value).
		 */

		private final Map<UUID, Double> bounties = new HashMap<>();
		
		/**
		 * Creates a new bounty instance.
		 * 
		 * @param target Who is the target.
		 */
		
		public BountyTarget(final UUID target) {
			this.target = target;
		}
		
		/**
		 * Returns the target.
		 * 
		 * @return The target.
		 */
		
		public final UUID getTarget() {
			return target;
		}
		
		/**
		 * Returns the bounty placed on the target's head by the specified UUID.
		 * 
		 * @param uuid The UUID.
		 * 
		 * @return The bounty placed on the target's head by the specified UUID.
		 */
		
		public final Double getBounty(final UUID uuid) {
			return bounties.get(uuid);
		}
		
		/**
		 * Sets the bounty placed by the specified UUID.
		 * 
		 * @param uuid The UUID.
		 * @param amount The bounty's amount.
		 */
		
		public final void setBounty(final UUID uuid, final double amount) {
			bounties.put(uuid, amount);
			refreshPlayerListName();
		}
		
		/**
		 * Removes a bounty placed on the target's head.
		 * 
		 * @param uuid The UUID that placed a bounty.
		 */
		
		public final void removeBounty(final UUID uuid) {
			bounties.remove(uuid);
			if(getTotalBounty() == 0d) {
				clearBounties();
				return;
			}
			refreshPlayerListName();
		}
		
		/**
		 * Clear all bounties for the target (and remove this instance from the main list).
		 */
		
		public final void clearBounties() {
			bounties.clear();
			bounties.remove(target);
			refreshPlayerListName();
		}
		
		/**
		 * Returns the total amount of money placed on the target's head.
		 * 
		 * @return The total amount of money placed on the target's head.
		 */

		public final double getTotalBounty() {
			double totalBounty = 0d;
			for(final double bounty : bounties.values()) {
				totalBounty += bounty;
			}
			return totalBounty;
		}
		
		/**
		 * Allows you to refresh the name of the current player in the TAB menu (if enabled by user).
		 */
		
		public final void refreshPlayerListName() {
			if(!config.playerListEnable) {
				return;
			}
			final OfflinePlayer player = Bukkit.getOfflinePlayer(target);
			if(player.isOnline()) {
				final double bounty = getTotalBounty();
				player.getPlayer().setPlayerListName(PlaceholderFormatter.defaultFormat(config.playerListDisplay, player, bounty));
			}
		}
		
		@Override
		public final String toString() {
			if(bounties.size() == 0) {
				return null;
			}
			final JSONObject json = new JSONObject();
			json.put("target", target.toString());
			final JSONArray array = new JSONArray();
			for(final Entry<UUID, Double> entry : bounties.entrySet()) {
				final JSONObject bounty = new JSONObject();
				bounty.put("uuid", entry.getKey().toString());
				bounty.put("amount", entry.getValue());
				array.add(bounty);
			}
			json.put("bounties", array);
			return json.toJSONString();
		}
		
	}

}