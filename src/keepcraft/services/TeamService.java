package keepcraft.services;

import keepcraft.data.models.User;
import keepcraft.data.models.UserTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TeamService {

	public void addPlayerToTeam(UserTeam userTeam, Player player) {
		addPlayerToTeam(userTeam, player, false);
	}

	public void addStealth(User user, Player player) {
		addPlayerToTeam(user.getTeam(), player, true);
	}

	public void removeStealth(User user, Player player) {
		addPlayerToTeam(user.getTeam(), player, false);
	}

	private void addPlayerToTeam(UserTeam userTeam, Player player, boolean stealth) {
		if (player == null) return;

		// TODO teams might persist through server reboots, meaning it would only be necessary to do this when the user is first created

		// Remove from current teams, if any
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		for (Team otherTeam : scoreboard.getTeams()) {
			if (otherTeam.hasPlayer(player)) {
				otherTeam.removePlayer(player);
			}
		}

		// Add to new team
		getTeam(userTeam, stealth).addPlayer(player);

		if (player.isOp()) {
			player.setPlayerListName(ChatService.NameAdmin + player.getDisplayName());
		} else {
			player.setPlayerListName(userTeam.getChatColor().toString() + player.getDisplayName());
		}
	}

	private Team getTeam(UserTeam userTeam, boolean stealth) {
		String teamName = String.format("%s%s", userTeam.getName(), stealth ? "_stealth" : "");

		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = scoreboard.getTeam(teamName);
		if (team == null) {
			team = scoreboard.registerNewTeam(teamName);
			team.setDisplayName(userTeam.getName());
			team.setPrefix(userTeam.getChatColor().toString() + "<" + ChatColor.RESET);
			team.setSuffix(userTeam.getChatColor().toString() + ">" + ChatColor.RESET);
			team.setAllowFriendlyFire(false);
			team.setCanSeeFriendlyInvisibles(true);
			if (stealth) {
				team.setNameTagVisibility(NameTagVisibility.NEVER);
			}
		}
		return team;
	}
}
