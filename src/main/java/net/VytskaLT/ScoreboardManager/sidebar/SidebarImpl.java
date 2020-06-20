package net.VytskaLT.ScoreboardManager.sidebar;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.VytskaLT.ScoreboardManager.ScoreboardManagerPlugin;
import net.VytskaLT.ScoreboardManager.ScoreboardPacketUtil;
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
    private final Set<Player> players = new HashSet<>();
    @Getter
    private boolean visible, destroyed;
    @Getter
    private String title;
    private final VirtualTeam[] teams = new VirtualTeam[15];

    public SidebarImpl(ScoreboardManagerPlugin plugin) {
        Preconditions.checkNotNull(plugin);
        this.plugin = plugin;
        plugin.sidebarManagers.forEach(manager -> players.forEach(player -> {
            if(manager.getPlayers().contains(player))
                manager.removePlayer(player);
        }));
        plugin.sidebarManagers.add(this);
    }

    @Override
    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    @Override
    public void addPlayer(Player player) {
        Preconditions.checkNotNull(player);
        checkDestroyed();
        if(!players.contains(player)) {
            if(visible) {
                Set<Player> p = Collections.singleton(player);
                ScoreboardPacketUtil.createObjective(p, title);
                ScoreboardPacketUtil.displayScoreboard(p);
                sendLines(p);
            }
            players.add(player);
        }
    }

    @Override
    public void removePlayer(Player player) {
        Preconditions.checkNotNull(player);
        checkDestroyed();
        if(players.contains(player)) {
            if(visible) {
                Set<Player> p = Collections.singleton(player);
                removeLines(p);
                ScoreboardPacketUtil.removeObjective(p);
            }
            players.remove(player);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        checkDestroyed();
        if(visible == !this.visible) {
            if(visible) {
                Preconditions.checkNotNull(title);
                ScoreboardPacketUtil.createObjective(players, title);
                ScoreboardPacketUtil.displayScoreboard(players);
                sendLines(players);
            } else {
                ScoreboardPacketUtil.removeObjective(players);
                removeLines(players);
            }
            this.visible = visible;
        }
    }

    @Override
    public void destroy() {
        if(!destroyed) {
            setVisible(false);
            plugin.sidebarManagers.remove(this);
            destroyed = true;
        }
    }

    @Override
    public void removeLine(int line) {
        checkDestroyed();
        VirtualTeam team = teams[line];
        if(team == null) return;
        String old = team.player;

        if (old != null && visible) {
            ScoreboardPacketUtil.removeLine(players, old);
            ScoreboardPacketUtil.removeTeam(players, team.name);
        }

        teams[line] = null;
    }

    @Override
    public void setLine(int line, String value) {
        Preconditions.checkNotNull(value);
        checkLine(line);
        checkDestroyed();
        VirtualTeam team = getOrCreateTeam(line);
        String old = team.player;

        team.setValue(value);
        if (old != null && visible) {
            ScoreboardPacketUtil.removeTeam(players, team.name);
            if(!old.equals(team.player))
                ScoreboardPacketUtil.removeLine(players, old);
        }

        if(visible) team.update(old);
        sendLine(players, line, visible);
        for (VirtualTeam l : teams) {
            if(l != null && l != team) l.updateScore(players);
        }
    }

    @Override
    public String getLine(int line) {
        checkDestroyed();
        checkLine(line);
        VirtualTeam team = teams[line];
        return team == null ? null : team.value;
    }

    @Override
    public void setTitle(String title) {
        Preconditions.checkNotNull(title);
        Preconditions.checkState(title.length() <= 32, "Title cannot be longer than 32 characters");
        checkDestroyed();
        this.title = title;
        if(visible) ScoreboardPacketUtil.updateObjective(players, title);
    }

    private void sendLine(Set<Player> players, int line, boolean first) {
        checkDestroyed();
        checkLine(line);
        if (!visible) return;

        updateScores();
        VirtualTeam team = getOrCreateTeam(line);
        if(first) team.create(players);
        team.updateScore(players);
    }

    private void updateScores() {
        int size = (int) Stream.of(teams).filter(Objects::nonNull).count();
        int i = 0;
        for (VirtualTeam team : teams) {
            if(team != null) {
                team.score = size - i;
                i++;
            }
        }
    }

    private VirtualTeam getOrCreateTeam(int line) {
        if (teams[line] == null)
            teams[line] = new VirtualTeam(line);
        return teams[line];
    }

    private void sendLines(Set<Player> players) {
        for (VirtualTeam team : teams) {
            if(team != null) sendLine(players, team.line, true);
        }
    }

    private void removeLines(Set<Player> players) {
        for (VirtualTeam line : teams) {
            if(line != null) {
                ScoreboardPacketUtil.removeTeam(players, line.name);
                ScoreboardPacketUtil.removeLine(players, line.player);
            }
        }
    }

    private void checkLine(int line) {
        if (line > 14 || line < 0) throw new IndexOutOfBoundsException(String.valueOf(line));
    }

    private void checkDestroyed() {
        if(destroyed) throw new IllegalStateException("Sidebar is destroyed");
    }

    public class VirtualTeam {

        public int score;
        public final int line;
        public final String name;
        public String prefix = "", suffix = "", player, value;

        public VirtualTeam(int line) {
            this.line = line;
            this.name = "s" + line;
        }

        public void create(Set<Player> players) {
            ScoreboardPacketUtil.createTeam(players, name, prefix, suffix);
            ScoreboardPacketUtil.addPlayer(players, name, player);
        }

        public void update(String oldPlayer) {
            ScoreboardPacketUtil.updateTeam(players, name, prefix, suffix);
            if(oldPlayer != null && !oldPlayer.equals(player)) {
                ScoreboardPacketUtil.removePlayer(players, name, oldPlayer);
                ScoreboardPacketUtil.addPlayer(players, name, player);
            }
        }

        public void updateScore(Set<Player> players) {
            ScoreboardPacketUtil.score(players, score, player);
        }

        public void setValue(String value) {
            if(value.length() > 48) throw new IllegalArgumentException("Value cannot be longer than 48 characters");
            this.value = value;

            /*boolean lastColorChar = false;
            ChatColor color = null;
            ChatColor formatColor = null;
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if(lastColorChar) {
                    lastColorChar = false;
                    ChatColor chatColor = ChatColor.getByChar(c);
                    if(chatColor == null) continue;
                    if(!chatColor.isFormat()) {
                        color = chatColor;
                        formatColor = null;
                    } else
                        formatColor = chatColor;
                } else if(c == ChatColor.COLOR_CHAR) lastColorChar = true;
            }*/
            String reset = ChatColor.getLastColors(value);

            player = lineColors.get(line) + ChatColor.RESET.toString() + reset;
            if (value.length() <= 16) {
                prefix = value;
                suffix = "";
            } else if (value.length() <= 32) {
                prefix = value.substring(0, 16);
                suffix = value.substring(16);
            } else if(value.length() <= 48-reset.length()) {
                prefix = value.substring(0, 16);
                suffix = value.substring(32);
                player = player + value.substring(16, 32);
            } else {
                prefix = value.substring(0, 16);
                suffix = value.substring(32);
                player = value.substring(16, 32);
            }
        }
    }
}
