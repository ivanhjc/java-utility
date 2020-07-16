package net.ivanhjc.utility.data;

/**
 * @author Ivan Huang on 2017/7/13
 */
public class MoneyUtils {

    private static final double PITT = 3500; //Personal income tax threshold 个人所得税起征点
    private static final double[] TAXABLE_INCOME_RANGE = new double[]{0, 1455, 4155, 7755, 27255, 41255, 57505}; //应纳税所得额范围
    private static final double[] TAX_RANGE = new double[]{0.0, 43.65, 310.5, 996.0, 5808.75, 9621.5, 14621.75}; //应纳税所得额范围

    /**
     * Returns the personal income tax giving an income
     */
    public static double getTax(double income) {
        double taxableIncome = income - PITT;
        if (taxableIncome <= 0)
            return 0;

        TaxGrade grade = getTaxGradeByIncome(income);
        assert grade != null;
        return taxableIncome * grade.taxRate - grade.quickDeduction;
    }

    /**
     * Returns the tax grade of an income
     */
    public static TaxGrade getTaxGradeByIncome(double income) {
        for (int i = TAXABLE_INCOME_RANGE.length - 1; i >= 0; i--) {
            if (income - PITT > TAXABLE_INCOME_RANGE[i])
                return TaxGrade.valueOf("GRADE" + (i + 1));
        }
        return null;
    }

    /**
     * Returns the corresponding income of a given tax
     */
    public static double getIncome(double tax) {
        TaxGrade grade = getTaxGradeByTax(tax);
        assert grade != null;
        return (tax + grade.quickDeduction) / grade.taxRate + PITT;
    }

    /**
     * Returns the tax grade of a tax
     */
    public static TaxGrade getTaxGradeByTax(double tax) {
        for (int i = TAX_RANGE.length - 1; i >= 0; i--) {
            if (tax > TAX_RANGE[i])
                return TaxGrade.valueOf("GRADE" + (i + 1));
        }
        return null;
    }

    /**
     * Returns the corresponding tax range of the taxable income range
     */
    public static double[] getTaxRange() {
        double[] taxRange = new double[TAXABLE_INCOME_RANGE.length];
        for (int i = 0; i < taxRange.length; i++) {
            taxRange[i] = getTax(TAXABLE_INCOME_RANGE[i] + PITT);
        }
        return taxRange;
    }

    private enum TaxGrade { //税率级数
        GRADE1(0.03, 0),
        GRADE2(0.10, 105),
        GRADE3(0.20, 555),
        GRADE4(0.25, 1005),
        GRADE5(0.30, 2755),
        GRADE6(0.35, 5505),
        GRADE7(0.45, 13505);

        public final double taxRate;
        public final double quickDeduction; //速算扣除数

        TaxGrade(double taxRate, double quickDeduction) {
            this.taxRate = taxRate;
            this.quickDeduction = quickDeduction;
        }

        public String toString() {
            return String.format("%s[%s, %s]", this.name(), taxRate, quickDeduction);
        }
    }

}
