# ScoreboardManager
A Scoreboard manager intended for use in my server.
## Requirements
Spigot 1.8.8 and ProtocolLib
## Installation
Get the jar by building the project with `mvn clean install` or just get it from releases and put it in your plugins folder.
## Examples
### TeamManager
```java
TeamManager manager = new TeamManager();

Team team = manager.getOrCreateTeam("test");

// A TeamInfo holds information about a team like the display name, prefix, suffix, entries etc.
// Each player can have a different TeamInfo.
// The global TeamInfo is the default one.

team.getGlobalInfo().setDisplayName("Test");
team.getGlobalInfo().setPrefix(ChatColor.GREEN + "Test ");
team.getGlobalInfo().setSuffix(ChatColor.RED + " test");
team.getGlobalInfo().addEntry(player.getName());

manager.addPlayer(player);

new BukkitRunnable() {
    @Override
    public void run() {
        // Make and set a different TeamInfo for a player
        TeamInfo teamInfo = team.setTeamInfo(player,
            new TeamInfoBuilder().displayName("Test2").prefix("Test2 ").suffix(" test2")
                .addEntry(player.getName()).build() // If you want, you can build a TeamInfo with a builder.
        );
    }
}.runTaskLater(this, 20*5);
```
### Sidebar
```java
Sidebar sidebar = new SidebarImpl();

sidebar.setTitle("Test");
for (int i = 0; i < 15; i++)
    sidebar.setLine(i, "Line " + i);
        
sidebar.addPlayer(player);
sidebar.setVisible(true);
```