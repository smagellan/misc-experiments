package smagellan.test;

public class CompareTest {
    public static void main(String[] args) {
        String str1 = "Ich heiße Jonas";
        String str2 = "ich HEIẞE jonas";
        //prints true
        System.out.println(str1.equalsIgnoreCase(str2));
        //prints false
        System.out.println(str1.toUpperCase().equals(str2.toUpperCase()));
    }
}
