import java.util.Comparator;

class SortDocid implements Comparator<DocList> {
	public int compare (DocList term1, DocList term2)
	{
		return term1.Docid-term2.Docid;
	}
}
