package basketball;

public class Guard extends Player {
    private double slashSuccess = 0.9;

    public Guard(String name, int row, int col) {
        super(name, row, col);
        setPassSuccess(0.90);
        setLayupSuccess(0.85);
        setMidRangeSuccess(0.65);
        setLongRangeSuccess(0.45);
    }

    public double getSlashSuccess() {
        return slashSuccess;
    }

    public void setSlashSuccess(double v) {
        this.slashSuccess = v;
    }

    @Override
    public boolean isSpecialAbilityAvailable() {
        if (specialUsed || game == null) return false;
        return game.getBallHolder() == this;
    }

    @Override
    public void specialAbility() {
        if (!isSpecialAbilityAvailable()) {
            lastSpecialResult = SpecialResult.UNAVAILABLE;
            return;
        }
        specialUsed = true;
        Player matched = game.getMatchedDefender(this);
        if (matched == null) {
            lastSpecialResult = SpecialResult.FAIL;
            return;
        }
        boolean ok = game.getRng().nextDouble() < slashSuccess;
        if (!ok) {
            lastSpecialResult = SpecialResult.FAIL;
            return;
        }
        int nr = matched.getRow() + 1;
        int nc = matched.getCol();
        if (!game.inBounds(nr, nc) || game.isOccupied(nr, nc)) {
            lastSpecialResult = SpecialResult.FAIL;
            return;
        }
        setPosition(nr, nc);
        lastSpecialResult = SpecialResult.SUCCESS;
    }
}
