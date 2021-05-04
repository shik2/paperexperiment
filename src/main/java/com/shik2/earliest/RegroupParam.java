package com.shik2.earliest;

import com.shik2.ga.MEC;

import java.util.Map;

/**
 * @author shik2
 * @date 2020/12/22
 * <p>
 * Description:
 **/
public class RegroupParam {
    private MEC mec;
    private Map<Task, MEC> transfer;

    public RegroupParam(MEC mec, Map<Task, MEC> transfer) {
        this.mec = mec;
        this.transfer = transfer;
    }

    public MEC getMec() {
        return mec;
    }

    public void setMec(MEC mec) {
        this.mec = mec;
    }

    public Map<Task, MEC> getTransfer() {
        return transfer;
    }

    public void setTransfer(Map<Task, MEC> transfer) {
        this.transfer = transfer;
    }
}
