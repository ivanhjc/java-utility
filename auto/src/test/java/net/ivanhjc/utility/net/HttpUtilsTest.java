package net.ivanhjc.utility.net;

import net.ivanhjc.utility.auto.Coder;
import org.junit.Test;

public class HttpUtilsTest {

    private Object sample = Coder.sample("/home/ivanhjc/Projects/java-utility/data/target/", "User");

    public HttpUtilsTest() throws Exception {

    }

    @Test
    public void toURLParamsIncludeAll() throws Exception {
        System.out.println(HttpUtils.toURLParamsIncludeAll(sample));
    }

    @Test
    public void toURLParamsIncludeNonNull() throws Exception {
        System.out.println(HttpUtils.toURLParamsIncludeNonNull(sample));
    }

    @Test
    public void toURLParamsExcludeSeparatorsAndBlanks() throws Exception {
        System.out.println(HttpUtils.toURLParamsExcludeSeparatorsAndBlanks(sample, null));
    }
}
