package basketball;

public class Stage {
    public final int maxPoints;
    public final double highWeight;
    public final double mediumWeight;
    public final double lowWeight;

    public Stage(int maxPoints, double highWeight, double mediumWeight, double lowWeight) {
        this.maxPoints = maxPoints;
        this.highWeight = highWeight;
        this.mediumWeight = mediumWeight;
        this.lowWeight = lowWeight;
    }
}
