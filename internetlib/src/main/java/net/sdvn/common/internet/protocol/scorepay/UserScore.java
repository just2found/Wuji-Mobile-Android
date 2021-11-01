package net.sdvn.common.internet.protocol.scorepay;


import androidx.annotation.Keep;

import net.sdvn.common.internet.core.GsonBaseProtocol;

/**
 * Created by yun on 2018/1/17.
 */
@Keep
public class UserScore extends GsonBaseProtocol {

    /**
     * data : {"mbpoint":"12356","credit":750.4,"usable":true}
     */

    public DataBean data;

    @Keep
    public static class DataBean {

        public double mbpoint;
        public double credit;
        public boolean usable;
        public double notransferable_mbp;
        public double transferable_mbp;
        public double service_charge;
    }
}
