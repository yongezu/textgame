package basketball;

public class Team {
    public static int SIZE = 3;
    private String name;
    private Player[] players;

    public Team(String name, Player[] players) {
        this.name = name;
        this.players = players;
    }

    public String getName() { return name; }
    public Player getPlayer(int i) { return players[i]; }
    public Player[] getPlayers() { return players; }
}
