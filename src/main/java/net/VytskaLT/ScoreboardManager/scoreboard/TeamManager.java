package net.VytskaLT.ScoreboardManager.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Set;

public interface TeamManager {
    Set<Player> getPlayers();
    void addPlayer(Player player);
    void removePlayer(Player player);

    void destroy();

    void joinTeam(Player player, Team team);
    void joinTeam(String entry, Team team);
    void addEntryFor(Player player, String entry, Team team);
    void addEntryFor(Player player, List<String> entry, Team team);
    void removeEntryFor(Player player, String entry, Team team);
    void removeEntryFor(Player player, List<String> entry, Team team);
    List<String> getEntriesFor(Player player, Team team);
    void leaveTeam(Player player);
    Team createTeam(String name);
    Team getAndCreateTeam(String name);
    Team getTeam(Player player);
}
