package net.VytskaLT.ScoreboardManager.team;

import com.google.common.base.Preconditions;
import org.bukkit.scoreboard.NameTagVisibility;

import java.util.HashSet;
import java.util.Set;

public class TeamInfoBuilder {

    private String displayName, prefix, suffix;
    private boolean allowFriendlyFire, canSeeFriendlyInvisibles;
    private NameTagVisibility nameTagVisibility;
    private final Set<String> entries = new HashSet<>();

    public TeamInfo build() {
        TeamInfo teamInfo = new TeamInfo();
        teamInfo.setDisplayName(displayName);
        teamInfo.setPrefix(prefix);
        teamInfo.setSuffix(suffix);
        teamInfo.setAllowFriendlyFire(allowFriendlyFire);
        teamInfo.setCanSeeFriendlyInvisibles(canSeeFriendlyInvisibles);
        teamInfo.setNameTagVisibility(nameTagVisibility == null ? NameTagVisibility.ALWAYS : nameTagVisibility);
        teamInfo.entries.addAll(entries);
        return teamInfo;
    }

    public TeamInfoBuilder addEntry(String entry) {
        Preconditions.checkNotNull(entry);
        entries.add(entry);
        return this;
    }

    public TeamInfoBuilder removeEntry(String entry) {
        Preconditions.checkNotNull(entry);
        entries.remove(entry);
        return this;
    }

    public TeamInfoBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public TeamInfoBuilder prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public TeamInfoBuilder suffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public TeamInfoBuilder allowFriendlyFire(boolean allowFriendlyFire) {
        this.allowFriendlyFire = allowFriendlyFire;
        return this;
    }

    public TeamInfoBuilder canSeeFriendlyInvisibles(boolean canSeeFriendlyInvisibles) {
        this.canSeeFriendlyInvisibles = canSeeFriendlyInvisibles;
        return this;
    }

    public TeamInfoBuilder nameTagVisibility(NameTagVisibility nameTagVisibility) {
        this.nameTagVisibility = nameTagVisibility;
        return this;
    }
}
