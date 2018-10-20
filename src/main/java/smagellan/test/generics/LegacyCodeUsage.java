package smagellan.test.generics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LegacyCodeUsage {
    public static void main(String[] args) {
        testListAcceptsAnything2();
    }

    private static void testListAcceptsAnything() {
        List<Date> lst = new ArrayList<>();
        lst.add(new Date());
        lst = new LegacyObject().process(lst);
        for (Object listElement : lst) {
            System.err.println("class:" + listElement.getClass() + "; val:" + listElement);
        }
    }

    private static void testListAcceptsAnything2() {
        List<Date> lst = new ArrayList<Date>(){
            @Override
            public boolean add(Date d){
                return super.add(d);
            }
        };
        lst.add(new Date());
        lst = new LegacyObject().process(lst);
        for (Object listElement : lst) {
            System.err.println("class:" + listElement.getClass() + "; val:" + listElement);
        }
    }
}

class LegacyObject {
    public List process(List l) {
        l.add("hello");
        return l;
    }
}
