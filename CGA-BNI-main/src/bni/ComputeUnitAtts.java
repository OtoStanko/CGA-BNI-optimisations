package bni;

import bni.comp.NetInfo;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import mod.jmut.core.Calc;

/**
 * @author Oto Stanko
 */
public class ComputeUnitAtts implements Runnable {

    NetInfo net;
    boolean[][] states;
    Calc cal;
    ArrayList<mod.jmut.core.comp.Attractor> atts;

    private CountDownLatch latch;

    public ComputeUnitAtts(NetInfo net, boolean[][] states, Calc cal) {
        this.net = net;
        this.states = states;
        this.cal = cal;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void run() {
        //synchronized(ComputeUnit.class) {
        atts = cal.findAttractors_v2(net.netD, states, false);
        //}

        latch.countDown();
    }

}

