package net.sdvn.scorepaylib.pay.paypal;

public class PayPalResultInfo {
    public String sdvnorderno;
    public String paypalid;

    public PayPalResultInfo(String sdvnorderno, String paypalid) {
        this.sdvnorderno = sdvnorderno;
        this.paypalid = paypalid;
    }
}
