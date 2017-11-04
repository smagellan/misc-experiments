package my.test;

public class R {
    public static void main(String[] args) {
        String s = "qwerty";
        char chars[] = s.toCharArray();
        int start = 0;
        int end = chars.length - 1;
        while (start < end){
            char tmp = chars[start];
            chars[start] = chars[end];
            chars[end] = tmp;
            start++;
            end--;
        }
        System.err.println(new String(chars));
    }
}
