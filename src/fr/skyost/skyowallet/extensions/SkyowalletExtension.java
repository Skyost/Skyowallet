package fr.skyost.skyowallet.extensions;

import java.util.HashMap;

import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionDefault;

public abstract class SkyowalletExtension implements Listener {
	
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

}
