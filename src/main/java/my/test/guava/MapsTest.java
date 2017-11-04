package my.test.guava;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

public class MapsTest {
    public static void main(String[] args) {
        Collection<Parent> collection = Arrays.asList(new Child(1L), new Child(2L));
        Map<Long, Parent> p = Maps.uniqueIndex(collection, Parent::getId);
    }
}

class Parent{
    private final long id;

    Parent(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}

class Child extends Parent {
    Child(long id) {
        super(id);
    }
}
