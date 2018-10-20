package smagellan.test.generics;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;

public class Utils {
    private Utils(){}

    public static <K, D, S extends D> Map<K, D> listToMap(
            Iterable<? extends S> source,
            Function<S, ? extends K> keyMapper) {
        if (source == null) {
            return null;
        }
        return stream(source.spliterator(), false)
                .collect(toMap(keyMapper, v -> v));
    }

    public static void main(String[] args) {
        List<Child> children = Arrays.asList(new Child(-1L, 1L), new Child(-2L, 2L), new Child(-3L, 3L));
        List<Parent> parent = Arrays.asList(new Parent(-1L), new Parent(-2L), new Parent(-3L));

        Map<Long, Parent> childToParent = listToMap(children, Parent::getParentId);
        Map<Long, Parent> childToParent2 = listToMap(children, Child::getParentId);
        Map<Long, Parent> childToParent3 = listToMap(children, Child::getChildId);
        Map<Long, Parent> parentToParent = listToMap(parent, Parent::getParentId);
        System.err.println(String.format("%s, %s, %s, %s", childToParent, childToParent2, childToParent3, parentToParent));
    }
}


class Parent {
    private final long parentId;
    public Parent(long parentId){
        this.parentId = parentId;
    }

    public long getParentId() {
        return parentId;
    }
}

class Child extends Parent {
    private final long childId;

    public Child(long parentId, long childId){
        super(parentId);
        this.childId = childId;
    }

    public long getChildId() {
        return childId;
    }
}