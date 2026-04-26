package basketball;

public abstract class Player {
    public enum SpecialResult { NONE, UNAVAILABLE, SUCCESS, FAIL }

    private final String name;
    private int row;
    private int col;
    private double passSuccess = 0.8;
    private double layupSuccess = 0.75;
    private double midRangeSuccess = 0.5;
    private double longRangeSuccess = 0.3;

    protected boolean specialUsed = false;
    protected SpecialResult lastSpecialResult = SpecialResult.NONE;
    protected Game game;

    public Player(String name, int row, int col) {
        this.name = name;
        this.row = row;
        this.col = col;
    }

    public String archetype() {
        return getClass().getSimpleName();
    }

    public abstract void specialAbility();
    public abstract boolean isSpecialAbilityAvailable();

    public void onGameReset() {
        specialUsed = false;
        lastSpecialResult = SpecialResult.NONE;
    }

    public boolean isSpecialUsed() {
        return specialUsed;
    }

    public SpecialResult getLastSpecialResult() {
        return lastSpecialResult;
    }

    public void clearLastSpecialResult() {
        lastSpecialResult = SpecialResult.NONE;
    }

    public void setGame(Game g) {
        this.game = g;
    }

    public String getName() { return name; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public void setPosition(int row, int col) { this.row = row; this.col = col; }

    public double getPassSuccess() { return passSuccess; }
    public void setPassSuccess(double v) { this.passSuccess = v; }
    public double getLayupSuccess() { return layupSuccess; }
    public void setLayupSuccess(double v) { this.layupSuccess = v; }
    public double getMidRangeSuccess() { return midRangeSuccess; }
    public void setMidRangeSuccess(double v) { this.midRangeSuccess = v; }
    public double getLongRangeSuccess() { return longRangeSuccess; }
    public void setLongRangeSuccess(double v) { this.longRangeSuccess = v; }
}
