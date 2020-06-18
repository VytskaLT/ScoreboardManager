package net.VytskaLT.ScoreboardManager;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@UtilityClass
public class ScoreboardPacketUtil {

    private final ProtocolManager manager = ProtocolLibrary.getProtocolManager();

    public void addEntry(Team team, Player player, List<String> entries) throws InvocationTargetException {
        PacketContainer addPacket = manager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        addPacket.getModifier().writeDefaults();
        addPacket.getStrings().write(0, team.getName());
        addPacket.getIntegers().write(1, 3);
        addPacket.getSpecificModifier(Collection.class).write(0, entries);
        manager.sendServerPacket(player, addPacket);
    }

    public void removeEntry(Team team, Player player, List<String> entries) throws InvocationTargetException {
        PacketContainer addPacket = manager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        addPacket.getModifier().writeDefaults();
        addPacket.getStrings().write(0, team.getName());
        addPacket.getIntegers().write(1, 4);
        addPacket.getSpecificModifier(Collection.class).write(0, entries);
        manager.sendServerPacket(player, addPacket);
    }

    public void createTeam(Set<Player> players, String name, String prefix, String suffix) {
        team(players, 0, name, prefix, suffix);
    }

    public void removeTeam(Set<Player> players, String name) {
        PacketContainer teamPacket = manager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        teamPacket.getStrings().write(0, name);
        teamPacket.getIntegers().write(1, 1);
        try {
            for (Player player : players) {
                manager.sendServerPacket(player, teamPacket);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void updateTeam(Set<Player> players, String name, String prefix, String suffix) {
        team(players, 2, name, prefix, suffix);
    }

    private void team(Set<Player> players, int mode, String name, String prefix, String suffix) {
        PacketContainer teamPacket = manager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        teamPacket.getStrings().write(0, name);
        teamPacket.getIntegers().write(0, mode);
        teamPacket.getStrings().write(1, "");
        teamPacket.getStrings().write(2, prefix);
        teamPacket.getStrings().write(3, suffix);
        teamPacket.getIntegers().write(1, 0);
        teamPacket.getStrings().write(4, "always");
        teamPacket.getIntegers().write(2, 0);
        try {
            for (Player player : players) {
                manager.sendServerPacket(player, teamPacket);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void addPlayer(Set<Player> players, String name, String playerName) {
        addOrRemovePlayer(players, 3, name, playerName);
    }

    public void removePlayer(Set<Player> players, String name, String playerName) {
        addOrRemovePlayer(players, 4, name, playerName);
    }

    private void addOrRemovePlayer(Set<Player> players, int mode, String name, String playerName) {
        PacketContainer teamPacket = manager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        teamPacket.getStrings().write(0, name);
        teamPacket.getIntegers().write(1, mode);
        teamPacket.getSpecificModifier(Collection.class).write(0, Collections.singletonList(playerName));
        try {
            for (Player player : players) {
                manager.sendServerPacket(player, teamPacket);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void createObjective(Set<Player> players, String displayName) {
        objective(players, 0, displayName);
    }

    public void removeObjective(Set<Player> players) {
        objective(players, 1, null);
    }

    public void updateObjective(Set<Player> players, String displayName) {
        objective(players, 2, displayName);
    }

    private void objective(Set<Player> players, int mode, String displayName) {
        PacketContainer objPacket = manager.createPacket(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
        objPacket.getStrings().write(0, "sidebar"); // Objective name
        objPacket.getIntegers().write(0, mode); // Mode
        if(mode == 0 || mode == 2) {
            objPacket.getStrings().write(1, displayName); // Display name
            objPacket.getEnumModifier(HealthDisplay.class, 2).write(0, HealthDisplay.INTEGER); // Health display
        }
        try {
            for (Player player : players) {
                manager.sendServerPacket(player, objPacket);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void removeLine(Set<Player> players, String line) {
        PacketContainer scorePacket = manager.createPacket(PacketType.Play.Server.SCOREBOARD_SCORE);
        scorePacket.getStrings().write(0, line);
        scorePacket.getScoreboardActions().write(0, EnumWrappers.ScoreboardAction.REMOVE);
        try {
            for (Player player : players) {
                manager.sendServerPacket(player, scorePacket);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void score(Set<Player> players, int score, String line) {
        PacketContainer scorePacket = manager.createPacket(PacketType.Play.Server.SCOREBOARD_SCORE);
        scorePacket.getStrings().write(0, line);
        scorePacket.getStrings().write(1, "sidebar");
        scorePacket.getIntegers().write(0, score);
        scorePacket.getScoreboardActions().write(0, EnumWrappers.ScoreboardAction.CHANGE);
        try {
            for (Player player : players) {
                manager.sendServerPacket(player, scorePacket);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void displayScoreboard(Set<Player> players) {
        PacketContainer displayPacket = manager.createPacket(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
        displayPacket.getIntegers().write(0, 1); // Display type - sidebar
        displayPacket.getStrings().write(0, "sidebar");
        try {
            for (Player player : players) {
                manager.sendServerPacket(player, displayPacket);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public enum HealthDisplay {
        INTEGER, HEARTS
    }
}
