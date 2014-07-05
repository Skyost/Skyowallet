package fr.skyost.skyowallet.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class SubCommandsExecutor implements CommandExecutor {
	
private final List<CommandInterface> commands = new ArrayList<CommandInterface>();

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
	 * Gets the executor of a sub-command.
	 * 
	 * @param command The sub-command's label.
	 * 
	 * @return The executor.
	 */
	
	public final CommandInterface getExecutor(final String command) {
		for(final CommandInterface commandInterface : commands) {
			if(Arrays.asList(commandInterface.names()).contains(command)) {
				return commandInterface;
			}
		}
		return null;
	}
	
	/**
	 * Gets an array which contains a list of sub-commands.
	 * 
	 * @return The array.
	 */
	
	public final CommandInterface[] getCommands() {
		return commands.toArray(new CommandInterface[commands.size()]);
	}

	@Override
	public abstract boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args);
	
	public interface CommandInterface {
		
		/**
		 * Gets the names of the sub-command.
		 * 
		 * @return The names.
		 */
		
		public String[] names();
		
		/**
		 * If the sender must be a Player.
		 * 
		 * @return <b>true</b> If the sender must be a Player.
		 * <br><b>false</b> If the sender can be the Console, a CommandBlock, ...
		 */
		
		public boolean forcePlayer();
		
		/**
		 * Gets the permission of the sub-command. Can be <b>null</b>.
		 * 
		 * @return The permission.
		 */
		
		public String getPermission();
		
		/**
		 * Gets the minimum arguments length of the sub-command.
		 * 
		 * @return The minimum arguments length;
		 */
		
		public int getMinArgsLength();
		
		/**
		 * Gets the usage of the sub-command.
		 * 
		 * @return The usage.
		 */
		
		public String getUsage();
		
		/**
		 * Wrapper for <b>onCommand(...)</b> of <b>SubCommandsExecutor</b>.
		 * 
		 * @param sender The command's sender.
		 * @param args Arguments specified.
		 * 
		 * @return <b>true</b> If the command is valid.
		 * </b>false</b> If the command is not valid.
		 * 
		 * @throws Exception If something wrong occurs.
		 */
		
		public boolean onCommand(final CommandSender sender, final String[] args) throws Exception;

	}

}
