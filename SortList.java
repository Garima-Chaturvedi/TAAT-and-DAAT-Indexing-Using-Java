import java.util.*;

class SortList implements Comparator<EachTermList> {
	public int compare (EachTermList term1, EachTermList term2)
	{
		return term2.freq-term1.freq;
	}
}
