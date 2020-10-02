package net.VytskaLT.ScoreboardManager.team;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.VytskaLT.ScoreboardManager.ScoreboardManagerPlugin;
import org.bukkit.entity.Player;

import javax.security.auth.Destroyable;
import java.util.*;

public class TeamManager implements Destroyable {

    private final ScoreboardManagerPlugin plugin;
    private final Set<Player> players = new HashSet<>();
    final Set<Team> teams = new HashSet<>();
    @Getter
    private boolean destroyed;
    private final boolean checkManagers;

    public TeamManager() {
        this(null);
    }

    /**
     * @param players players to add to team manager.
     */
    public TeamManager(Iterable<Player> players) {
        this(players, true);
    }

    /**
     * Constructor that should only be used by <code>Sidebar</code>
     *
     * @param players players to add to team manager.
     * @param checkManagers whether to remove players that already are in a team manager.
     * @deprecated should only be used by <code>Sidebar</code>.
     */
    @Deprecated
    public TeamManager(Iterable<Player> players, boolean checkManagers) {
        this.checkManagers = checkManagers;
        if (players != null && checkManagers)
            for (Player player : players) {
                checkManagers(player);
                this.players.add(player);
            }

        plugin = ScoreboardManagerPlugin.getPlugin(ScoreboardManagerPlugin.class);
        Preconditions.checkNotNull(plugin, "Plugin not initialized. Did you put it in your plugins folder?");
        plugin.teamManagers.add(this);
    }

    /**
     * Returns an unmodifiable set of players in this team manager.
     *
     * @return unmodifiable set of players in this team manager.
     */
    public Set<Player> getPlayers() {
        checkDestroyed();
        return Collections.unmodifiableSet(players);
    }

    /**
     * Returns an unmodifiable set of teams in this team manager.
     *
     * @return unmodifiable set of teams in this team manager.
     */
    public Set<Team> getTeams() {
        checkDestroyed();
        return Collections.unmodifiableSet(teams);
    }

    /**
     * Returns team based on its name.
     *
     * @param name name of team.
     * @return team with this name.
     */
    public Team getTeam(String name) {
        checkDestroyed();
        Preconditions.checkNotNull(name, "Team name cannot be null");
        for (Team team : teams)
            if (team.getName().equals(name))
                return team;
        return null;
    }

    /**
     * Returns whether a team by the name exists.
     *
     * @param name name of team.
     * @return whether a team by the name exists.
     */
    public boolean teamExists(String name) {
        return getTeam(name) != null;
    }

    /**
     * Returns team based on its name. If it doesn't already exists, creates it.
     *
     * @param name name of team.
     * @return team with this name.
     */
    public Team getOrCreateTeam(String name) {
        return getOrCreateTeam(name, new HashMap<>());
    }

    /**
     * Returns team based on its name. If it doesn't already exists, creates it.
     *
     * @param name name of team.
     * @param teamInfo a map of team infos players will get.
     * @return team with this name.
     */
    public Team getOrCreateTeam(String name, Map<Player, TeamInfo> teamInfo) {
        checkDestroyed();
        Team team = getTeam(name);
        if (team != null)
            return team;
        team = new Team(this, name);
        teams.add(team);

        for (Player player : players)
            if (!teamInfo.containsKey(player))
                team.setTeamInfo(player, null);

        for (Map.Entry<Player, TeamInfo> entry : teamInfo.entrySet()) {
            Player player = entry.getKey();
            TeamInfo info = entry.getValue();
            Preconditions.checkNotNull(player);
            Preconditions.checkState(players.contains(player), "Player is not in team manager");
            team.setTeamInfo(player, info);
        }
        return team;
    }

    /**
     * Adds a player to this team manager.
     *
     * @param player player to add to team manager.
     * @return whether the player was added.
     */
    public boolean addPlayer(Player player) {
        return addPlayer(player, new HashMap<>());
    }

    /**
     * Adds a player to this team manager.
     *
     * @param player player to add to team manager.
     * @param teamInfo a map of team infos the player should get.
     * @return whether the player was added.
     */
    public boolean addPlayer(Player player, Map<Team, TeamInfo> teamInfo) {
        checkDestroyed();
        Preconditions.checkNotNull(player, "Player cannot be null");
        Preconditions.checkNotNull(teamInfo, "Team info map cannot be null, use addPlayer(player) instead");

        if (!players.contains(player)) {
            checkManagers(player);
            players.add(player);
            teams.forEach(team -> {
                Preconditions.checkState(teams.contains(team), "Team is not in team manager");
                team.setTeamInfo(player, teamInfo.get(team));
            });
            return true;
        }
        return false;
    }

    /**
     * Removes a player from this team manager.
     *
     * @param player player to remove from team manager.
     * @return if the player was removed.
     */
    public boolean removePlayer(Player player) {
        checkDestroyed();
        Preconditions.checkNotNull(player, "Player cannot be null");

        if (players.contains(player)) {
            teams.forEach(team -> {
                TeamInfo info = team.getTeamInfo(player);
                if (info != null) {
                    info.players.remove(player);
                    TeamPacketUtil.removeTeam(Collections.singleton(player), info);
                }
            });
            players.remove(player);
            return true;
        }
        return false;
    }

    /**
     * Destroys this team manager.
     */
    @Override
    public void destroy() {
        if (!destroyed) {
            new HashSet<>(teams).forEach(Team::destroy);
            plugin.teamManagers.remove(this);
            destroyed = true;
        }
    }

    private void checkDestroyed() {
        Preconditions.checkState(!destroyed, "Team manager is destroyed");
    }

    private void checkManagers(Player player) {
        if (checkManagers)
            plugin.teamManagers.forEach(manager -> {
                if (manager != this && manager.checkManagers)
                    manager.removePlayer(player);
            });
    }
}
