package basketball;

public class Center extends Player {
    private double reboundSuccess = 0.99;

    public Center(String name, int row, int col) {
        super(name, row, col);
    }

    public double getReboundSuccess() {
        return reboundSuccess;
    }

    public void setReboundSuccess(double v) {
        this.reboundSuccess = v;
    }

    @Override
    public boolean isSpecialAbilityAvailable() {
        if (specialUsed) return false;
        return getRow() == 6;
    }

    @Override
    public void specialAbility() {
        // Center's rebound is applied automatically by the game on a missed shot.
        lastSpecialResult = SpecialResult.UNAVAILABLE;
    }

    public boolean tryRebound() {
        if (!isSpecialAbilityAvailable()) return false;
        specialUsed = true;
        boolean ok = game.getRng().nextDouble() < reboundSuccess;
        lastSpecialResult = ok ? SpecialResult.SUCCESS : SpecialResult.FAIL;
        return ok;
    }
}
