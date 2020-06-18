package net.VytskaLT.ScoreboardManager;

import net.VytskaLT.ScoreboardManager.scoreboard.TeamManager;
import net.VytskaLT.ScoreboardManager.scoreboard.TeamManagerImpl;
import net.VytskaLT.ScoreboardManager.sidebar.Sidebar;
import net.VytskaLT.ScoreboardManager.sidebar.SidebarImpl;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;

public class ScoreboardManagerPlugin extends JavaPlugin implements Listener {

    private static ScoreboardManagerPlugin instance;

    public final Set<TeamManagerImpl> scoreboardManagers = new HashSet<>();
    public final Set<SidebarImpl> sidebarManagers = new HashSet<>();

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    public static Sidebar createSidebar() {
        return new SidebarImpl(instance);
    }

    public static TeamManager createTeamManager() {
        return createTeamManager(instance.getServer().getScoreboardManager().getNewScoreboard());
    }

    public static TeamManager createTeamManager(Scoreboard scoreboard) {
        return new TeamManagerImpl(instance, scoreboard);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            return false;
        }
        Player p = (Player) sender;
        TeamManager manager = createTeamManager();
        manager.addPlayer(p);
        Team ownerTeam = manager.getAndCreateTeam("owner");
        ownerTeam.setPrefix(ChatColor.RED + "[oWnER] ");
        manager.addEntryFor(p, p.getName(), ownerTeam);
        return true;
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        scoreboardManagers.forEach(manager -> manager.removePlayer(p));
        sidebarManagers.forEach(manager -> manager.removePlayer(p));
    }
}
