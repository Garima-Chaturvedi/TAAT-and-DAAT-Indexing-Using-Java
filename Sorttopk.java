import java.util.Comparator;

class Sorttopk implements Comparator<TopKL> {
	public int compare (TopKL term1, TopKL term2)
	{
		return term2.DocCount-term1.DocCount;
	}
}
