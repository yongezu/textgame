package basketball;

public class Forward extends Player {
    private double cutSuccess = 0.30;

    public Forward() {
        this.passSuccess = 0.70;
        this.layupSuccess = 0.85;
        this.midRangeSuccess = 0.55;
        this.longRangeSuccess = 0.30;
    }

    @Override
    public void specialAbility(Game game) {
        if (!isSpecialAbilityAvailable(game)) {
            setLastSpecialResult(UNAVAILABLE);
            return;
        }
        setSpecialUsed(true);
        boolean ok = this.getRandomDouble() < cutSuccess;
        if (!ok) {
            setLastSpecialResult(FAIL);
            return;
        }
        // directly move to (6, 4)
        int nr = 6;
        int nc = 4;
        if (game.isOccupied(nr, nc)) {
            setLastSpecialResult(FAIL);
            return;
        }
        setPosition(nr, nc);
        setLastSpecialResult(SUCCESS);
    }

    @Override
    public boolean isSpecialAbilityAvailable(Game game) {
        if (isSpecialUsed()) {
            return false;
        }
        if (game.getBallHolder() == this) {
            return false;
        }
        // if the row < 4, return false
        if (getRow() < 4) {
            return false;
        }
        return true;
    }
}
