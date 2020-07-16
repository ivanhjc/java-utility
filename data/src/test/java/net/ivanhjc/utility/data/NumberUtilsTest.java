package net.ivanhjc.utility.data;

import org.junit.Test;
import java.math.BigDecimal;

/**
 * @author Ivan Huang on 2018/8/31 18:38.
 */
public class NumberUtilsTest {

    @Test
    public void formatDecimal() {
        System.out.println(NumberUtils.formatDecimal(new BigDecimal(100.00800), 4, false));
        System.out.println(NumberUtils.formatDecimal(100.008004, 5, true));
    }
}
