package basketball;

public class Forward extends Player {
    private double cutSuccess = 0.9;

    public Forward(String name, int row, int col) {
        super(name, row, col);
    }

    public double getCutSuccess() {
        return cutSuccess;
    }

    public void setCutSuccess(double v) {
        this.cutSuccess = v;
    }

    @Override
    public boolean isSpecialAbilityAvailable() {
        if (specialUsed || game == null) return false;
        if (game.getBallHolder() == this) return false;
        return getRow() >= 4;
    }

    @Override
    public void specialAbility() {
        if (!isSpecialAbilityAvailable()) {
            lastSpecialResult = SpecialResult.UNAVAILABLE;
            return;
        }
        specialUsed = true;
        boolean ok = game.getRng().nextDouble() < cutSuccess;
        if (!ok) {
            lastSpecialResult = SpecialResult.FAIL;
            return;
        }
        int nr = 6;
        int nc = 4;
        if (!game.inBounds(nr, nc) || game.isOccupied(nr, nc)) {
            lastSpecialResult = SpecialResult.FAIL;
            return;
        }
        setPosition(nr, nc);
        lastSpecialResult = SpecialResult.SUCCESS;
    }
}
