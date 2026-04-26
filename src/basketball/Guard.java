package basketball;

public class Guard extends Player {
    private double slashSuccess = 0.30;

    public Guard() {
        this.passSuccess = 0.85; // Boosted
        this.layupSuccess = 1.0;
        this.midRangeSuccess = 0.50;
        this.longRangeSuccess = 0.40;
    }

    @Override
    public void specialAbility(Game game) {
        if (!isSpecialAbilityAvailable(game)) {
            setLastSpecialResult(UNAVAILABLE);
            return;
        }
        setSpecialUsed(true);
        if (getRandomDouble() < slashSuccess) {
            Player defender = game.getMatchedDefender(this);
            int targetRow = defender.getRow() + 1;
            int targetCol = defender.getCol();

            if (game.inBounds(targetRow, targetCol) && !game.isOccupied(targetRow, targetCol)) {
                setPosition(targetRow, targetCol);
                setLastSpecialResult(SUCCESS);
            } else {
                setLastSpecialResult(FAIL);
            }
        } else {
            setLastSpecialResult(FAIL);
        }
    }

    @Override
    public boolean isSpecialAbilityAvailable(Game game) {
        if (isSpecialUsed()) {
            return false;
        }
        if (game.getBallHolder() != this) {
            return false;
        }
        return true;
    }
}
