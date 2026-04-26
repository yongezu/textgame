package basketball;

public class Defender extends Player {
    private double highWeight = 80.;
    private double mediumWeight = 15.;
    private double lowWeight = 5.;

    public Defender(String name, int row, int col) {
        super(name, row, col);
    }

    public Defender(String name, int row, int col,
                    double highWeight, double mediumWeight, double lowWeight) {
        super(name, row, col);
        this.highWeight = highWeight;
        this.mediumWeight = mediumWeight;
        this.lowWeight = lowWeight;
    }

    public double getHighWeight() { return highWeight; }
    public void setHighWeight(double v) { this.highWeight = v; }
    public double getMediumWeight() { return mediumWeight; }
    public void setMediumWeight(double v) { this.mediumWeight = v; }
    public double getLowWeight() { return lowWeight; }
    public void setLowWeight(double v) { this.lowWeight = v; }

    @Override
    public void specialAbility() {
        // Defenders are computer-controlled; no user-invoked ability.
    }

    @Override
    public boolean isSpecialAbilityAvailable() {
        return false;
    }
}
