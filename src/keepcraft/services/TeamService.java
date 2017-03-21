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
//		if (scoreboard.getObjective("team_below_Name") == null) {
//			// Register team below name objective
//			//Objective objective = scoreboard.registerNewObjective("team_below_name", "dummy");
//			//objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
//		}
	}

	public void addPlayerToTeam(UserTeam userTeam, Player player) {
		Team team = getTeam(userTeam);
		team.addPlayer(player);
		//player.setScoreboard(scoreboard);
	}

	private Team getTeam(UserTeam userTeam) {
		Team team = scoreboard.getTeam(userTeam.getName());
		if (team == null) {
			team = scoreboard.registerNewTeam(userTeam.getName());
			team.setDisplayName(userTeam.getName());
			team.setPrefix(userTeam.getChatColor().toString() + "[" + userTeam.getName().substring(0, 1) + "]" + ChatColor.RESET);
			team.setAllowFriendlyFire(false);
			team.setCanSeeFriendlyInvisibles(true);
		}
		return team;
	}
}
