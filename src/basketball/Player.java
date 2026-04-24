package basketball;

public class Player {
    private final String name;
    private int row;
    private int col;
    private double passSuccess = 0.8;
    private double layupSuccess = 0.75;
    private double midRangeSuccess = 0.5;
    private double longRangeSuccess = 0.3;

    public Player(String name, int row, int col) {
        this.name = name;
        this.row = row;
        this.col = col;
    }

    public String getName() {
        return name;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public double getPassSuccess() {
        return passSuccess;
    }

    public void setPassSuccess(double passSuccess) {
        this.passSuccess = passSuccess;
    }

    public double getLayupSuccess() {
        return layupSuccess;
    }

    public void setLayupSuccess(double v) {
        this.layupSuccess = v;
    }

    public double getMidRangeSuccess() {
        return midRangeSuccess;
    }

    public void setMidRangeSuccess(double v) {
        this.midRangeSuccess = v;
    }

    public double getLongRangeSuccess() {
        return longRangeSuccess;
    }

    public void setLongRangeSuccess(double v) {
        this.longRangeSuccess = v;
    }
}
