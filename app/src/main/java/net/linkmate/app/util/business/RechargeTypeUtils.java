package net.linkmate.app.util.business;

import net.linkmate.app.R;

import java.util.ArrayList;
import java.util.List;

public class RechargeTypeUtils {
    /**
     * 1:支付宝
     * 2:微信
     * 3:PayPal
     */
    private static int[] types = new int[]{1, 2, 3};
    private static int[] textIds = new int[]{R.string.alipay, R.string.wepay, R.string.paypal};
    private static int[] iconIds = new int[]{R.drawable.icon_logo_alipay, R.drawable.icon_logo_wepay, R.drawable.icon_logo_paypal};

    public static List<Integer> getTypes() {
        List<Integer> lists = new ArrayList<>();
        for (int type : types) {
            lists.add(type);
        }
        return lists;
    }

    public static int getTypeTextId(int type) {
        int id = textIds[0];
        for (int i = 0; i < types.length; i++) {
            if (types[i] == type) {
                id = textIds[i];
            }
        }
        return id;
    }

    public static int getTypeIconId(int type) {
        int id = iconIds[0];
        for (int i = 0; i < types.length; i++) {
            if (types[i] == type) {
                id = iconIds[i];
            }
        }
        return id;
    }
}
