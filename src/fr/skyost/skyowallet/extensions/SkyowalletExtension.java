package fr.skyost.skyowallet.extensions;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.utils.Skyoconfig;

public abstract class SkyowalletExtension implements Listener {
	
	private final Plugin plugin;
	
	protected SkyowalletExtension(final Plugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Loads the extension (register its events and load the configuration).
	 * 
	 * @throws InvalidConfigurationException If there is a problem in the extension's configuration.
	 */
	
	public final void load() throws InvalidConfigurationException {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		getConfiguration().load();
	}
	
	/**
	 * Gets the extension's name.
	 * 
	 * @return The extension's name.
	 */
	
	public abstract String getName();
	
	/**
	 * Gets the extension's permissions.
	 * <br><b>Key :</b> Permission's name.
	 * <br><b>Value :</b> Default value.
	 * 
	 * @return The extension's permissions.
	 */
	
	public abstract HashMap<String, PermissionDefault> getPermissions();
	
	/**
	 * Gets the extension's YAML configuration.
	 * 
	 * @return The YAML configuration.
	 */
	
	public abstract Skyoconfig getConfiguration();
	
	/**
	 * Gets the extension's configuration file.
	 * 
	 * @return The configuration file.
	 */
	
	public final File getConfigurationFile() {
		return new File(SkyowalletAPI.getExtensionsDirectory(), getName().toLowerCase().replace(" ", "") + ".yml");
	}
	
	/**
	 * Checks if this extension is enabled (only checks via the configuration).
	 */
	
	public abstract boolean isEnabled();
	
	/**
	 * Extensions are enabled by default. Use this method to disable them.
	 * <br><b>NOTE :</b> If the user has enabled the extension and you call this method, <i>isEnabled()</i> will always return <b>true</b>.
	 */
	
	public final void disable() {
		HandlerList.unregisterAll(this);
	}
	
}
