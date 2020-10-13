package net.VytskaLT.ScoreboardManager.sidebar;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.VytskaLT.ScoreboardManager.ScoreboardManagerPlugin;
import net.VytskaLT.ScoreboardManager.team.Team;
import net.VytskaLT.ScoreboardManager.team.TeamInfo;
import net.VytskaLT.ScoreboardManager.team.TeamManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Stream;

public class SidebarImpl implements Sidebar {

    private static final Map<Integer, ChatColor> lineColors = new HashMap<>();

    static {
        int i = 0;
        for (ChatColor color : ChatColor.values()) {
            lineColors.put(i, color);
            i++;
        }
    }

    private final ScoreboardManagerPlugin plugin;
    final Set<Player> players = new HashSet<>();
    @Getter
    boolean visible;
    @Getter
    private boolean destroyed;
    @Getter
    String title;
    final Line[] lines = new Line[15];
    @Getter
    private final TeamManager teamManager;
    boolean updateTitle;

    public SidebarImpl() {
        this.teamManager = new TeamManager(null, false);

        plugin = ScoreboardManagerPlugin.getPlugin(ScoreboardManagerPlugin.class);
        Preconditions.checkNotNull(plugin, "Plugin not initialized. Did you put it in your plugins folder?");
        plugin.sidebars.forEach(manager -> players.forEach(player -> {
            if (manager.getPlayers().contains(player))
                manager.removePlayer(player);
        }));
        plugin.sidebars.add(this);

        for (int i = 0; i < lines.length; i++)
            lines[i] = new Line(i);
    }

    @Override
    public Set<Player> getPlayers() {
        checkDestroyed();
        return Collections.unmodifiableSet(players);
    }

    @Override
    public void addPlayer(Player player) {
        checkDestroyed();
        Preconditions.checkNotNull(player);
        if (!players.contains(player)) {
            if (visible) {
                teamManager.addPlayer(player);

                Set<Player> p = Collections.singleton(player);
                SidebarPacketUtil.create(p, title);
                SidebarPacketUtil.displayScoreboard(p);
                sendLines(p);
            }
            checkManagers(player);
            players.add(player);
        }
    }

    @Override
    public void removePlayer(Player player) {
        checkDestroyed();
        Preconditions.checkNotNull(player);
        if (players.contains(player)) {
            if (visible) {
                Set<Player> p = Collections.singleton(player);
                SidebarPacketUtil.remove(p);
                removeLines(p);
                teamManager.removePlayer(player);
            }
            players.remove(player);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        checkDestroyed();
        if (visible != this.visible) {
            this.visible = visible;
            if (visible) {
                players.forEach(teamManager::addPlayer);
                SidebarPacketUtil.create(players, title);
                sendLines(players);
                SidebarPacketUtil.displayScoreboard(players);
            } else {
                players.forEach(teamManager::removePlayer);
                SidebarPacketUtil.remove(players);
                removeLines(players);
            }
        }
    }

    @Override
    public void removeLine(int line) {
        checkDestroyed();
        Line sLine = lines[line];
        if (sLine.value == null)
            return;

        sLine.value = null;
        if (sLine.player != null && visible) {
            sLine.updatePlayer = false;
            sLine.remove = true;
        }
    }

    @Override
    public void setLine(int line, String value) {
        checkDestroyed();
        checkLine(line);

        if (value == null) {
            removeLine(line);
            return;
        }

        Line sLine = lines[line];

        if (value.equals(sLine.value))
            return;

        sLine.oldPlayer = sLine.player;

        sLine.setValue(value);
        sLine.update();

        if (visible) {
            sLine.updatePlayer = true;
            sLine.remove = false;
        }
    }

    @Override
    public String getLine(int line) {
        checkDestroyed();
        checkLine(line);
        return lines[line].value;
    }

    @Override
    public void setTitle(String title) {
        checkDestroyed();
        Preconditions.checkNotNull(title);
        Preconditions.checkState(title.length() <= 32, "Title cannot be longer than 32 characters");
        this.title = title;
        if (visible)
            updateTitle = true;
    }

    @Override
    public void destroy() {
        if (!destroyed) {
            setVisible(false);
            plugin.sidebars.remove(this);
            destroyed = true;
        }
    }

    private void checkManagers(Player player) {
        plugin.sidebars.forEach(manager -> manager.removePlayer(player));
    }

    void sendLine(Set<Player> players, Line line) {
        checkDestroyed();
        if (!visible) return;

        updateScores();
        line.updateScore(players);
    }

    private void updateScores() {
        int size = (int) Stream.of(lines).filter(team -> team.value != null).count();
        int i = 0;
        for (Line team : lines)
            if (team.value != null) {
                team.score = size - i - 1;
                i++;
            }
    }

    private void sendLines(Set<Player> players) {
        for (Line line : lines)
            if (line.value != null)
                sendLine(players, line);
    }

    private void removeLines(Set<Player> players) {
        for (Line team : lines) {
            team.team.destroy();
            SidebarPacketUtil.removeLine(players, team.player);
        }
    }

    private void checkLine(int line) {
        if (line > 14 || line < 0) throw new IndexOutOfBoundsException(String.valueOf(line));
    }

    private void checkDestroyed() {
        if (destroyed) throw new IllegalStateException("Sidebar is destroyed");
    }

    class Line {

        public int score;
        public final int line;
        public final Team team;
        public String value;
        public String player, oldPlayer;
        public boolean updatePlayer, remove;

        public Line(int line) {
            this.line = line;
            this.team = teamManager.getOrCreateTeam("s" + line);
            player = lineColors.get(line).toString();
        }

        public void update() {
            if (oldPlayer != null)
                team.getGlobalInfo().removeEntry(oldPlayer);
            team.getGlobalInfo().addEntry(player);
        }

        public void updateScore(Set<Player> players) {
            SidebarPacketUtil.score(players, score, player);
        }

        public void setValue(String value) {
            value = value.substring(0, Math.min(value.length(), 32));

            this.value = value;

            TeamInfo teamInfo = team.getGlobalInfo();
            if (value.length() <= 16) {
                teamInfo.setPrefix(value);
                teamInfo.setSuffix("");
            } else {
                String prefix = value.substring(0, 16);
                teamInfo.setPrefix(prefix);
                teamInfo.setSuffix(value.substring(16));

                String reset = ChatColor.getLastColors(prefix);
                player = lineColors.get(line) + ChatColor.RESET.toString() + reset;
            }
        }
    }
}
