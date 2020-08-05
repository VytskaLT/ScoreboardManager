package net.VytskaLT.ScoreboardManager.team;

import com.google.common.base.Preconditions;
import net.VytskaLT.ScoreboardManager.ScoreboardManagerPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TeamUpdaterTask extends BukkitRunnable {

    private final ScoreboardManagerPlugin plugin;

    public TeamUpdaterTask(ScoreboardManagerPlugin plugin) {
        Preconditions.checkNotNull(plugin);
        this.plugin = plugin;

        runTaskTimer(plugin, 1, 1);
    }

    @Override
    public void run() {
        plugin.teamManagers.forEach(manager -> manager.teams.forEach(team -> team.infoSet.forEach(info -> {
            if (info.updateTeam) {
                TeamPacketUtil.updateTeam(info.players, info);
                info.updateTeam = false;
            }
            if (info.updateEntries) {
                if (!info.addEntries.isEmpty()) {
                    TeamPacketUtil.addEntries(info.players, info, info.addEntries);
                    info.addEntries.clear();
                }
                if (!info.removeEntries.isEmpty()) {
                    TeamPacketUtil.removeEntries(info.players, info, info.removeEntries);
                    info.removeEntries.clear();
                }
                info.updateEntries = false;
            }
        })));
    }
}
