package basketball;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    public static int OFFENSE = 0;
    public static int DEFENSE = 1;

    // Shot results
    public static int MADE = 2;
    public static int MADE_THREE = 3;
    public static int MISSED = 4;
    public static int REBOUNDED = 5;
    public static int NO_POSSESSION = 6;

    public static int GRID_SIZE = 7;
    public static int BASKET_ROW = 6;
    public static int BASKET_COL = 4;

    private Team offense, defense;
    private int focusedIndex;
    private int ballHolderIndex;
    private int possession;
    private int offenseScore, defenseScore;
    private int gameCounter;
    private Stage stage;
    private Random rng = new Random();

    public Game(Team offense, Team defense) {
        this.offense = offense;
        this.defense = defense;
        this.focusedIndex = 0;
        this.ballHolderIndex = 0;
        this.possession = OFFENSE;
    }

    public boolean move(char dir) {
        // direction is 'w', 'a', 's', 'd', corresponding to up, left, down, right
        int dr = 0, dc = 0;
        char c = Character.toLowerCase(dir);
        if (c == 'w') {
            dr = -1;
        } else if (c == 'a') {
            dc = -1;
        } else if (c == 's') {
            dr = 1;
        } else if (c == 'd') {
            dc = 1;
        } else {
            return false;
        }
        Player focusedPlayer = getFocusedPlayer();
        int nr = focusedPlayer.getRow() + dr;
        int nc = focusedPlayer.getCol() + dc;
        if (!inBounds(nr, nc)) return false;
        if (isOccupied(nr, nc)) return false;
        focusedPlayer.setPosition(nr, nc);
        updateDefenders();
        return true;
    }

    public boolean pass(int receiverIndex) {
        if (possession != OFFENSE) return false;
        if (receiverIndex < 0 || receiverIndex >= Team.SIZE) return false;
        if (receiverIndex == ballHolderIndex) return false;

        Player passer = offense.getPlayer(ballHolderIndex);
        boolean success = rng.nextDouble() < passer.getPassSuccess();
        if (success) {
            this.ballHolderIndex = receiverIndex;
            this.focusedIndex = receiverIndex;
        } else {
            defenseScore++;
            resetPlay();
        }
        return success;
    }

    public int getPlayerDistance(Player p1, Player p2) {
        return Math.abs(p1.getRow() - p2.getRow()) + Math.abs(p1.getCol() - p2.getCol());
    }

    public int getBasketDistance(Player p) {
        return Math.abs(p.getRow() - BASKET_ROW) + Math.abs(p.getCol() - BASKET_COL);
    }

    public double getShootingSuccess() {
        Player p = getBallHolder();
        int distance = BASKET_ROW - p.getRow();
        double prob = 1.0;
        if (distance < 2) {
            prob = p.getLayupSuccess();
        } else if (distance <= 4) {
            prob = p.getMidRangeSuccess();
        } else {
            prob = p.getLongRangeSuccess();
        }
        Player matchedDefender = getMatchedDefender(p);
        int dr = matchedDefender.getRow() - p.getRow();
        boolean isBlocked = matchedDefender.getCol() == p.getCol();
        if (dr == 1 && isBlocked) {
            prob *= 0.5;
        } else if (dr == 2 && isBlocked) {
            prob *= 0.75;
        } else {
            prob *= 1.0;
        }
        return prob;
    }

    public int shoot() {
        if (possession != OFFENSE) return NO_POSSESSION;
        Player shooter = getBallHolder();
        double prob = getShootingSuccess();

        boolean made = rng.nextDouble() < prob;
        if (made) {
            int distance = BASKET_ROW - shooter.getRow();
            if(distance >= 5) {
                offenseScore += 3;
            } else {
                offenseScore += 2;
            }
            defenseScore += 1;
            resetPlay();
            if(distance >= 5) {
                return MADE_THREE;
            } else {
                return MADE;
            }
        }
        // missed: see if Center can rebound
        for (int i = 0; i < Team.SIZE; i++) {
            Player p = offense.getPlayer(i);
            if (p instanceof Center) {
                Center c = (Center) p;
                if (c.tryRebound(this)) {
                    ballHolderIndex = i;
                    focusedIndex = i;
                    return REBOUNDED;
                }
            }
        }
        defenseScore += 1;
        resetPlay();
        return MISSED;
    }

    public int invokeSpecial() {
        Player p = getFocusedPlayer();
        p.clearLastSpecialResult();
        p.specialAbility(this);
        return p.getLastSpecialResult();
    }

    public void setFocusedIndex(int i) {
        if (i < 0 || i >= Team.SIZE) return;
        this.focusedIndex = i;
    }
    public int getFocusedIndex() {
        return focusedIndex;
    }
    public Player getFocusedPlayer() {
        return offense.getPlayer(focusedIndex);
    }
    public Player getBallHolder() {
        return offense.getPlayer(ballHolderIndex);
    }
    public int getBallHolderIndex() {
        return ballHolderIndex;
    }
    public int getPossession() {
        return possession;
    }

    public Team getOffense() {
        return offense;
    }
    public Team getDefense() {
        return defense;
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

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE;
    }

    public boolean isOccupied(int row, int col) {
        for (int i = 0; i < Team.SIZE; i++) {
            Player p = offense.getPlayer(i);
            if (p.getRow() == row && p.getCol() == col) return true;
        }
        for (int i = 0; i < Team.SIZE; i++) {
            Player p = defense.getPlayer(i);
            if (p.getRow() == row && p.getCol() == col) return true;
        }
        return false;
    }

    private boolean isOccupiedExcluding(int row, int col, Player exclude) {
        for (int i = 0; i < Team.SIZE; i++) {
            Player p = offense.getPlayer(i);
            if (p == exclude) continue;
            if (p.getRow() == row && p.getCol() == col) return true;
        }
        for (int i = 0; i < Team.SIZE; i++) {
            Player p = defense.getPlayer(i);
            if (p == exclude) continue;
            if (p.getRow() == row && p.getCol() == col) return true;
        }
        return false;
    }

    public Player getMatchedDefender(Player offender) {
        for (int i = 0; i < Team.SIZE; i++) {
            if (offense.getPlayer(i) == offender) return defense.getPlayer(i);
        }
        return null;
    }

    public double getNextRandomDouble() {
        return this.rng.nextDouble();
    }

    public void resetPlay() {
        Game init = (stage == null) ? createInitial() : createInitial(stage);
        for (int i = 0; i < Team.SIZE; i++) {
            Player o = offense.getPlayer(i);
            Player srcO = init.offense.getPlayer(i);
            o.setPosition(srcO.getRow(), srcO.getCol());
            o.reset();

            Player d = defense.getPlayer(i);
            Player srcD = init.defense.getPlayer(i);
            d.setPosition(srcD.getRow(), srcD.getCol());
            d.reset();
        }
        focusedIndex = 0;
        ballHolderIndex = 0;
        possession = OFFENSE;
        gameCounter++;
    }

    private void updateDefenders() {
        int i = focusedIndex;
        Player o = offense.getPlayer(i);
        Defender d = (Defender) defense.getPlayer(i);
        int ro = o.getRow();
        int co = o.getCol();
        int highRow, medRow;
        if (ro < 4) {
            highRow = ro + 2;
            medRow = ro + 1;
        } else {
            highRow = ro + 1;
            medRow = ro + 2;
        }

        List<int[]> cells = new ArrayList<int[]>();
        List<Double> weights = new ArrayList<Double>();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = ro + dr;
                int nc = co + dc;
                if (!inBounds(nr, nc)) continue;
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

    public Stage getStage() {
        return stage;
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public static Game createInitial() {
        return createInitial(new Stage(0, 80, 15, 5));
    }

    public static Game createInitial(Stage stage) {
        Guard p1 = new Guard();
        p1.setPlayerNumber(1);
        p1.setPosition(0, 1);

        Forward p2 = new Forward();
        p2.setPlayerNumber(2);
        p2.setPosition(2, 2);

        Center p3 = new Center();
        p3.setPlayerNumber(3);
        p3.setPosition(1, 4);

        Player[] offensePlayers = new Player[] { p1, p2, p3 };

        Player[] defensePlayers = new Player[Team.SIZE];
        for (int i = 0; i < Team.SIZE; i++) {
            Player o = offensePlayers[i];
            Defender d = new Defender(stage.highWeight, stage.mediumWeight, stage.lowWeight);
            d.setPlayerNumber(i + 1);
            d.setPosition(o.getRow() + 2, o.getCol());
            defensePlayers[i] = d;
        }

        Game game = new Game(
            new Team("players", offensePlayers),
            new Team("defenders", defensePlayers)
        );
        game.setStage(stage);
        return game;
    }
}
