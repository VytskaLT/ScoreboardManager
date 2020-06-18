package net.VytskaLT.ScoreboardManager.scoreboard;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.VytskaLT.ScoreboardManager.ScoreboardManagerPlugin;
import net.VytskaLT.ScoreboardManager.ScoreboardPacketUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class TeamManagerImpl implements TeamManager {

    private final ScoreboardManagerPlugin plugin;
    @Getter
    private final Scoreboard scoreboard;
    private final Set<Player> players = new HashSet<>();
    private final Map<Player, Map<Team, Map<String, Boolean>>> entries = new HashMap<>();
    private boolean destroyed;

    public TeamManagerImpl(ScoreboardManagerPlugin plugin, Scoreboard scoreboard) {
        Preconditions.checkNotNull(plugin);
        this.plugin = plugin;
        this.scoreboard = scoreboard;
        plugin.scoreboardManagers.add(this);
    }

    @Override
    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    @Override
    public void addPlayer(Player player) {
        checkDestroyed();
        if(!players.contains(player)) {
            player.setScoreboard(scoreboard);
            players.add(player);
            entries.put(player, new HashMap<>());
        }
    }

    @Override
    public void removePlayer(Player player) {
        checkDestroyed();
        if(players.contains(player)) {
            leaveTeam(player);
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            players.remove(player);
            Map<Team, Map<String, Boolean>> map = entries.get(player);
            map.forEach((team, entryMap) -> {
                List<String> entries = new ArrayList<>();
                entryMap.forEach((entry, bool) -> {
                    if(bool)
                        entries.add(entry);
                });
                removeEntryFor(player, entries, team);
            });
            entries.remove(player);
        }
    }

    @Override
    public void destroy() {
        if(!destroyed) {
            players.forEach(this::removePlayer);
            plugin.scoreboardManagers.remove(this);
            destroyed = true;
        }
    }

    @Override
    public void joinTeam(Player player, Team team) {
        Preconditions.checkState(players.contains(player));
        checkDestroyed();
        leaveTeam(player);
        joinTeam(player.getName(), team);
    }

    @Override
    public void joinTeam(String entry, Team team) {
        checkDestroyed();
        entries.forEach((player, teamMap) -> {
            Map<String, Boolean> entryMap = teamMap.get(team);
            if(entryMap != null && entryMap.get(entry))
                entryMap.remove(entry);
        });
        team.addEntry(entry);
    }

    @Override
    public void addEntryFor(Player player, String entry, Team team) {
        addEntryFor(player, Collections.singletonList(entry), team);
    }

    @Override
    public void addEntryFor(Player player, List<String> entry, Team team) {
        Preconditions.checkState(players.contains(player));
        checkDestroyed();
        entries.get(player).computeIfAbsent(team, k -> new HashMap<>());
        Map<String, Boolean> map = entries.get(player).get(team);
        try {
            ScoreboardPacketUtil.addEntry(team, player, entry);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return;
        }
        entry.forEach(str -> {
            map.replace(str, true);
            map.put(str, true);
        });
    }

    @Override
    public void removeEntryFor(Player player, String entry, Team team) {
        removeEntryFor(player, Collections.singletonList(entry), team);
    }

    @Override
    public void removeEntryFor(Player player, List<String> entry, Team team) {
        Preconditions.checkState(players.contains(player));
        checkDestroyed();
        entries.get(player).computeIfAbsent(team, k -> new HashMap<>());
        Map<String, Boolean> map = entries.get(player).get(team);
        try {
            ScoreboardPacketUtil.removeEntry(team, player, entry);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return;
        }
        entry.forEach(str -> {
            map.replace(str, false);
            map.put(str, false);
        });
    }

    @Override
    public List<String> getEntriesFor(Player player, Team team) {
        Preconditions.checkState(players.contains(player));
        checkDestroyed();
        List<String> teamEntries = new ArrayList<>(team.getEntries());
        Map<String, Boolean> map = entries.get(player).get(team);
        if(map == null)
            return teamEntries;
        map.forEach((entry, bool) -> {
            if(bool)
                teamEntries.add(entry);
            else
                teamEntries.remove(entry);
        });
        return teamEntries;
    }

    @Override
    public void leaveTeam(Player player) {
        Preconditions.checkState(players.contains(player));
        checkDestroyed();
        Team team = getTeam(player);
        if(team != null)
            team.removeEntry(player.getName());
    }

    @Override
    public Team createTeam(String name) {
        checkDestroyed();
        Team t = scoreboard.getTeam(name);
        if(t != null) t.unregister();
        return scoreboard.registerNewTeam(name);
    }

    @Override
    public Team getAndCreateTeam(String name) {
        checkDestroyed();
        Team team = scoreboard.getTeam(name);
        if(team == null)
            team = scoreboard.registerNewTeam(name);
        return team;
    }

    @Override
    public Team getTeam(Player player) {
        checkDestroyed();
        if(players.contains(player))
            for(Team team : scoreboard.getTeams())
                if(team.hasEntry(player.getName()))
                    return team;
        return null;
    }

    private void checkDestroyed() {
        if(destroyed) throw new IllegalStateException("Team manager is destroyed");
    }
}
