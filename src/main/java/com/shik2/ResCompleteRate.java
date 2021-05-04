package com.shik2;

/**
 * @author shik2
 * @date 2020/12/25
 * <p>
 * Description:
 **/
public class ResCompleteRate {
    private double lamda;
    private double paperCRate;
    private double closeCRate;
    private double gaCRate;
    private double edfCRate;
    private double minCRate;
    private double desCRate;

    public ResCompleteRate(double lamda) {
        this.lamda = lamda;
    }

    public double getLamda() {
        return lamda;
    }

    public void setLamda(double lamda) {
        this.lamda = lamda;
    }

    public double getPaperCRate() {
        return paperCRate;
    }

    public void setPaperCRate(double paperCRate) {
        this.paperCRate = paperCRate;
    }

    public double getCloseCRate() {
        return closeCRate;
    }

    public void setCloseCRate(double closeCRate) {
        this.closeCRate = closeCRate;
    }

    public double getGaCRate() {
        return gaCRate;
    }

    public void setGaCRate(double gaCRate) {
        this.gaCRate = gaCRate;
    }

    public double getEdfCRate() {
        return edfCRate;
    }

    public void setEdfCRate(double edfCRate) {
        this.edfCRate = edfCRate;
    }

    public double getMinCRate() {
        return minCRate;
    }

    public void setMinCRate(double minCRate) {
        this.minCRate = minCRate;
    }

    public double getDesCRate() {
        return desCRate;
    }

    public void setDesCRate(double desCRate) {
        this.desCRate = desCRate;
    }

    @Override
    public String toString() {
        return "ResCompleteRate{" +
                "lamda=" + lamda +
                ", paperCRate=" + paperCRate +
                ", closeCRate=" + closeCRate +
                ", gaCRate=" + gaCRate +
                ", edfCRate=" + edfCRate +
                ", minCRate=" + minCRate +
                ", desCRate=" + desCRate +
                '}';
    }
}
