package net.VytskaLT.ScoreboardManager.sidebar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import lombok.experimental.UtilityClass;
import net.VytskaLT.ScoreboardManager.ScoreboardManagerPlugin;
import org.bukkit.entity.Player;

import java.util.Set;

@UtilityClass
class SidebarPacketUtil {

    private final String objectiveName = "sidebar";

    public void create(Set<Player> players, String displayName) {
        objective(players, 0, displayName);
    }

    public void remove(Set<Player> players) {
        objective(players, 1, null);
    }

    public void update(Set<Player> players, String displayName) {
        objective(players, 2, displayName);
    }

    private void objective(Set<Player> players, int mode, String displayName) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
        packet.getStrings().write(0, objectiveName);
        packet.getIntegers().write(0, mode);
        if (mode == 0 || mode == 2) {
            packet.getStrings().write(1, displayName == null ? "" : displayName);
            packet.getEnumModifier(HealthDisplay.class, 2).write(0, HealthDisplay.INTEGER);
        }
        ScoreboardManagerPlugin.sendPacket(players, packet);
    }

    public void removeLine(Set<Player> players, String line) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE);
        packet.getStrings().write(0, line);
        packet.getScoreboardActions().write(0, EnumWrappers.ScoreboardAction.REMOVE);
        ScoreboardManagerPlugin.sendPacket(players, packet);
    }

    public void score(Set<Player> players, int score, String line) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE);
        packet.getStrings().write(0, line);
        packet.getStrings().write(1, objectiveName);
        packet.getIntegers().write(0, score);
        packet.getScoreboardActions().write(0, EnumWrappers.ScoreboardAction.CHANGE);
        ScoreboardManagerPlugin.sendPacket(players, packet);
    }

    public void displayScoreboard(Set<Player> players) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
        packet.getIntegers().write(0, 1);
        packet.getStrings().write(0, objectiveName);
        ScoreboardManagerPlugin.sendPacket(players, packet);
    }

    private enum HealthDisplay {
        INTEGER, HEARTS
    }
}
