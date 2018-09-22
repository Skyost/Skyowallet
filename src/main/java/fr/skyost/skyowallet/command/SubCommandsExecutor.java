package fr.skyost.skyowallet.command;

import com.google.common.base.Joiner;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.util.PlaceholderFormatter;

/**
 * A command executor that allows to execute sub-commands.
 */

public abstract class SubCommandsExecutor implements CommandExecutor {

	/**
	 * All sub-commands.
	 */
	
	private final List<CommandInterface> commands = new ArrayList<>();

	/**
	 * The Skyowallet instance.
	 */

	private Skyowallet skyowallet;

	/**
	 * The command name.
	 */

	private String commandName;

	/**
	 * Creates a new sub-command executor instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param commandName The command name.
	 */

	public SubCommandsExecutor(final Skyowallet skyowallet, final String commandName) {
		this.skyowallet = skyowallet;
		this.commandName = commandName;
	}

	/**
	 * Returns the Skyowallet instance.
	 *
	 * @return The Skyowallet instance.
	 */

	public final Skyowallet getSkyowallet() {
		return skyowallet;
	}

	/**
	 * Sets the Skyowallet instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public final void setSkyowallet(final Skyowallet skyowallet) {
		this.skyowallet = skyowallet;
	}

	/**
	 * Returns the command name.
	 *
	 * @return The command name.
	 */

	public final String getCommandName() {
		return commandName;
	}

	/**
	 * Sets the command name.
	 *
	 * @param commandName The command name.
	 */

	public final void setCommandName(final String commandName) {
		this.commandName = commandName;
	}

	/**
	 * Registers a sub-command.
	 * 
	 * @param command The sub-command.
	 */
	
	public final void registerSubCommand(final CommandInterface command) {
		if(!commands.contains(command)) {
			commands.add(command);
		}
	}
	
	/**
	 * Returns the executor of a sub-command.
	 * 
	 * @param command The sub-command's label.
	 * 
	 * @return The executor.
	 */
	
	public final CommandInterface getExecutor(final String command) {
		for(final CommandInterface commandInterface : commands) {
			if(Arrays.asList(commandInterface.getNames()).contains(command)) {
				return commandInterface;
			}
		}
		return null;
	}
	
	/**
	 * Returns an array which contains a list of sub-command.
	 * 
	 * @return The array.
	 */
	
	public final CommandInterface[] getCommands() {
		return commands.toArray(new CommandInterface[0]);
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		try {
			final CommandInterface commandInterface = getExecutor(args[0]);
			if(commandInterface == null) {
				sendUsage(sender);
				return true;
			}
			if(commandInterface.mustBePlayer() && !(sender instanceof Player)) {
				sender.sendMessage(skyowallet.getPluginMessages().messageConsoleDisallowed);
				return true;
			}
			final String permission = commandInterface.getPermission();
			if(permission != null && !sender.hasPermission(permission)) {
				sender.sendMessage(skyowallet.getPluginMessages().messageNoPermission);
				return true;
			}
			if(args.length - 1 < commandInterface.getMinArgsLength()) {
				sender.sendMessage(ChatColor.RED + "/" + label + " " + args[0] + " " + commandInterface.getUsage());
				return true;
			}
			if(commandInterface.onCommand(this, sender, Arrays.copyOfRange(args, 1, args.length))) {
				return true;
			}
		}
		catch(final Exception ex) {
			ex.printStackTrace();
			sender.sendMessage(ChatColor.RED + ex.getClass().getName());
			return true;
		}
		sendUsage(sender);
		return true;
	}

	/**
	 * Sends the command usage to the specified command sender.
	 *
	 * @param sender The command sender.
	 */

	public final void sendUsage(final CommandSender sender) {
		sender.sendMessage(ChatColor.RED + PlaceholderFormatter.format(skyowallet.getPluginMessages().messageCommandUsage, new PlaceholderFormatter.LineBreakPlaceholder(), new PlaceholderFormatter.Placeholder("usage", getUsage(sender))).replace(" or ", ChatColor.ITALIC + "\n or " + ChatColor.RESET + ChatColor.RED));
	}
	
	/**
	 * Returns the usage of the command.
	 *
	 * @return The usage.
	 */
	
	public final String getUsage() {
		return getUsage(null);
	}

	/**
	 * Returns the usage of the command.
	 *
	 * @param sender The command sender.
	 *
	 * @return The usage.
	 */

	public final String getUsage(final CommandSender sender) {
		final List<String> subCommands = new ArrayList<>();
		for(final CommandInterface command : commands) {
			if(sender != null && !sender.hasPermission(command.getPermission())) {
				continue;
			}
			final String commandUsage = command.getUsage();
			subCommands.add("/" + commandName + " " + command.getNames()[0] + (commandUsage == null ? "" : " " + command.getUsage()));
		}
		return Joiner.on(" or ").join(subCommands) + ".";
	}
	
	public interface CommandInterface {
		
		/**
		 * Returns the names of the sub-command.
		 * 
		 * @return The names.
		 */
		
		String[] getNames();
		
		/**
		 * If the sender must be a Player.
		 * 
		 * @return <b>true</b> If the sender must be a Player.
		 * <br><b>false</b> If the sender can be the Console, a CommandBlock, ...
		 */

		boolean mustBePlayer();
		
		/**
		 * Returns the permission of the sub-command. Can be <b>null</b>.
		 * 
		 * @return The permission.
		 */
		
		String getPermission();
		
		/**
		 * Returns the minimum arguments length of the sub-command.
		 * 
		 * @return The minimum arguments length;
		 */
		
		int getMinArgsLength();
		
		/**
		 * Returns the usage of the sub-command.
		 * 
		 * @return The usage.
		 */
		
		String getUsage();
		
		/**
		 * Wrapper for <b>onCommand(...)</b> of <b>SubCommandsExecutor</b>.
		 *
		 * @param command The parent command.
		 * @param sender The command's sender.
		 * @param args Arguments specified.
		 * 
		 * @return <b>true</b> If the command is valid.
		 * <br><b>false</b> If the command is not valid.
		 */
		
		boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String... args);

	}

}