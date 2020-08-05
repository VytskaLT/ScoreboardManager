package net.VytskaLT.ScoreboardManager.team;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
public class TeamInfo {

    Team team;
    final Set<Player> players = new HashSet<>();
    boolean updateTeam, updateEntries;
    @Getter
    private String displayName, prefix, suffix;
    @Getter
    private boolean allowFriendlyFire, canSeeFriendlyInvisibles;
    @Getter
    private NameTagVisibility nameTagVisibility = NameTagVisibility.ALWAYS;
    final Set<String> entries = new HashSet<>();
    final Set<String> addEntries = new HashSet<>();
    final Set<String> removeEntries = new HashSet<>();

    TeamInfo(Team team) {
        this.team = team;
    }

    /**
     * Returns whether this team info is assigned to a team.
     * @return whether this team info is assigned to a team.
     */
    public boolean isAssigned() {
        return team != null;
    }

    /**
     * Unassigns this team info from team.
     */
    public void unassign() {
        if (isAssigned()) {
            Preconditions.checkState(team.getGlobalInfo() != this, "Team info is global");
            team.infoSet.remove(this);
            TeamPacketUtil.removeTeam(players, this);
            team.getGlobalInfo().players.addAll(players);
            players.clear();
            team = null;
        }
    }

    /**
     * Returns the name of the team if this team info is assigned.
     *
     * @return the name of the team if this team info is assigned.
     */
    public String getName() {
        return team == null ? null : team.getName();
    }

    /**
     * Returns an unmodifiable set of entries.
     *
     * @return an unmodifiable set of entries.
     */
    public Set<String> getEntries() {
        return Collections.unmodifiableSet(entries);
    }

    /**
     * Adds an entry.
     *
     * @param entry to add.
     * @return whether the entry was added.
     */
    public boolean addEntry(String entry) {
        Preconditions.checkNotNull(entry, "Entry cannot be null");
        if (!entries.contains(entry)) {
            entries.add(entry);
            addEntries.add(entry);
            removeEntries.remove(entry);
            updateEntries();
            return true;
        }
        return false;
    }

    /**
     * Removes an entry.
     *
     * @param entry to remove.
     * @return whether the entry was removed.
     */
    public boolean removeEntry(String entry) {
        Preconditions.checkNotNull(entry, "Entry cannot be null");
        if (entries.contains(entry)) {
            entries.remove(entry);
            removeEntries.add(entry);
            addEntries.remove(entry);
            updateEntries();
            return true;
        }
        return false;
    }

    /**
     * Sets the display name.
     *
     * @param displayName new display name.
     */
    public void setDisplayName(String displayName) {
        Preconditions.checkNotNull(displayName, "Display name cannot be null");
        checkLength(displayName);
        if (!displayName.equals(this.displayName)) {
            this.displayName = displayName;
            updateTeam();
        }
    }

    /**
     * Sets the prefix.
     *
     * @param prefix new prefix.
     */
    public void setPrefix(String prefix) {
        Preconditions.checkNotNull(prefix, "Prefix cannot be null");
        checkLength(prefix);
        if (!prefix.equals(this.prefix)) {
            this.prefix = prefix;
            updateTeam();
        }
    }

    /**
     * Sets the suffix.
     *
     * @param suffix new suffix.
     */
    public void setSuffix(String suffix) {
        Preconditions.checkNotNull(suffix, "Suffix cannot be null");
        checkLength(suffix);
        if (!suffix.equals(this.suffix)) {
            this.suffix = suffix;
            updateTeam();
        }
    }

    /**
     * Sets whether friendly fire is allowed.
     *
     * @param allowFriendlyFire whether friendly fire is allowed.
     */
    public void setAllowFriendlyFire(boolean allowFriendlyFire) {
        if (this.allowFriendlyFire != allowFriendlyFire) {
            this.allowFriendlyFire = allowFriendlyFire;
            updateTeam();
        }
    }

    /**
     * Sets whether players can see friendly invisibles.
     *
     * @param canSeeFriendlyInvisibles whether players can see friendly invisibles.
     */
    public void setCanSeeFriendlyInvisibles(boolean canSeeFriendlyInvisibles) {
        if (this.canSeeFriendlyInvisibles != canSeeFriendlyInvisibles) {
            this.canSeeFriendlyInvisibles = canSeeFriendlyInvisibles;
            updateTeam();
        }
    }

    /**
     * Sets the name tag visibility.
     *
     * @param nameTagVisibility new name tag visibility.
     */
    public void setNameTagVisibility(NameTagVisibility nameTagVisibility) {
        Preconditions.checkNotNull(nameTagVisibility, "Name tag visibility cannot be null");
        if (this.nameTagVisibility != nameTagVisibility) {
            this.nameTagVisibility = nameTagVisibility;
            updateTeam();
        }
    }

    private void checkLength(String str) {
        Preconditions.checkState(str.length() <= 16, "Cannot be over 16 characters");
    }

    private void updateTeam() {
        if (!updateTeam && !players.isEmpty())
            updateTeam = true;
    }

    private void updateEntries() {
        if (!updateEntries)
            if (!players.isEmpty())
                updateEntries = true;
            else {
                addEntries.clear();
                removeEntries.clear();
            }
    }
}
