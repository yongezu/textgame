package basketball;

public class Game {
    public static final int GRID_SIZE = 7;
    public static final int BASKET_ROW = GRID_SIZE;

    public enum Possession { OFFENSE, DEFENSE }
    public enum ShotResult { MADE, MISSED, NO_POSSESSION }

    private final Team offense;
    private final Team defense;
    private int focusedIndex = 0;
    private int ballHolderIndex = 0;
    private Possession possession = Possession.OFFENSE;
    private int offenseScore = 0;
    private int defenseScore = 0;
    private final java.util.Random rng = new java.util.Random();

    public Game(Team offense, Team defense) {
        this.offense = offense;
        this.defense = defense;
    }

    public Possession getPossession() {
        return possession;
    }

    public int getOffenseScore() {
        return offenseScore;
    }

    public int getDefenseScore() {
        return defenseScore;
    }

    public Team getOffense() {
        return offense;
    }

    public Team getDefense() {
        return defense;
    }

    public int getFocusedIndex() {
        return focusedIndex;
    }

    public Player getFocusedPlayer() {
        return offense.getPlayer(focusedIndex);
    }

    public void setFocusedIndex(int index) {
        if (index < 0 || index >= Team.SIZE) {
            throw new IllegalArgumentException("Invalid focus index: " + index);
        }
        this.focusedIndex = index;
    }

    public int getBallHolderIndex() {
        return ballHolderIndex;
    }

    public Player getBallHolder() {
        return offense.getPlayer(ballHolderIndex);
    }

    public void setBallHolderIndex(int index) {
        if (index < 0 || index >= Team.SIZE) {
            throw new IllegalArgumentException("Invalid ball holder index: " + index);
        }
        this.ballHolderIndex = index;
    }

    public boolean pass(int receiverIndex) {
        if (possession != Possession.OFFENSE) return false;
        if (receiverIndex < 0 || receiverIndex >= Team.SIZE) return false;
        if (receiverIndex == ballHolderIndex) return false;

        Player passer = offense.getPlayer(ballHolderIndex);
        boolean success = rng.nextDouble() < passer.getPassSuccess();
        if (success) {
            ballHolderIndex = receiverIndex;
        } else {
            defenseScore++;
            resetPlay();
        }
        return success;
    }

    private void resetPlay() {
        Game init = createInitial();
        for (int i = 0; i < Team.SIZE; i++) {
            Player src = init.offense.getPlayer(i);
            offense.getPlayer(i).setPosition(src.getRow(), src.getCol());
            Player sdef = init.defense.getPlayer(i);
            defense.getPlayer(i).setPosition(sdef.getRow(), sdef.getCol());
        }
        focusedIndex = 0;
        ballHolderIndex = 0;
        possession = Possession.OFFENSE;
    }

    public ShotResult shoot() {
        if (possession != Possession.OFFENSE) return ShotResult.NO_POSSESSION;
        Player shooter = offense.getPlayer(ballHolderIndex);
        int distance = BASKET_ROW - shooter.getRow();
        double prob;
        if (distance < 2) {
            prob = shooter.getLayupSuccess();
        } else if (distance <= 4) {
            prob = shooter.getMidRangeSuccess();
        } else {
            prob = shooter.getLongRangeSuccess();
        }
        boolean made = rng.nextDouble() < prob;
        if (made) offenseScore += 2;
        defenseScore += 1;
        resetPlay();
        return made ? ShotResult.MADE : ShotResult.MISSED;
    }

    public boolean move(char dir) {
        int dr = 0;
        int dc = 0;
        switch (Character.toUpperCase(dir)) {
            case 'W': dr = -1; break;
            case 'S': dr =  1; break;
            case 'A': dc = -1; break;
            case 'D': dc =  1; break;
            default: return false;
        }
        Player p = getFocusedPlayer();
        int nr = p.getRow() + dr;
        int nc = p.getCol() + dc;
        if (nr < 0 || nr >= GRID_SIZE || nc < 0 || nc >= GRID_SIZE) return false;
        if (isOccupied(nr, nc)) return false;
        p.setPosition(nr, nc);
        return true;
    }

    private boolean isOccupied(int row, int col) {
        for (Player p : offense.getPlayers()) {
            if (p.getRow() == row && p.getCol() == col) return true;
        }
        for (Player p : defense.getPlayers()) {
            if (p.getRow() == row && p.getCol() == col) return true;
        }
        return false;
    }

    public static Game createInitial() {
        Player[] offense = new Player[] {
            new Player("P1", 0, 0),
            new Player("P2", 2, 3),
            new Player("P3", 1, 5),
        };
        Player[] defense = new Player[] {
            new Player("D1", 6, 2),
            new Player("D2", 5, 1),
            new Player("D3", 4, 3),
        };
        return new Game(
            new Team("players", offense),
            new Team("defenders", defense)
        );
    }
}
