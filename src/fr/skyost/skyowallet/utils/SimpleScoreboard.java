package fr.skyost.skyowallet.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * <h1>SimpleScoreboard</h1>
 * <br>
 * 
 * @author RainoBoy97.
 */

public class SimpleScoreboard {

	private final Scoreboard scoreboard;
	private String title;
	private final HashMap<String, Integer> scores;
	private final List<Team> teams;
	
	/**
	 * Create a new SimpleScoreboard instance.
	 * 
	 * @param title The scoreboard's title.
	 */

	public SimpleScoreboard(final String title) {
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		this.title = title;
		this.scores = Maps.newLinkedHashMap();
		this.teams = Lists.newArrayList();
	}
	
	/**
	 * Add a blank line.
	 */

	public void blankLine() {
		add(" ");
	}
	
	/**
	 * Add line of text.
	 * 
	 * @param text The text.
	 */

	public final void add(final String text) {
		add(text, null);
	}
	
	/**
	 * Add a line of text with a custom score.
	 * 
	 * @param text The text.
	 * @param score The score.
	 */

	public final void add(final String text, final Integer score) {
		Preconditions.checkArgument(text.length() < 48, "text cannot be over 48 characters in length");
		scores.put(fixDuplicates(text), score);
	}
	
	/**
	 * Fix duplicates texts in the scoreboard.
	 * 
	 * @param text The text.
	 * 
	 * @return The fixed text.
	 */

	private final String fixDuplicates(String text) {
		while(scores.containsKey(text)) {
			text += "�r";
		}
		return text.length() > 48 ? text.substring(0, 47) : text;
	}
	
	/**
	 * Creates a team.
	 * 
	 * @param text The text.
	 * 
	 * @return The team.
	 */

	private final Entry<Team, String> createTeam(final String text) {
		String result = "";
		if(text.length() <= 16) {
			return new SimpleEntry<>(null, text);
		}
		final Team team = scoreboard.registerNewTeam("text-" + scoreboard.getTeams().size());
		final Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
		team.setPrefix(iterator.next());
		result = iterator.next();
		if(text.length() > 32) {
			team.setSuffix(iterator.next());
		}
		teams.add(team);
		return new SimpleEntry<>(team, result);
	}
	
	/**
	 * Build the scoreboard.
	 */

	public final void build() {
		final Objective obj = scoreboard.registerNewObjective((title.length() > 16 ? title.substring(0, 15) : title), "dummy");
		obj.setDisplayName(title);
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		int index = scores.size();
		for(final Entry<String, Integer> text : scores.entrySet()) {
			final Entry<Team, String> team = createTeam(text.getKey());
			final OfflinePlayer player = Bukkit.getOfflinePlayer(team.getValue());
			if(team.getKey() != null) {
				team.getKey().addPlayer(player);
			}
			obj.getScore(player).setScore(text.getValue() != null ? text.getValue() : index);
			index -= 1;
		}
	}
	
	/**
	 * Reset the scoreboard.
	 */

	public final void reset() {
		title = null;
		scores.clear();
		for(Team t : teams)
			t.unregister();
		teams.clear();
	}
	
	/**
	 * Get the built scoreboard.
	 * 
	 * @return The scoreboard.
	 */

	public final Scoreboard getScoreboard() {
		return scoreboard;
	}
	
	/**
	 * Send the current scoreboard to some players.
	 * 
	 * @param players The players.
	 */

	public final void send(final Player... players) {
		for(Player p : players) {
			p.setScoreboard(scoreboard);
		}
	}

}