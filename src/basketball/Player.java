package basketball;

import java.util.Random;

public abstract class Player {

    // Results of the special ability
    public static int NONE = 0;
    public static int UNAVAILABLE = 1;
    public static int SUCCESS = 2;
    public static int FAIL = 3;

    // characteristics of the player
    private int row, col;
    protected double passSuccess;
    protected double layupSuccess;
    protected double midRangeSuccess;
    protected double longRangeSuccess;
    private boolean specialUsed;
    private int lastSpecialResult = NONE;
    private Random random;
    private int playerNumber;

    public Player() {
        this(0);
    }

    public Player(int playerNumber) {
        this.random = new Random();
        this.playerNumber = playerNumber;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }
    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public String archetype() {
        return getClass().getSimpleName();
    }

    public void setSpecialUsed(boolean specialUsed) {
        this.specialUsed = specialUsed;
    }
    public boolean isSpecialUsed() {
        return specialUsed;
    }
    public void setLastSpecialResult(int lastSpecialResult) {
        this.lastSpecialResult = lastSpecialResult;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }

    public double getPassSuccess() {
        return passSuccess;
    }
    public double getLayupSuccess() {
        return layupSuccess;
    }
    public double getMidRangeSuccess() {
        return midRangeSuccess;
    }
    public double getLongRangeSuccess() {
        return longRangeSuccess;
    }

    public void reset() {
        this.specialUsed = false;
        this.lastSpecialResult = NONE;
    }

    public int getLastSpecialResult() {
        return lastSpecialResult;
    }

    public void clearLastSpecialResult() {
        this.lastSpecialResult = NONE;
    }

    // use this random generator
    public double getRandomDouble() {
        return this.random.nextDouble();
    }

    public void specialAbility(Game game) {
        return;
    }
    public boolean isSpecialAbilityAvailable(Game game) {
        return false;
    }
}
