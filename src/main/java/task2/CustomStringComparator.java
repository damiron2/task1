package task2;

import java.util.Arrays;
import java.util.Comparator;

public class CustomStringComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        o1 = o1.trim();
        o2 = o2.trim();
        String[] s1 = o1.split("\\.");
        String[] s2 = o2.split("\\.");
        if (s1[1].compareTo(s2[1])==0){
            return Integer.parseInt(s1[0])-Integer.parseInt(s2[0]);
        } else{
            return s1[1].compareToIgnoreCase(s2[1]);
        }
    }
}
