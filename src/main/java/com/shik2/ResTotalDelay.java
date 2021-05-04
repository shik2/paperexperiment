package com.shik2;

/**
 * @author shik2
 * @date 2021/01/03
 * <p>
 * Description:
 **/
public class ResTotalDelay {
    private double lamda;
    private double paperTDelay;
    private double closeTDelay;
    private double gaTDelay;
    private double edfTDelay;
    private double minTDelay;
    private double desTDelay;


    public ResTotalDelay(double lamda) {
        this.lamda = lamda;
    }

    public double getPaperTDelay() {
        return paperTDelay;
    }

    public void setPaperTDelay(double paperTDelay) {
        this.paperTDelay = paperTDelay;
    }

    public double getCloseTDelay() {
        return closeTDelay;
    }

    public void setCloseTDelay(double closeTDelay) {
        this.closeTDelay = closeTDelay;
    }

    public double getGaTDelay() {
        return gaTDelay;
    }

    public void setGaTDelay(double gaTDelay) {
        this.gaTDelay = gaTDelay;
    }

    public double getEdfTDelay() {
        return edfTDelay;
    }

    public void setEdfTDelay(double edfTDelay) {
        this.edfTDelay = edfTDelay;
    }

    public double getMinTDelay() {
        return minTDelay;
    }

    public void setMinTDelay(double minTDelay) {
        this.minTDelay = minTDelay;
    }

    public double getDesTDelay() {
        return desTDelay;
    }

    public void setDesTDelay(double desTDelay) {
        this.desTDelay = desTDelay;
    }

    @Override
    public String toString() {
        return "ResTotalDelay{" +
                "lamda=" + lamda +
                ", paperTDelay=" + paperTDelay +
                ", closeTDelay=" + closeTDelay +
                ", gaTDelay=" + gaTDelay +
                ", edfTDelay=" + edfTDelay +
                ", minTDelay=" + minTDelay +
                ", desTDelay=" + desTDelay +
                '}';
    }
}
