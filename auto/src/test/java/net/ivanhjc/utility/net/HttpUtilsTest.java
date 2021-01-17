package net.ivanhjc.utility.net;

import org.junit.Test;

public class HttpUtilsTest {

    private Object sample;

    public HttpUtilsTest() throws Exception {
//        sample = Coder.sample("/home/ivanhjc/Projects/java-utility/data/target/", "User");
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

    @Test
    public void test() {
        String s = HttpUtils.getInstance().postJSON("https://api-cd.tools.huawei.com/api/cd-cloud-pipeline/v2/pipelines/failReport", "{\"eventId\":\"503d0dba2b2d4f7e9535cf9c6318847d\",\"jobId\":\"888aaa409f5a41689c34bbfe9f0745e7\",\"pipelineId\":\"e6fbdc239ed443f289721c7d6237c8c6\",\"stageName\":\"Build\",\"jobName\":\"BuildApproval\",\"taskName\":\"CodeGate\",\"service\":\"Gate\",\"description\":\"Gate was not passed, please have a look at gate report for detail\",\"owner\":\"User\"}, result is: {\"status\":\"success\",\"message\":\"save or update fail report success.\"}");
        System.out.println(s);
    }
}
