package wbo;
/**
 *
 *
//COPYRIGHT//
 *
 */

import Jama.*;
import java.util.Random;

/**
 *
 * @author timn
 */
public class OrbitalHolder {
    private OrbitalHolder first = null; // has the same value in all items of the list
    ////private OrbitalHolder last = null;  // != null only for the first item in the list
    private OrbitalHolder next = null;  // points to the next item
    /**
     * Append a new element to the list or, if @param parent == null,
     * create the list
     * Usage example: 
     *   OrbitalHolder orbList = null;  // Create the list
     *   while (....) {
     *     orbList = new OrbitalHolder(orbList); // create a new item
     *   }
     */
    public OrbitalHolder(OrbitalHolder last) {
        if (last == null) {
            first = this;
            //last = this;
        } else {
            // duplicate the 
            this.first = last.first;
            // append this item to the list
            last.next = this; 
            // and update the pointer to the last item (stored in this.first)
            //first.last = this;
        }
    }
    //--------------------------------------------------------------------------
    /**
     * @returns the number of elements in the list
     */
    public int getLength() {
        OrbitalHolder item = this.first;
        // get the number of items:
        int count = 0;
        while (item != null) {
            count++;
            item = item.next;
        }        
        return count;
    }
    //--------------------------------------------------------------------------
    /**
     * Constructs the array from the list and @returns it
     */
    public double[][] asArray() {
        // allocate memory
        double[][] result = new double[ getLength() ][];
        // save the data
        OrbitalHolder item = this.first;
        int i = 0;
        while (item != null) {
            result[ i++ ] = item.data;
            item = item.next;
        }
        return result;
    }
    //--------------------------------------------------------------------------
    public double weight;
    public double[] data;
    public Object[] misc;
    public int[] parentAtoms;
    //--------------------------------------------------------------------------
    public void copyDataFrom(OrbitalHolder src) {
        
    }
    //--------------------------------------------------------------------------
    /**
     * A simple print-out method (mainly for debug purposes)
     */
    private void printWeights(String format) {
        OrbitalHolder item = this.first;
        int i = 0;
        while (item != null) {            
            System.out.printf(format, item.weight);
            item = item.next;
            i++;
        }
        System.out.printf("| %d item(s) totally %n", i);
    }
    //--------------------------------------------------------------------------
    /**
     * Merges lists @param firstA and @param firstB using sortAscendingly rule
     * and @returns a pointer to the first item in the obtained list.
     * Note: 
     */
    private static OrbitalHolder mergeLists(OrbitalHolder firstA, OrbitalHolder firstB, boolean sortAscendingly) {
        OrbitalHolder mergedList = null, result = null;
        OrbitalHolder itemA = firstA, itemB = firstB;
        // fool prof: firstA and firstB must point to the first items
        if (itemA != null) itemA = itemA.first;
        if (itemB != null) itemB = itemB.first;
        
        while ((itemA != null) || (itemB != null)) {
            
            if ((itemA != null) && // if listA is not empty, and 
                    ((itemB == null) || // either listB is empty,
                    ((itemB != null) && ((itemA.weight > itemB.weight) ^ (sortAscendingly))))
                    // or the element in the listB has lower priority than that in listA
                    ) {
                if (mergedList != null) {
                    mergedList.next = itemA; // append item to the list
                } else {
                    result = itemA; // save the first item in the list!
                }
                itemA.first = result; // update the 'first' field of newly added item
                mergedList = itemA;  // shift the 'current' last item towards itemA (or start the list if it was empty)
                itemA = itemA.next;  // move A pointer to the next item
                mergedList.next = null; // zero the 'next' pointer of the item which has been added. Note: this must 
                // be done AFTER the A pointer has been shifted!!!
            }
            if ((itemB != null) && // if listB is not empty, and 
                    ((itemA == null) || // either listA is empty,
                    ((itemA != null) && ! ((itemA.weight > itemB.weight) ^ (sortAscendingly))))
                    // or the element in the listA has lower priority than that in listB
                    ) {
                if (mergedList != null) {
                    mergedList.next = itemB;
                } else {
                    result = itemB; // save the first item in the list!
                }
                itemB.first = result; // update the 'first' field of newly added item
                mergedList = itemB;
                itemB = itemB.next;
                mergedList.next = null;
            }
        }
        return result;
    }
    //--------------------------------------------------------------------------
    private static OrbitalHolder mergeSort(OrbitalHolder first, boolean sortAscendingly) {
        if (first != null) first = first.first; // fool prof.
        
        OrbitalHolder item = first;
        OrbitalHolder pre_middle = null; // the element which preceeds 'middle', and
        OrbitalHolder middle = first; // 'middle' - begins with the first element...
        int i = 0;        
        // find the middle element
        while (item != null) {
            i++;            
            if (i % 2 == 0) {
                pre_middle = middle;
                middle = middle.next;  // ...and steps 1 element forward each 
                // time when a couple of new elements has been discovered
            }
            item = item.next;
        }
        // Ok! now - cut the first half of the list
        if (pre_middle == null) {
            return first; // the initial list contained 1 or 0 elements! -- nothing to do!
        }
        pre_middle.next = null; // actual 'cutting' -- correction of the last record in the first list
        middle.first = middle;  // and correction of the first item in the second list
        // sort each of the sub-lists...
        first = mergeSort(first, sortAscendingly); 
        middle = mergeSort(middle, sortAscendingly); 
        // ...and merge them:
        return mergeLists(first, middle, sortAscendingly); 
    }
    //--------------------------------------------------------------------------
    private static void test_mergeSort() {
        int NTests = 100000;
        int maxLen = 10;
        int NPassed = 0;
        int nFwd = 0;
        int nZeroLen = 0;
        Random r = new Random();
        for (int t=0; t<NTests; t++) {        
            boolean dir = ( r.nextDouble() > 0.5 );
            if (dir) nFwd ++;
            int len = (int)( r.nextInt(maxLen) );        
            if (len == 0) nZeroLen ++;
            OrbitalHolder list = null;
            for (int i=0; i<len; i++) {
                list = new OrbitalHolder(list);
                list.weight = r.nextDouble();
            }
            list = mergeSort(list, dir);
            // test the sorting:
            if (list != null)
                list = list.first;
            
            boolean ok = true;
            while (list != null) {
                if (list.next != null) {
                    ok &= (list.weight > list.next.weight) ^ dir;
                }
                list = list.next;
            }
            if (ok) 
                NPassed++;
        }
        System.out.printf("%d / %d (including fwd = %d and zero_len = %d) %n", NPassed, NTests, nFwd, nZeroLen);
    }
    //--------------------------------------------------------------------------
    // test:
    public static void main(String[] args) {
        test_mergeSort() ;
        
    }
    //--------------------------------------------------------------------------
    /**
     * 
     */
    public void sortByWeight() {
        
    }
    //--------------------------------------------------------------------------
    
    
    
    
    private Object[][] matr_list = new Object[2][]; 
    // [0] = first element, [1] = last element;
    // Each element: [0] = pointer to an object, [1] = pointer to the next element
    //--------------------------------------------------------------------------
    public void append_orbital(Matrix o) {
        Object[] item = new Object[2];
        item[0] = o;
        item[1] = null;
        if (matr_list[0] == null) {
            matr_list[0] = item;
            matr_list[1] = matr_list[0];            
        } else {
            matr_list[1] [1] = item;
            matr_list[1] = item;
        }        
    }
    //--------------------------------------------------------------------------
    
    
    
}
