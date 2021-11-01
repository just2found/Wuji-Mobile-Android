package net.sdvn.common.internet.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Description:
 *
 * @author admin
 * CreateDate: 2021/4/6
 */
public class UtilsTest {

    @Test
    public void sha256() {
        System.out.println(Utils.genRandomNum(16));
        String source = "password=" +
                Utils.md5("111111") +
                "&random=" +
                "as214323534";
        System.out.println(source);
        String sha256 = Utils.sha256(source);
        System.out.println(sha256);
    }

    @Test
    public void md5() {
        String md5111111 = Utils.md5("111111");
        assertEquals("96e79218965eb72c92a549dd5a330112", md5111111);
    }

    @Test
    public void pwdconfirm() {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String randomNum = Utils.genRandomNum(16);
        String source = "password=" +
                Utils.md5("123456a.") +
                "&random=" + randomNum +
                "&time=" + timestamp;
        String sha256 = Utils.sha256(source);
        System.out.println("random: " + randomNum);
        System.out.println("timestamp: " + timestamp);
        System.out.println("signature : " + sha256);
    }
}