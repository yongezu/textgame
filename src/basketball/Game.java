package basketball;

public class Game {
    public static final int GRID_SIZE = 7;
    public static final int BASKET_ROW = GRID_SIZE;

    public enum Possession { OFFENSE, DEFENSE }
    public enum ShotResult { MADE, MADE_THREE, MISSED, REBOUNDED, NO_POSSESSION }

    private final Team offense;
    private final Team defense;
    private int focusedIndex = 0;
    private int ballHolderIndex = 0;
    private Possession possession = Possession.OFFENSE;
    private int offenseScore = 0;
    private int defenseScore = 0;
    private int gameCounter = 0;
    private final java.util.Random rng = new java.util.Random();

    public Game(Team offense, Team defense) {
        this.offense = offense;
        this.defense = defense;
        for (Player p : offense.getPlayers()) p.setGame(this);
        for (Player p : defense.getPlayers()) p.setGame(this);
    }

    public java.util.Random getRng() {
        return rng;
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE;
    }

    public Player getMatchedDefender(Player offensePlayer) {
        for (int i = 0; i < Team.SIZE; i++) {
            if (offense.getPlayer(i) == offensePlayer) return defense.getPlayer(i);
        }
        return null;
    }

    public Player.SpecialResult invokeSpecial() {
        Player p = getFocusedPlayer();
        p.clearLastSpecialResult();
        p.specialAbility();
        return p.getLastSpecialResult();
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

    public int getGameCounter() {
        return gameCounter;
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
            Player o = offense.getPlayer(i);
            o.setPosition(src.getRow(), src.getCol());
            Player sdef = init.defense.getPlayer(i);
            defense.getPlayer(i).setPosition(sdef.getRow(), sdef.getCol());
        }
        focusedIndex = 0;
        ballHolderIndex = 0;
        possession = Possession.OFFENSE;
        gameCounter++;
    }

    public ShotResult shoot() {
        if (possession != Possession.OFFENSE) return ShotResult.NO_POSSESSION;
        Player shooter = offense.getPlayer(ballHolderIndex);
        int distance = BASKET_ROW - shooter.getRow();
        double prob;
        boolean longRange = false;
        if (distance < 2) {
            prob = shooter.getLayupSuccess();
        } else if (distance <= 4) {
            prob = shooter.getMidRangeSuccess();
        } else {
            prob = shooter.getLongRangeSuccess();
            longRange = true;
        }
        Player matchedDefender = defense.getPlayer(ballHolderIndex);
        int dr = matchedDefender.getRow() - shooter.getRow();
        int dc = matchedDefender.getCol() - shooter.getCol();
        int defDist = Math.max(Math.abs(dr), Math.abs(dc));
        double openness;
        if (defDist <= 1) openness = 0.5;
        else if (defDist == 2) openness = 0.75;
        else openness = 1.0;
        prob *= openness;
        boolean made = rng.nextDouble() < prob;
        if (made) {
            offenseScore += longRange ? 3 : 2;
            defenseScore += 1;
            resetPlay();
            return longRange ? ShotResult.MADE_THREE : ShotResult.MADE;
        }
        boolean rebounded = false;
        for (Player p : offense.getPlayers()) {
            if (p instanceof Center) {
                Center c = (Center) p;
                if (c.tryRebound()) {
                    rebounded = true;
                    ballHolderIndex = indexOfOffense(c);
                    focusedIndex = ballHolderIndex;
                    break;
                }
            }
        }
        if (rebounded) return ShotResult.REBOUNDED;
        defenseScore += 1;
        resetPlay();
        return ShotResult.MISSED;
    }

    private int indexOfOffense(Player p) {
        for (int i = 0; i < Team.SIZE; i++) {
            if (offense.getPlayer(i) == p) return i;
        }
        return -1;
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
        updateDefenders();
        return true;
    }

    private boolean isOccupiedExcluding(int row, int col, Player exclude) {
        for (Player p : offense.getPlayers()) {
            if (p == exclude) continue;
            if (p.getRow() == row && p.getCol() == col) return true;
        }
        for (Player p : defense.getPlayers()) {
            if (p == exclude) continue;
            if (p.getRow() == row && p.getCol() == col) return true;
        }
        return false;
    }

    private void updateDefenders() {
        int i = focusedIndex;
        {
            Player o = offense.getPlayer(i);
            Defender d = (Defender) defense.getPlayer(i);
            int ro = o.getRow();
            int co = o.getCol();
            int highRow;
            int medRow;
            if (ro < 4) {
                highRow = ro + 2;
                medRow = ro + 1;
            } else {
                highRow = ro + 1;
                medRow = ro + 2;
            }

            java.util.List<int[]> cells = new java.util.ArrayList<>();
            java.util.List<Double> weights = new java.util.ArrayList<>();
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = ro + dr;
                    int nc = co + dc;
                    if (nr < 0 || nr >= GRID_SIZE || nc < 0 || nc >= GRID_SIZE) continue;
                    if (isOccupiedExcluding(nr, nc, d)) continue;
                    double w;
                    if (nr == highRow && nc == co) w = d.getHighWeight();
                    else if (nr == medRow && nc == co) w = d.getMediumWeight();
                    else w = d.getLowWeight();
                    cells.add(new int[] { nr, nc });
                    weights.add(w);
                }
            }
            if (cells.isEmpty()) return;
            double total = 0;
            for (double w : weights) total += w;
            double pick = rng.nextDouble() * total;
            double acc = 0;
            for (int k = 0; k < cells.size(); k++) {
                acc += weights.get(k);
                if (pick < acc) {
                    d.setPosition(cells.get(k)[0], cells.get(k)[1]);
                    break;
                }
            }
        }
    }

    public boolean isOccupied(int row, int col) {
        for (Player p : offense.getPlayers()) {
            if (p.getRow() == row && p.getCol() == col) return true;
        }
        for (Player p : defense.getPlayers()) {
            if (p.getRow() == row && p.getCol() == col) return true;
        }
        return false;
    }

    public static Game createInitial() {
        return createInitial(new Stage(0, 80, 15, 5));
    }

    public static Game createInitial(Stage stage) {
        Player[] offense = new Player[] {
            new Guard("P1", 0, 1),
            new Forward("P2", 2, 2),
            new Center("P3", 1, 4),
        };
        Player[] defense = new Player[Team.SIZE];
        for (int i = 0; i < Team.SIZE; i++) {
            Player p = offense[i];
            defense[i] = new Defender("D" + (i + 1), p.getRow() + 2, p.getCol(),
                stage.highWeight, stage.mediumWeight, stage.lowWeight);
        }
        return new Game(
            new Team("players", offense),
            new Team("defenders", defense)
        );
    }
}
