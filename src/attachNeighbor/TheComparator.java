package attachNeighbor;
import java.util.*;

public class TheComparator implements Comparator {
    public int compare(Object a, Object b) {
    	
        Chpano apano = (Chpano) a;
        Chpano bpano = (Chpano) b;
        return Double.compare(apano.getDist(),bpano.getDist());
    }

}