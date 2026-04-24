package basketball;

public class Team {
    public static final int SIZE = 3;

    private final String name;
    private final Player[] players;

    public Team(String name, Player[] players) {
        if (players.length != SIZE) {
            throw new IllegalArgumentException("Team must have exactly " + SIZE + " players");
        }
        this.name = name;
        this.players = players;
    }

    public String getName() {
        return name;
    }

    public Player[] getPlayers() {
        return players;
    }

    public Player getPlayer(int index) {
        return players[index];
    }
}
