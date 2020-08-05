package net.VytskaLT.ScoreboardManager.team;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import lombok.experimental.UtilityClass;
import net.VytskaLT.ScoreboardManager.ScoreboardManagerPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;

import java.util.*;

@UtilityClass
class TeamPacketUtil {

    private final Map<NameTagVisibility, String> nameTagVisibilityIds = new HashMap<>();

    static {
        nameTagVisibilityIds.put(NameTagVisibility.ALWAYS, "always");
        nameTagVisibilityIds.put(NameTagVisibility.NEVER, "never");
        nameTagVisibilityIds.put(NameTagVisibility.HIDE_FOR_OTHER_TEAMS, "hideForOtherTeams");
        nameTagVisibilityIds.put(NameTagVisibility.HIDE_FOR_OWN_TEAM, "hideForOwnTeam");
    }

    public void addEntries(Set<Player> players, TeamInfo teamInfo, Set<String> entries) {
        teamEntry(players, teamInfo, entries, 3);
    }

    public void removeEntries(Set<Player> players, TeamInfo teamInfo, Set<String> entries) {
        teamEntry(players, teamInfo, entries, 4);
    }

    private void teamEntry(Set<Player> players, TeamInfo teamInfo, Set<String> entries, int action) {
        if (players.isEmpty()) return;
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        packet.getStrings().write(0, teamInfo.getName());
        packet.getIntegers().write(1, action);
        packet.getSpecificModifier(Collection.class).write(0, new ArrayList<>(entries));
        ScoreboardManagerPlugin.sendPacket(players, packet);
    }

    public void createTeam(Set<Player> players, TeamInfo info) {
        team(players, 0, info);
    }

    public void updateTeam(Set<Player> players, TeamInfo teamInfo) {
        team(players, 2, teamInfo);
    }

    private void team(Set<Player> players, int mode, TeamInfo teamInfo) {
        if (players.isEmpty()) return;
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        packet.getIntegers().write(1, mode);
        packet.getStrings().write(0, teamInfo.getName());
        packet.getStrings().write(1, teamInfo.getDisplayName() == null ? teamInfo.getName() : teamInfo.getDisplayName());
        packet.getStrings().write(2, teamInfo.getPrefix() == null ? "" : teamInfo.getPrefix());
        packet.getStrings().write(3, teamInfo.getSuffix() == null ? "" : teamInfo.getSuffix());
        packet.getStrings().write(4, nameTagVisibilityIds.get(teamInfo.getNameTagVisibility()));
        packet.getIntegers().write(0, -1);
        packet.getIntegers().write(2, teamInfo.isAllowFriendlyFire() ? teamInfo.isCanSeeFriendlyInvisibles() ? 3 : 1 : 0);
        if (mode == 0)
            packet.getModifier().write(6, new ArrayList<>(teamInfo.getEntries()));
        ScoreboardManagerPlugin.sendPacket(players, packet);
    }

    public void removeTeam(Set<Player> players, TeamInfo teamInfo) {
        if (players.isEmpty()) return;
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        packet.getIntegers().write(1, 1);
        packet.getStrings().write(0, teamInfo.getName());
        ScoreboardManagerPlugin.sendPacket(players, packet);
    }
}
