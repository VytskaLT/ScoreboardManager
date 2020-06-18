package net.VytskaLT.ScoreboardManager.sidebar;

import org.bukkit.entity.Player;

import java.util.Set;

public interface Sidebar {
    Set<Player> getPlayers();
    void addPlayer(Player player);
    void removePlayer(Player player);

    void setVisible(boolean visible);
    boolean isVisible();
    void destroy();

    void removeLine(int line);
    void setLine(int line, String text);
    String getLine(int line);

    void setTitle(String title);
    String getTitle();
}
