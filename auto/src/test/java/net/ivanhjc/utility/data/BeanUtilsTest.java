package net.ivanhjc.utility.data;

import com.google.gson.*;
import net.ivanhjc.utility.auto.Coder;
import org.junit.Test;

import java.util.Date;

public class BeanUtilsTest {

    @Test
    public void getNullProperties() throws Exception {
        Object sample = Coder.sample("/home/ivanhjc/Projects/customer-service-system/customer-service-system-common/target/classes", "com.winsky.lease.css.common.model.vo.TicketCsrFollowupSaveRequest");
        StringBuilder result = BeanUtils.getNullProperties("test,tid,adjustOrder[name,orderId]", sample);
        System.out.println(result);
    }

    @Test
    public void test() throws Exception {
        Object sample = Coder.sample("/home/ivanhjc/Projects/java-utility/data/target/test-classes", "net.ivanhjc.utility.data.model.Person");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong()))
                .registerTypeAdapter(Date.class, (JsonSerializer<Date>) (date, type, jsonSerializationContext) -> new JsonPrimitive(date.getTime()))
                .create();
        String json = gson.toJson(sample);
        System.out.println("sample1: \n" + json);
        Object sample2 = gson.fromJson(json, sample.getClass());
        System.out.println("sample2: \n" + gson.toJson(sample2));
    }
}
