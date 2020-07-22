package net.ivanhjc.utility.data;

import net.ivanhjc.utility.auto.Coder;
import org.junit.Test;

public class BeanUtilsTest {
    @Test
    public void getNullProperties() throws Exception {
        Object sample = Coder.sample("/home/ivanhjc/Projects/customer-service-system/customer-service-system-common/target/classes", "com.winsky.lease.css.common.model.vo.TicketCsrFollowupSaveRequest");
        StringBuilder result = BeanUtils.getNullProperties("test,tid,adjustOrder[name,orderId]", sample);
        System.out.println(result);
    }
}
