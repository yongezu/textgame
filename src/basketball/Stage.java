package basketball;

public class Stage {
    public final int maxPoints;
    public final double highWeight, mediumWeight, lowWeight;

    public Stage(int maxPoints, double high, double medium, double low) {
        this.maxPoints = maxPoints;
        this.highWeight = high;
        this.mediumWeight = medium;
        this.lowWeight = low;
    }
}
