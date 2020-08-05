package net.VytskaLT.ScoreboardManager.team;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import javax.security.auth.Destroyable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Team implements Destroyable {

    @Getter
    private final TeamManager manager;
    @Getter
    private final String name;
    @Getter
    private final TeamInfo globalInfo = new TeamInfo(this);
    final Set<TeamInfo> infoSet = new HashSet<>(Collections.singletonList(globalInfo));
    private boolean destroyed;

    /**
     * Returns the team info of a player.
     *
     * @param player player to get team info of.
     * @return team info of player.
     */
    public TeamInfo getTeamInfo(Player player) {
        checkDestroyed();
        checkPlayer(player);

        for (TeamInfo teamInfo : infoSet) {
            if (teamInfo.players.contains(player))
                return teamInfo;
        }
        globalInfo.players.add(player);
        return null;
    }

    /**
     * Sets a player's team info.
     *
     * @param player player to set team info of.
     * @param teamInfo new team info of player.
     * @return new team info of player.
     */
    public TeamInfo setTeamInfo(Player player, TeamInfo teamInfo) {
        checkDestroyed();
        checkPlayer(player);

        teamInfo = teamInfo == null ? globalInfo : teamInfo;

        TeamInfo oldInfo = getTeamInfo(player);
        if (oldInfo != null) {
            if (oldInfo == teamInfo)
                return teamInfo;
            oldInfo.players.remove(player);
        }

        if (teamInfo.team != this)
            teamInfo.unassign();
        teamInfo.team = this;

        teamInfo.players.add(player);
        Set<Player> playerSet = Collections.singleton(player);
        if (oldInfo != null) {
            TeamPacketUtil.updateTeam(playerSet, teamInfo);
            if (!oldInfo.entries.isEmpty()) {
                Set<String> entries = new HashSet<>(oldInfo.entries);
                entries.removeAll(teamInfo.entries);
                TeamPacketUtil.removeEntries(playerSet, teamInfo, entries);

                entries = new HashSet<>(teamInfo.entries);
                entries.removeAll(oldInfo.entries);
                TeamPacketUtil.addEntries(playerSet, teamInfo, entries);
            } else if (!teamInfo.entries.isEmpty())
                TeamPacketUtil.addEntries(playerSet, teamInfo, teamInfo.entries);
        } else TeamPacketUtil.createTeam(playerSet, teamInfo);

        infoSet.add(teamInfo);
        return teamInfo;
    }

    /**
     * Destroys this team.
     */
    @Override
    public void destroy() {
        if (!destroyed) {
            infoSet.forEach(info -> TeamPacketUtil.removeTeam(info.players, info));
            manager.teams.remove(this);
            destroyed = true;
        }
    }

    private void checkDestroyed() {
        Preconditions.checkState(!destroyed, "Team is destroyed");
    }

    private void checkPlayer(Player player) {
        Preconditions.checkNotNull(player, "Player cannot be null");
        Preconditions.checkState(manager.getPlayers().contains(player), "Player is not in team manager");
    }
}
