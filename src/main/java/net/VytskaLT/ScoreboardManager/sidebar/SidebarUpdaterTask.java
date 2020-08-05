package net.VytskaLT.ScoreboardManager.sidebar;

import com.google.common.base.Preconditions;
import net.VytskaLT.ScoreboardManager.ScoreboardManagerPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SidebarUpdaterTask extends BukkitRunnable {

    private final ScoreboardManagerPlugin plugin;

    public SidebarUpdaterTask(ScoreboardManagerPlugin plugin) {
        Preconditions.checkNotNull(plugin);
        this.plugin = plugin;

        runTaskTimer(plugin, 1, 1);
    }

    @Override
    public void run() {
        plugin.sidebars.forEach(sidebar -> {
            if (sidebar.updateTitle) {
                if (sidebar.visible)
                    SidebarPacketUtil.update(sidebar.players, sidebar.title);
                sidebar.updateTitle = false;
            }
            for (SidebarImpl.Line line : sidebar.lines) {
                if (line != null) {
                    if (line.remove) {
                        if (sidebar.visible)
                            SidebarPacketUtil.removeLine(sidebar.players, line.player);
                        line.remove = false;
                    } else if (line.updatePlayer) {
                        if (sidebar.visible) {
                            if (line.oldPlayer != null && !line.oldPlayer.equals(line.player))
                                SidebarPacketUtil.removeLine(sidebar.players, line.oldPlayer);

                            line.update();
                            sidebar.sendLine(sidebar.players, line);
                            for (SidebarImpl.Line l : sidebar.lines)
                                if (l != line && l.value != null)
                                    l.updateScore(sidebar.players);
                        }
                        line.updatePlayer = false;
                    }
                }
            }
        });
    }
}
