package net.VytskaLT.ScoreboardManager;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import lombok.SneakyThrows;
import net.VytskaLT.ScoreboardManager.sidebar.SidebarImpl;
import net.VytskaLT.ScoreboardManager.sidebar.SidebarUpdaterTask;
import net.VytskaLT.ScoreboardManager.team.TeamManager;
import net.VytskaLT.ScoreboardManager.team.TeamUpdaterTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class ScoreboardManagerPlugin extends JavaPlugin implements Listener {

    public final Set<TeamManager> teamManagers = new HashSet<>();
    public final Set<SidebarImpl> sidebars = new HashSet<>();

    @Override
    public void onEnable() {
        new TeamUpdaterTask(this);
        new SidebarUpdaterTask(this);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        new HashSet<>(sidebars).forEach(SidebarImpl::destroy);
        new HashSet<>(teamManagers).forEach(TeamManager::destroy);
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        teamManagers.forEach(manager -> manager.removePlayer(p));
        sidebars.forEach(manager -> manager.removePlayer(p));
    }

    @SneakyThrows
    @Deprecated
    public static void sendPacket(Set<Player> players, PacketContainer packet) {
        for (Player player : players)
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }
}
