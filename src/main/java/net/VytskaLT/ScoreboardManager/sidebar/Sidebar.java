package net.VytskaLT.ScoreboardManager.sidebar;

import org.bukkit.entity.Player;

import javax.security.auth.Destroyable;
import java.util.Set;

public interface Sidebar extends Destroyable {

    /**
     * Returns an unmodifiable set of players in this sidebar.
     *
     * @return an unmodifiable set of players in this sidebar.
     */
    Set<Player> getPlayers();

    /**
     * Adds a player to this sidebar. If this player is already in a sidebar, it will remove it from it.
     *
     * @param player player to add to this sidebar.
     */
    void addPlayer(Player player);

    /**
     * Adds a player to this sidebar.
     *
     * @param player player to remove to this sidebar.
     */
    void removePlayer(Player player);

    /**
     * Changes the visibility of this sidebar.
     *
     * @param visible new visibility of this sidebar.
     *                <code>true</code> if sidebar should be visible,
     *                <code>false</code> if sidebar should be hidden.
     */
    void setVisible(boolean visible);

    /**
     * Returns the visibility of this sidebar.
     * @return visibility of this sidebar.
     */
    boolean isVisible();

    /**
     * Destroys this sidebar.
     */
    void destroy();

    /**
     * Returns if this sidebar is destroyed.
     *
     * @return if this sidebar is destroyed.
     */
    boolean isDestroyed();

    /**
     * Removes a line. Equivalent to <code>setLine(line, null)</code>.
     *
     * @param line line to remove.
     */
    void removeLine(int line);

    /**
     * Sets a line's value.
     *
     * @param line line to set the value to.
     * @param value new value of line.
     */
    void setLine(int line, String value);

    /**
     * Gets a line's value.
     *
     * @param line line to get value of.
     * @return value of line.
     */
    String getLine(int line);

    /**
     * Sets the title of this sidebar.
     *
     * @param title new title of sidebar.
     */
    void setTitle(String title);

    /**
     * Returns the current title of this sidebar.
     *
     * @return current title of this sidebar.
     */
    String getTitle();
}
