package fr.skyost.skyowallet.extension;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;

/**
 * The class that allows to manage extensions.
 */

public class ExtensionManager {

	/**
	 * The Skyowallet instance.
	 */

	private Skyowallet skyowallet;

	/**
	 * The registered extensions.
	 */

	private final HashSet<SkyowalletExtension> extensions = new HashSet<>();

	/**
	 * Creates a new extension manager instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param extensions The extensions to register.
	 */

	public ExtensionManager(final Skyowallet skyowallet, final SkyowalletExtension... extensions) {
		this.skyowallet = skyowallet;
		for(final SkyowalletExtension extension : extensions) {
			register(extension);
		}
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
	 * Loads and registers an extension.
	 *
	 * @param extension The Skyowallet extension.
	 */

	public void register(final SkyowalletExtension extension) {
		register(extension, true);
	}

	/**
	 * Loads and registers an extension.
	 *
	 * @param extension The Skyowallet extension.
	 * @param log If there should be a log in the console.
	 */

	public void register(final SkyowalletExtension extension, final boolean log) {
		final Logger logger = skyowallet.getLogger();
		final String name = extension.getName();
		try {
			extension.load();
			if(!extension.isEnabled()) {
				extension.unload();
				return;
			}
			if(log) {
				logger.log(Level.INFO, "Loading " + name + "...");
			}
			final PluginManager manager = Bukkit.getPluginManager();
			for(final Map.Entry<String, PermissionDefault> entry : extension.getPermissions().entrySet()) {
				manager.addPermission(new Permission(entry.getKey(), entry.getValue()));
			}
			for(final Map.Entry<String, CommandExecutor> entry : extension.getCommands().entrySet()) {
				final CommandExecutor executor = entry.getValue();
				final PluginCommand command = skyowallet.getCommand(entry.getKey());
				command.setUsage(ChatColor.RED + "/" + command.getName() + " " + (executor instanceof SubCommandsExecutor ? ((SubCommandsExecutor)executor).getUsage() : command.getUsage()));
				command.setExecutor(executor);
			}
			extensions.add(extension);
			if(log) {
				logger.log(Level.INFO, name + " loaded and registered !");
			}
		}
		catch(final Exception ex) {
			if(log) {
				logger.log(Level.SEVERE, "An error occurred while verifying / enabling the extension \"" + name + "\" : " + ex.getClass().getName() + ".");
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Unloads and unregisters an extension (just a convenience method).
	 *
	 * @param extension The Skyowallet extension.
	 *
	 * @throws InvalidConfigurationException If the config cannot be saved.
	 */

	public void unregister(final SkyowalletExtension extension) throws InvalidConfigurationException {
		unregister(extension, true);
	}

	/**
	 * Unloads and unregisters an extension (just a convenience method).
	 *
	 * @param extension The Skyowallet extension.
	 * @param log If there should be a log in the console.
	 *
	 * @throws InvalidConfigurationException If the config cannot be saved.
	 */

	public void unregister(final SkyowalletExtension extension, final boolean log) throws InvalidConfigurationException {
		final Logger logger = extension.getPlugin().getLogger();
		final String name = extension.getName();
		if(log) {
			logger.log(Level.INFO, "Disabling " + name + "...");
		}
		extension.unload();
		extensions.remove(extension);
		if(log) {
			logger.log(Level.INFO, name + " disabled !");
		}
	}

	/**
	 * Returns whether the extension has been loaded.
	 *
	 * @param extension The extension.
	 *
	 * @return Whether the extension has been loaded.
	 */

	public boolean isRegistered(final SkyowalletExtension extension) {
		return extensions.contains(extension);
	}

	/**
	 * Returns all loaded extensions.
	 *
	 * @return All loaded extensions.
	 */

	public Set<SkyowalletExtension> getLoadedExtensions() {
		return extensions;
	}

}