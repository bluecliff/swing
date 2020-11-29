import org.roaringbitmap.RoaringBitmap;

/**
 * @author lishuangjiang
 * @date 2020/11/23
 */
public class Test {
    public static void main(String[] args) {
        RoaringBitmap user1 = RoaringBitmap.bitmapOf(1, 3, 4, 650000);
        RoaringBitmap user2 = RoaringBitmap.bitmapOf(1, 3, 5, 5, 650000);
        RoaringBitmap user3 = new RoaringBitmap();
        user3.add(1L, 10000L);

        RoaringBitmap rr = RoaringBitmap.bitmapOf(1, 2, 3, 1000);
        RoaringBitmap rr2 = new RoaringBitmap();
        rr2.add(4000L, 4255L);
        rr.select(3); // would return the third value or 1000
        rr.rank(2); // would return the rank of 2, which is index 1
        rr.contains(1000); // will return true
        rr.contains(7); // will return false

        RoaringBitmap rror = RoaringBitmap.or(rr, rr2);// new bitmap
        rr.or(rr2); //in-place computation
        boolean equals = rror.equals(rr);// true
        if (!equals) throw new RuntimeException("bug");
        // number of values stored?
        long cardinality = rr.getLongCardinality();
        System.out.println(cardinality);
        // a "forEach" is faster than this loop, but a loop is possible:
        for (int i : rr) {
            System.out.println(i);
        }
    }
}

