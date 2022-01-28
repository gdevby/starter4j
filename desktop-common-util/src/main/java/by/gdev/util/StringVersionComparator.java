package by.gdev.util;

import java.util.Comparator;
/**
 * Compared string. Split with '.' and compared every number. 
 * 
 * @author Robert Makrytski
 *
 */
public class StringVersionComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        String versions1[] = o1.split("\\.");
        String versions2[] = o2.split("\\.");

        int length = Math.min(versions1.length, versions2.length);
        for (int i = 0; i < length; i++) {
            int res = Integer.valueOf(versions1[i]).compareTo(Integer.valueOf(versions2[i]));
            if (res != 0)
                return res;
        }
        if (versions1.length != versions2.length) {
            if (length == versions1.length)
                return -1;
            else return 1;
        }
        return 0;
    }
    
}
