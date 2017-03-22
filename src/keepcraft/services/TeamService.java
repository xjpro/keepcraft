package keepcraft.services;

import keepcraft.data.models.UserTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TeamService {

	private final Scoreboard scoreboard;

	public TeamService() {
		scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
	}

	public void addPlayerToTeam(UserTeam userTeam, Player player) {

		// TODO teams might persist through server reboots, meaning it would only be necessary to do this when the user is first created

		// Remove from current teams, if any
		for (Team otherTeam : scoreboard.getTeams()) {
			if (otherTeam.hasPlayer(player)) {
				otherTeam.removePlayer(player);
			}
		}

		// Add to new team
		getTeam(userTeam).addPlayer(player);

		if (player.isOp()) {
			player.setPlayerListName(ChatService.NameAdmin + player.getDisplayName());
		} else {
			player.setPlayerListName(userTeam.getChatColor().toString() + player.getDisplayName());
		}
	}

	private Team getTeam(UserTeam userTeam) {
		Team team = scoreboard.getTeam(userTeam.getName());
		if (team == null) {
			team = scoreboard.registerNewTeam(userTeam.getName());
			team.setDisplayName(userTeam.getName());
			team.setPrefix(userTeam.getChatColor().toString() + "<" + ChatColor.RESET);
			team.setSuffix(userTeam.getChatColor().toString() + ">" + ChatColor.RESET);
			team.setAllowFriendlyFire(false);
			team.setCanSeeFriendlyInvisibles(true);
		}
		return team;
	}
}
