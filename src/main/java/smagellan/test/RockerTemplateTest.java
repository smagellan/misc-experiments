package smagellan.test;

import com.fizzed.rocker.Rocker;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class RockerTemplateTest {
    public static void main(String[] args) {
        //RockerRuntime.getInstance().setReloading(true);
        Map<String, Object> map = new HashMap<>();
        map.put("foo", "badger");
        map.put("bar", "fox");
        map.put("context", new JsonObject().put("path", "/TestRockerTemplate2.rocker.html"));

        Rocker.template("TestRockerTemplate2.rocker.html")
                .bind(map)
                .render();
    }
}
