package net.ivanhjc.utility.auto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.ivanhjc.utility.auto.enums.RandomGenerators;
import net.ivanhjc.utility.db.CarInfo;
import net.ivanhjc.utility.model.bean.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.net.MalformedURLException;

public class RandomGeneratorTest {
    private static final Logger LOG = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    @Test
    public void generate() {
        LOG.info("\n{}", GSON.toJson(RandomGenerators.generate(CarInfo.class)));
    }

    @Test
    public void generateFromClassFile() throws MalformedURLException, ClassNotFoundException {
        LOG.info("\n{}", GSON.toJson(RandomGenerators.generate("D:/projects/java-utility/auto/target/test-classes",
                "net.ivanhjc.utility.model.bean.Person")));
    }

    @Test
    public void generateList() {
        LOG.info("\n{}", GSON.toJson(RandomGenerators.generate(Person.class, 5)));
    }
}
