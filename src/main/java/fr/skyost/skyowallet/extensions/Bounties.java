package fr.skyost.skyowallet.extensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

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

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.commands.SubCommandsExecutor;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.Utils;

public class Bounties extends SkyowalletExtension {
	
	private ExtensionConfig config;
	private HashMap<UUID, Bounty> bounties = new HashMap<UUID, Bounty>();
	
	public Bounties(final JavaPlugin plugin) {
		super(plugin);
	}
	
	@Override
	public void load() throws InvalidConfigurationException {
		super.load();
		try {
			for(final String bountyData : config.bountiesData) {
				final Bounty bounty = bountyFromJson(bountyData);
				bounties.put(bounty.getTarget(), bounty);
			}
			config.bountiesData.clear();
		}
		catch(final Exception ex) {
			throw new InvalidConfigurationException(ex);
		}
	}
	
	@Override
	public final String getName() {
		return "Bounties";
	}

	@Override
	public final Map<String, PermissionDefault> getPermissions() {
		final Map<String, PermissionDefault> permissions = new HashMap<String, PermissionDefault>();
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
		final BountyCommand bountyCmd = new BountyCommand();
		for(final CommandInterface command : new CommandInterface[]{new BountyAdd(), new BountyRemove(), new BountyDelete(), new BountyGet(), new BountyList()}) {
			bountyCmd.registerSubCommand(command);
		}
		final Map<String, CommandExecutor> commands = new HashMap<String, CommandExecutor>();
		commands.put("bounty", bountyCmd);
		return commands;
	}
	
	@Override
	public final SkyowalletExtensionConfig getConfiguration() {
		return config == null ? config = new ExtensionConfig() : config;
	}
	
	@Override
	public final void unload() throws InvalidConfigurationException {
		for(final Bounty bounty : bounties.values()) {
			final String json = bounty.toString();
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
		final Bounty bounty = getBounty(player.getUniqueId());
		final double amount = bounty.getTotalBounty();
		if(amount != 0d) {
			final SkyowalletAccount account = SkyowalletAPI.getAccount(player);
			account.setWallet(account.getWallet() + amount);
			if(config.bountyNotify) {
				killer.sendMessage(config.message17.replace("/player/", player.getName()).replace("/amount/", String.valueOf(amount)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(amount)));
			}
		}
		bounty.clearBounties();
	}
	
	/**
	 * Gets the bounty for the specified UUID.
	 * 
	 * @param target The UUID.
	 * 
	 * @return The bounty corresponding to the specified UUID or a blank bounty if the specified target UUID does not exist.
	 */
	
	public final Bounty getBounty(final UUID target) {
		return bounties.containsKey(target) ? bounties.get(target) : new Bounty(target);
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
	
	private final Bounty bountyFromJson(final String json) throws ParseException {
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
			throw new NullPointerException("Bounties data is null.");
		}
		final Bounty currentBounty = new Bounty((UUID)target);
		for(final Object bounty : (JSONArray)jsonBounties) {
			final JSONObject jsonBounty = (JSONObject)bounty;
			Object uuid = jsonBounty.get("uuid");
			if(uuid == null) {
				throw new NullPointerException("Bounty UUID is null.");
			}
			uuid = Utils.uuidTryParse(uuid.toString());
			if(uuid == null) {
				throw new IllegalArgumentException("This is not a true UUID !");
			}
			final Object amount = jsonBounty.get("amount");
			if(amount == null) {
				throw new NullPointerException("Bounty amount is null.");
			}
			currentBounty.setBounty((UUID)uuid, Double.parseDouble(amount.toString()));
		}
		return currentBounty;
	}
	
	public class BountyCommand extends SubCommandsExecutor {
		
		@Override
		public boolean onCommand(final CommandSender sender, final Command command, final String label, String[] args) {
			if(args.length == 0) {
				return false;
			}
			return super.onCommand(sender, command, label, args);
		}
		
	}
	
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
		public boolean onCommand(final CommandSender sender, final String[] args) {
			final OfflinePlayer player = Utils.getPlayerByArgument(args[0]);
			if(player == null) {
				sender.sendMessage(config.message1);
				return true;
			}
			final UUID uuid = player.getUniqueId();
			final Double amount = Utils.doubleTryParse(args[1]);
			if(amount == null) {
				sender.sendMessage(Skyowallet.messages.message13);
				return true;
			}
			if(amount < config.bountyMin || amount > config.bountyMax) {
				sender.sendMessage(config.message2.replace("/bounty-min/", String.valueOf(config.bountyMin)).replace("/bounty-max/", String.valueOf(config.bountyMax)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(config.bountyMax)));
				return true;
			}
			final SkyowalletAccount account = SkyowalletAPI.getAccount((OfflinePlayer)sender);
			if(account.getWallet() < amount) {
				sender.sendMessage(config.message3);
				return true;
			}
			account.setWallet(account.getWallet() - amount);
			final Bounty bounty = getBounty(uuid);
			if(!bounties.containsKey(uuid)) {
				bounties.put(uuid, bounty);
			}
			bounty.setBounty(account.getUUID(), amount);
			sender.sendMessage(config.message4.replace("/amount/", String.valueOf(amount)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(amount)).replace("/player/", player.getName()).replace("/bounty/", String.valueOf(bounty.getTotalBounty())));
			if(config.bountyNotify && player.isOnline()) {
				((Player)player).sendMessage(config.message5.replace("/player/", sender.getName()).replace("/amount/", String.valueOf(amount)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(amount)));
			}
			return true;
		}
		
	}
	
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
		public boolean onCommand(final CommandSender sender, final String[] args) {
			final OfflinePlayer player = Utils.getPlayerByArgument(args[0]);
			if(player == null) {
				sender.sendMessage(config.message1);
				return true;
			}
			final UUID uuid = ((OfflinePlayer)sender).getUniqueId();
			final Bounty bounty = getBounty(player.getUniqueId());
			if(!bounty.bounties.containsKey(uuid)) {
				sender.sendMessage(config.message6.replace("/player/", player.getName()));
				return true;
			}
			final SkyowalletAccount account = SkyowalletAPI.getAccount(uuid);
			account.setWallet(account.getWallet() + bounty.bounties.get(uuid));
			bounty.removeBounty(uuid);
			sender.sendMessage(config.message7.replace("/player/", player.getName()));
			if(config.bountyNotify && player.isOnline()) {
				((Player)player).sendMessage(config.message8.replace("/player/", sender.getName()));
			}
			return true;
		}
		
	}
	
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
		public boolean onCommand(final CommandSender sender, final String[] args) {
			final OfflinePlayer player = Utils.getPlayerByArgument(args[0]);
			if(player == null) {
				sender.sendMessage(config.message1);
				return true;
			}
			final Bounty bounty = getBounty(player.getUniqueId());
			if(bounty.getTotalBounty() == 0d) {
				sender.sendMessage(config.message9);
				return true;
			}
			if(config.bountyGiveBackIfDeleted) {
				for(final Entry<UUID, Double> entry : bounty.bounties.entrySet()) {
					final OfflinePlayer offline = Bukkit.getOfflinePlayer(entry.getKey());
					final SkyowalletAccount account = SkyowalletAPI.getAccount(offline);
					if(account == null) {
						continue;
					}
					final double amount = entry.getValue();
					account.setWallet(account.getWallet() + amount);
					if(offline.isOnline()) {
						((Player)offline).sendMessage(config.message12.replace("/player/", sender.getName()).replace("/target/", player.getName()).replace("/amount/", String.valueOf(amount)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(amount)));
					}
				}
			}
			bounty.clearBounties();
			sender.sendMessage(config.message10.replace("/player/", player.getName()));
			if(config.bountyNotify && player.isOnline()) {
				((Player)player).sendMessage(config.message11.replace("/player/", sender.getName()));
			}
			return true;
		}
		
	}
	
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
		public boolean onCommand(final CommandSender sender, final String[] args) {
			OfflinePlayer player;
			if(args.length >= 1) {
				if(!sender.hasPermission(getPermission() + ".player")) {
					sender.sendMessage(Skyowallet.messages.message1);
					return true;
				}
				player = Utils.getPlayerByArgument(args[0]);
				if(player == null) {
					sender.sendMessage(config.message1);
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
			sender.sendMessage(config.message13.replace("/player/", player.getName()).replace("/amount/", String.valueOf(amount)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(amount)));
			return true;
		}
		
	}
	
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
		public boolean onCommand(final CommandSender sender, final String[] args) {
			if(bounties.size() == 0) {
				sender.sendMessage(config.message14);
				return true;
			}
			final List<Bounty> bounties = new ArrayList<Bounty>(Bounties.this.bounties.values());
			Collections.sort(bounties, new Comparator<Bounty>() {
				
			    @Override
			    public final int compare(final Bounty bounty1, final Bounty bounty2) {
			        return Double.compare(bounty1.getTotalBounty(), bounty2.getTotalBounty());
			    }
			    
			});
			for(final Bounty bounty : bounties) {
				final double amount = bounty.getTotalBounty();
				final OfflinePlayer player = Bukkit.getOfflinePlayer(bounty.getTarget());
				sender.sendMessage(config.message15.replace("/player/", player == null ? bounty.getTarget().toString() : player.getName()).replace("/amount/", String.valueOf(amount)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(amount)));
			}
			sender.sendMessage(ChatColor.GRAY + "-----------------------------------------------------");
			sender.sendMessage(config.message16.replace("/bounties/", String.valueOf(bounties.size())));
			return true;
		}
		
	}
	
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
		
		@ConfigOptions(name = "messages.1")
		public String message1 = ChatColor.RED + "This player does not exist.";
		@ConfigOptions(name = "messages.2")
		public String message2 = ChatColor.RED + "The bounty must be between /bounty-min/ and /bounty-max/ /currency-name/.";
		@ConfigOptions(name = "messages.3")
		public String message3 = ChatColor.RED + "You do not have enough money in your wallet !";
		@ConfigOptions(name = "messages.4")
		public String message4 = ChatColor.GOLD + "You just put /amount/ /currency-name/ on the head of /player/. The total bounty on his head is /bounty/ /currency-name/.";
		@ConfigOptions(name = "messages.5")
		public String message5 = ChatColor.DARK_RED + "/player/ just put /amount/ /currency-name/ on your head !";
		@ConfigOptions(name = "messages.6")
		public String message6 = ChatColor.RED + "You did not put any bounty on the head of /player/ !";
		@ConfigOptions(name = "messages.7")
		public String message7 = ChatColor.GREEN + "You just removed the bounty you put on the head of /player/ !";
		@ConfigOptions(name = "messages.8")
		public String message8 = ChatColor.DARK_GREEN + "/player/ just removed the bounty he puts on your head.";
		@ConfigOptions(name = "messages.9")
		public String message9 = ChatColor.RED + "/player/ did not have any bounty on his head.";
		@ConfigOptions(name = "messages.10")
		public String message10 = ChatColor.GREEN + "You just removed the bounties that target /player/.";
		@ConfigOptions(name = "messages.11")
		public String message11 = ChatColor.DARK_GREEN + "/player/ just removed all bounties on your head.";
		@ConfigOptions(name = "messages.12")
		public String message12 = ChatColor.GOLD + "/player/ just removed all bounties on the head of /target/. Therefore, /amount/ /currency-name/ are added back to your wallet.";
		@ConfigOptions(name = "messages.13")
		public String message13 = "The reward to kill " + ChatColor.GOLD + "/player/" + ChatColor.RESET + " is " + ChatColor.GOLD + "/amount/ /currency-name/" + ChatColor.RESET + ".";
		@ConfigOptions(name = "messages.14")
		public String message14 = ChatColor.RED + "There is currently no bounty on this server.";
		@ConfigOptions(name = "messages.15")
		public String message15 = ChatColor.GOLD + "¤ /player/" + ChatColor.RESET + " wanted for " + ChatColor.GOLD + "/amount/ /currency-name/" + ChatColor.RESET + ".";
		@ConfigOptions(name = "messages.16")
		public String message16 = "There are currently " + ChatColor.GOLD + "/bounties/" + ChatColor.RESET +  " available bounties on this server.";
		@ConfigOptions(name = "messages.17")
		public String message17 = ChatColor.GOLD + "You just earned /amount/ /currency-name/ for killing /player/.";
		
		@ConfigOptions(name = "bounties-data")
		public List<String> bountiesData = new ArrayList<String>();
		
	}
	
	/**
	 * Inner class that represents a bounty.
	 */
	
	public class Bounty {
		
		private final UUID target;
		private final Map<UUID, Double> bounties = new HashMap<UUID, Double>();
		
		/**
		 * Creates a new bounty instance.
		 * 
		 * @param target Who is the target.
		 */
		
		public Bounty(final UUID target) {
			this.target = target;
		}
		
		/**
		 * Gets the target.
		 * 
		 * @return The target.
		 */
		
		public final UUID getTarget() {
			return target;
		}
		
		/**
		 * Gets the bounty placed on the target's head by the specified UUID.
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
			Bounties.this.bounties.remove(target);
			refreshPlayerListName();
		}
		
		/**
		 * Gets the total amount of money placed on the target's head.
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
				((Player)player).setPlayerListName(config.playerListDisplay.replace("/player/", player.getName()).replace("/amount/", String.valueOf(bounty)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(bounty)));
			}
		}
		
		@SuppressWarnings("unchecked")
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