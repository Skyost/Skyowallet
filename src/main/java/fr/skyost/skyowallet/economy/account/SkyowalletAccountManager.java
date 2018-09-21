package fr.skyost.skyowallet.economy.account;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.SkyowalletManager;

/**
 * This class allows to manage accounts.
 */

public class SkyowalletAccountManager extends SkyowalletManager<SkyowalletAccount> {

	/**
	 * The Skyowallet instance.
	 */

	private Skyowallet skyowallet;

	/**
	 * Creates a new Skyowallet account manager instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param accounts Accounts to register.
	 */

	public SkyowalletAccountManager(final Skyowallet skyowallet, final SkyowalletAccount... accounts) {
		super(accounts);

		this.skyowallet = skyowallet;
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
	 * Creates and add an account.
	 *
	 * @param player The player.
	 *
	 * @return The account.
	 */

	public SkyowalletAccount add(final OfflinePlayer player) {
		return add(player == null ? null : player.getUniqueId());
	}

	/**
	 * Creates and add an account.
	 *
	 * @param uuid The UUID.
	 *
	 * @return The account.
	 */

	public SkyowalletAccount add(final UUID uuid) {
		if(uuid == null) {
			return null;
		}
		return super.add(new SkyowalletAccount(skyowallet, uuid));
	}

	/**
	 * Returns whether this manager has the specified player.
	 *
	 * @param player The player.
	 *
	 * @return Whether this manager has the specified player.
	 */

	public boolean has(final OfflinePlayer player) {
		return has(player == null ? null : player.getUniqueId());
	}

	/**
	 * Returns whether this manager has the specified UUID.
	 *
	 * @param uuid The UUID.
	 *
	 * @return Whether this manager has the specified UUID.
	 */

	public boolean has(final UUID uuid) {
		return has(uuid == null ? null : uuid.toString());
	}

	/**
	 * Returns the account that correspond to the specified player.
	 *
	 * @param player The player.
	 *
	 * @return The corresponding account.
	 */

	public SkyowalletAccount get(final OfflinePlayer player) {
		return get(player == null ? null : player.getUniqueId());
	}

	/**
	 * Returns the account that correspond to the specified UUID.
	 *
	 * @param uuid The UUID.
	 *
	 * @return The corresponding account.
	 */

	public SkyowalletAccount get(final UUID uuid) {
		return get(uuid == null ? null : uuid.toString());
	}

	/**
	 * Removes a player's account from this manager.
	 *
	 * @param player The player.
	 *
	 * @return An array :
	 * <br><em>0 :</em> Its wallet.
	 * <br><em>1 :</em> Its bank balance.
	 */

	public double[] remove(final OfflinePlayer player) {
		return remove(player == null ? null : player.getUniqueId());
	}

	/**
	 * Removes a UUID's account from this manager.
	 *
	 * @param uuid The UUID.
	 *
	 * @return An array :
	 * <br><em>0 :</em> Its wallet.
	 * <br><em>1 :</em> Its bank balance.
	 */

	public double[] remove(final UUID uuid) {
		return remove(uuid == null ? null : uuid.toString());
	}

	@Override
	public double[] remove(final SkyowalletAccount account) {
		return remove(account == null ? null : account.getIdentifier());
	}

	@Override
	public double[] remove(final String identifier) {
		final SkyowalletAccount result = (SkyowalletAccount)super.remove(identifier);
		if(result == null) {
			return new double[]{-1d, -1d};
		}

		return new double[]{result.getWallet().getAmount(), result.getBankBalance().getAmount()};
	}

}