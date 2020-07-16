package net.ivanhjc.utility.data;

import org.junit.Test;

import java.util.Arrays;

/**
 * @author Ivan Huang on 5/12/2018.
 */
public class MoneyUtilsTest {

    @Test
    public void getTax() {
        double income = 12759.16;
        System.out.println(MoneyUtils.getTax(income));
        System.out.println(MoneyUtils.getTaxGradeByIncome(income));
    }

    @Test
    public void getIncome() {
        double tax = 1309.79;
        System.out.println(MoneyUtils.getIncome(tax));
        System.out.println(MoneyUtils.getTaxGradeByTax(tax));
    }

    @Test
    public void getTaxRange() {
        System.out.println(Arrays.toString(MoneyUtils.getTaxRange()));
    }


}
