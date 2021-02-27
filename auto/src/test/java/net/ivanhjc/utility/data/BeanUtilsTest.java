package net.ivanhjc.utility.data;

import net.ivanhjc.utility.auto.enums.RandomGenerators;
import net.ivanhjc.utility.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class BeanUtilsTest {
    private static final Logger LOG = LogManager.getLogger();

    @Test
    public void getNullProperties() {
        StringBuilder result = BeanUtils.getNullProperties("name,age,test", RandomGenerators.generate(User.class));
        LOG.info(result);
    }
}
