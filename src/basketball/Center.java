package basketball;

public class Center extends Player {
    private double reboundSuccess = 0.40;

    public Center() {
        this.passSuccess = 0.60;
        this.layupSuccess = 0.80;
        this.midRangeSuccess = 0.40;
        this.longRangeSuccess = 0.10;
    }

    public double getReboundSuccess() {
        return reboundSuccess;
    }
    public void setReboundSuccess(double v) {
        this.reboundSuccess = v;
    }

    public boolean tryRebound(Game game) {
        if (!isSpecialAbilityAvailable(game)) return false;
        setSpecialUsed(true);
        boolean ok = game.getNextRandomDouble() < reboundSuccess;
        setLastSpecialResult(ok ? SUCCESS : FAIL);
        return ok;
    }

    @Override
    public void specialAbility(Game game) {
        // Center's rebound is applied automatically by the game on a missed shot.
        setLastSpecialResult(UNAVAILABLE);
    }

    @Override
    public boolean isSpecialAbilityAvailable(Game game) {
        if (isSpecialUsed()) return false;
        return getRow() == 6;
    }
}
