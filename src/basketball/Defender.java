package basketball;

public class Defender extends Player {
    private double highWeight;
    private double mediumWeight;
    private double lowWeight;

    public Defender() {
        this(80, 15, 5);
    }

    public Defender(double high, double medium, double low) {
        this.highWeight = high;
        this.mediumWeight = medium;
        this.lowWeight = low;
        this.passSuccess = 0.50;
        this.layupSuccess = 0.50;
        this.midRangeSuccess = 0.30;
        this.longRangeSuccess = 0.10;
    }

    @Override
    public void specialAbility(Game game) {
        return;
    }

    @Override
    public boolean isSpecialAbilityAvailable(Game game) {
        return false;
    }

    public double getHighWeight() {
        return highWeight;
    }
    public double getMediumWeight() {
        return mediumWeight;
    }
    public double getLowWeight() {
        return lowWeight;
    }
}
