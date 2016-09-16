import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList; 
import java.util.Iterator;
import java.util.Map;
import java.math.*;

public class CSE535Assignment {

	public static void main(String[] args) throws FileNotFoundException 	{
		
		/*NAME: Garima Chaturvedi
		UBITNAME: garimapr
		UBID: 50169189*/
		
		/*Defining Hashmaps for two Indexes*/
		HashMap<String, LinkedList<EachTermList>> hashMap_bydocid = new HashMap<String, LinkedList<EachTermList>>();
        HashMap<String, LinkedList<EachTermList>> hashMap_byfreq = new HashMap<String, LinkedList<EachTermList>>();
        
        /*Defining LinkedList to save terms and posting list size for TopK function*/
        LinkedList<TopKL> topk= new LinkedList<TopKL>();
        
        /*Taking term file from arguments*/
        String indexfile = args[0];
        String line = null;
        
        /*Taking output file from arguments*/
        FileOutputStream f = new FileOutputStream(args[1]);
        System.setOut(new PrintStream(f,true));
        
               
        try {
            FileReader fileReader = new FileReader(indexfile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) 
            {    
            	/*Parsing term file and taking terms, posting list size and [document no., frequency]*/
            	String[] element = line.split("\\\\");

            	String[] docnum = element[1].split("[c\\s+]+");
            	int ds=Integer.parseInt(docnum[1]);
            	
            	String[] docid_freq = element[2].split("[m\\[,\\]\\s]+");
            	
            	/*Defining two Linked Lists to save posting lists for each term*/
            	LinkedList<EachTermList> Eachterm_bydocid = new LinkedList<EachTermList>();
            	LinkedList<EachTermList> Eachterm_byfreq = new LinkedList<EachTermList>();
            	
            	/*Adding data into Linked List created for top k*/
            	TopKL terms= new TopKL(element[0], ds);
            	topk.add(terms);
            	
            	for (int i=1; i<docid_freq.length; i++)
            	{
            		/*Parsing [doc. no, freq] and adding in posting list*/
                	String[] eacht = docid_freq[i].split("[/\\s]+");
                	
                	int eacht1=Integer.parseInt(eacht[0]);
                	int eacht2=Integer.parseInt(eacht[1]);
                	
                	EachTermList group1 = new EachTermList(eacht1,eacht2);
                	Eachterm_bydocid.add(group1);
                	Eachterm_byfreq.add(group1);
                }
            
            /*adding posting list to hash map (index)*/
            hashMap_bydocid.put(element[0], Eachterm_bydocid );
            hashMap_byfreq.put(element[0], Eachterm_byfreq );
            
            /*Sorting the 2nd index on frequency*/
            Collections.sort(Eachterm_byfreq, new SortList());
                       
            }
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                indexfile + "'");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + indexfile + "'");                  
        }       		
        
        /*parsing the k argument value for top k*/
        int k= Integer.parseInt(args[2]);
        
        /*calling top k fucntion*/
        getTopK (topk, k);
        
        /*parsing the query_file*/
        String inputfile = args[3];
        String query = null;
        
        try {
            FileReader fileReader = new FileReader(inputfile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((query = bufferedReader.readLine()) != null) 
            {	
            	/*Taking each line of the query file and parsing each term*/
            	String[] Nterms = query.split("\\s");	
            
            	/*Calling the remaining functions for each set of query terms*/
            	getPostings(hashMap_bydocid, hashMap_byfreq, Nterms);
            	termAtATimeQueryAnd (hashMap_byfreq, Nterms);
            	termAtATimeQueryOr (hashMap_byfreq, Nterms);
            	docAtATimeQueryAnd (hashMap_bydocid, Nterms);
            	docAtATimeQueryOr (hashMap_bydocid, Nterms);
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                inputfile + "'");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + inputfile + "'");  
        }
	}
	

	public static void getTopK(LinkedList<TopKL> topk, int k) 
		{
		
		/*Taking top k values from the topk linked list and displaying*/
		System.out.print("FUNCTION: getTopK "+k+"\nResult: ");
		Collections.sort(topk, new Sorttopk());
			for (int i=0; i<k; i++)
			{
				System.out.print(topk.get(i).terms);
				if (i<k-1)
	    			{
					System.out.print(", ");
	    			}
			}
		}

	public static void getPostings(HashMap<String, LinkedList<EachTermList>> hashMap_bydocid, 
				HashMap<String, LinkedList<EachTermList>> hashMap_byfreq, String[] terms)
	{
		for (int i=0; i<terms.length; i++)
		{	
			/*Checking if term does not exist in Hash map*/
			if (!hashMap_bydocid.containsKey(terms[i]))
				{
				System.out.print("\nFUNCTION:  getPostings "+ terms[i]);
				System.out.print("\nTerm not found.");
				}
			
			/*If term exists then retrieving the doc IDs for each term*/
			else
			{
			LinkedList<EachTermList> Index1= hashMap_bydocid.get(terms[i]);
			LinkedList<EachTermList> Index2= hashMap_byfreq.get(terms[i]);
			
			/*Retrieving term from Doc-at-a-time Index*/
			System.out.print("\nFUNCTION:  getPostings "+ terms[i]+ "\nOrdered by doc IDs: ");
	        for(int l=0; l<Index1.size(); l++)
	        {
	    		System.out.print(Index1.get(l).Docid);
	    		if (l<Index1.size()-1)
	    			System.out.print(", ");
	    	}
	        
	        /*Retrieving term from Term-at-a-time Index*/
	        System.out.print("\nOrdered by TF: ");
	        for(int m=0; m<Index2.size(); m++)
	        {
	    		System.out.print(Index2.get(m).Docid);
	    		if (m<Index2.size()-1)
	    			System.out.print(", ");
	    		else
	    			continue;
	    	}
			}
		}
	}
	
	public static void termAtATimeQueryAnd (HashMap<String, LinkedList<EachTermList>> hashMap_byfreq, 
			String[] terms)
	{	
		/*Taking start time*/
		long startTime = System.currentTimeMillis();
		
		System.out.print("\nFUNCTION: termAtATimeQueryAnd "); 
		{
			for (int i=0; i<terms.length; i++)
			{
				System.out.print(terms[i]);
				if (i<terms.length-1)
				{
					System.out.print(", ");
				}
			}
		}
		
		/*Checking if all terms exists, even if one term does not exist then output is 'Terms not found'*/
		for (int i=0; i<terms.length; i++)
		{
			if (!hashMap_byfreq.containsKey(terms[i]))
			{ 
			System.out.print("\nTerms not found.");
			return;
			}
		}
		
		int comp=0;
		
		/*Creating new hashmap to keep the Doc IDs for comparison*/
		HashMap<Integer, Integer> Docids = new HashMap<Integer, Integer>();
		for (int i=0; i<terms.length; i++)
		{	
			/*Taking the posting list of each term and adding to Linked List*/
			LinkedList<EachTermList> Index2= hashMap_byfreq.get(terms[i]);
			
			/*Processing each Doc ID of each Term*/
			for(int m=0; m<Index2.size(); m++)
	        {	
				comp++;
				
				/*If the new Hashmap does not have the current Doc ID then add in the Hashmap 
				 and keep the mapped Integer value as 1*/
	    		if(!Docids.containsKey(Index2.get(m).Docid))
	    		{
	    			Integer int1=1;
	    			Docids.put(Index2.get(m).Docid, int1);
	    		}
	    		
	    		/*If the resultant Hashmap has the current Doc ID then increment its mapped Integer value by 1*/
	    		else
	    		{
	    			Integer newval=Docids.get(Index2.get(m).Docid)+1;
	    			Docids.remove(Index2.get(m).Docid);
	    			Docids.put(Index2.get(m).Docid, newval);
	    		}
	        }
		}
		
		/*Creating a Linked List to store the resultant Doc IDs*/
		LinkedList<DocList> Doclists = new LinkedList<DocList>();
		
		/*Reading from Hashmap and storing in resultant Linked list*/
		Iterator it = Docids.entrySet().iterator();
	    while (it.hasNext()) 
	    {
	        Map.Entry pair = (Map.Entry)it.next();
	        if ((int) pair.getValue()==terms.length)
	        {
	        	DocList obj1=new DocList((int)pair.getKey());
	        	Doclists.add(obj1);
	        }
		}
	    
	    /*Taking End time*/
	    long timetaken = System.currentTimeMillis() - startTime;
	    float timetakensec= timetaken/1000;
	    
	    /*Displaying docs found, comparisons made and time taken*/
	    System.out.print("\n"+Doclists.size()+" documents are found");
	    System.out.print("\n"+comp+" comparisions are made");
	    System.out.print("\n"+timetakensec+" seconds are used"); 
	    
	    /*If no docs Found*/
	    if (Doclists.size()==0)
	    {
	    	System.out.print("\nResult: Document not found.");
	    }
	    
	    /*If docs found then sort and display*/
	    else
	    {
	    	Collections.sort(Doclists, new SortDocid());
	    	System.out.print("\nResult: ");
	    	for (int k=0; k<Doclists.size();k++)
	    	{
	    		System.out.print(Doclists.get(k).Docid);
	    		if (k<Doclists.size()-1)
	    			System.out.print(", ");
	    		else
	    			continue;
	    	}
	    }
	}
	


	public static void termAtATimeQueryOr (HashMap<String, LinkedList<EachTermList>> hashMap_byfreq, 
		String[] terms)
	{	
		/*Taking Start Time*/
		long startTime = System.currentTimeMillis();
		System.out.print("\nFUNCTION: termAtATimeQueryOr "); 
		{
			for (int i=0; i<terms.length; i++)
			{
				System.out.print(terms[i]);
				if (i<terms.length-1)
				{
					System.out.print(", ");
				}
			}
		}
		
		/*Checking if at least one of the terms exist. If not then output is 'Terms not found'*/
		int c=0;
		for (int i=0; i<terms.length; i++)
		{
			if (!hashMap_byfreq.containsKey(terms[i]))
			{ 
			c++;
				if (c==terms.length)
				{
					System.out.print("\nTerms not found.");
					return;
				}
			}
			
		}
		
		int comp=0;
		
		/*Creating new hashmap to keep the Doc IDs for comparison*/
		HashMap<Integer, Integer> Docids = new HashMap<Integer, Integer>();
		for (int i=0; i<terms.length; i++)
		{	
			comp++;
			
			/*Checking if term is present in the Index, if not then skip that term*/
			if (!hashMap_byfreq.containsKey(terms[i]))
			{
				continue;
			}
			
			/*If term is present in Index then do the comparison*/
			else
			{	
				/*Taking the posting list of each term and adding to Linked List*/
				LinkedList<EachTermList> Index2= hashMap_byfreq.get(terms[i]);
				
				/*Processing each Doc ID of each Term*/
				for(int m=0; m<Index2.size(); m++)
				{	
					/*If Doc ID does not exist in new Hashmap then add in the Hashmap*/
					if(!Docids.containsKey(Index2.get(m).Docid))
					{
						Integer int1=1;
						Docids.put(Index2.get(m).Docid, int1);
					}
					/*If Exists in new Hashmap then ignore*/
					else
						continue;
				}
			}
		}
		
		/*Creating a Linked List to store the resultant Doc IDs*/
		LinkedList<DocList> Doclists = new LinkedList<DocList>();
		
		/*Reading from Hashmap and storing in resultant Linked list*/
		Iterator it = Docids.entrySet().iterator();
		while (it.hasNext()) 
		{
			Map.Entry pair = (Map.Entry)it.next();
			DocList obj1=new DocList((int)pair.getKey());
			Doclists.add(obj1);
		}
		
		/*Taking End Time*/
		long timetaken = System.currentTimeMillis() - startTime;
	    float timetakensec= timetaken/1000;
	    
	    /*Displaying docs found, comparisons made and time taken*/
		System.out.print("\n"+Doclists.size()+" documents are found");
	    System.out.print("\n"+comp+" comparisions are made");
	    System.out.print("\n"+timetakensec+" seconds are used"); 
	    
	    /*If no docs found*/
		if (Doclists.size()==0)
		{
			System.out.print("\nDocument not found.");
		}
		
		/*If docs found then sort and display*/
		else
		{
			Collections.sort(Doclists, new SortDocid());
			System.out.print("\nResult: ");
			for (int k=0; k<Doclists.size();k++)
			{
				System.out.print(Doclists.get(k).Docid);
				if (k<Doclists.size()-1)
					System.out.print(", ");
			}
		}
	}



	public static void docAtATimeQueryAnd (HashMap<String, LinkedList<EachTermList>> hashMap_bydocid, 
		String[] terms)
	{	
		/*Taking start time*/
		long startTime = System.currentTimeMillis();
		
		System.out.print("\nFUNCTION: docAtATimeQueryAnd "); 
		{
			for (int i=0; i<terms.length; i++)
			{
				System.out.print(terms[i]);
				if (i<terms.length-1)
				{
					System.out.print(", ");
				}
			}
		}
		
		/*Checking if all terms exist in index, even if one term does not exist output is 'Terms not found'*/
		for (int i=0; i<terms.length; i++)
		{
			if (!hashMap_bydocid.containsKey(terms[i]))
			{ 
			System.out.print("\nTerms not found.");
			return;
			}
			
		}
		
		int comp=0;
		
		/*Creating new Linked List to save resultant Doc IDs*/
		LinkedList<DocList> doclist=new LinkedList<DocList>();
		
		/*Creating a new Linked List to save the terms and its corresponding posting list as a node
		 to traverse each posting list in parallel  */
		LinkedList<LinkedList<EachTermList>> termlist= new LinkedList<LinkedList<EachTermList>>();
		
		int max=0;
		int flag=0;
		
		/*Creating an Int array to save the indexes of each posting list*/
		int[] indexl= new int[terms.length]; 
		
		/*Populating the Linked List having terms and posting lists*/
		for (int i=0; i<terms.length; i++)
		{
			indexl[i]=0;
			LinkedList<EachTermList> Index1= hashMap_bydocid.get(terms[i]);
			termlist.add(Index1);
		}
		
		for (int i=0; i<terms.length; i++)
		{	
			for (int j=indexl[i]; j<termlist.get(i).size(); j++)
			{   
				/*Setting a Max value at the start f the loop*/
				max=termlist.get(i).get(j).Docid;
				int count=0;
				for (int k=0; k<terms.length; k++)
				{	
					comp++;
					
					/*checking if the index is within the posting list size*/
					if (j<termlist.get(k).size())
					{
						/*checking if the current doc ID value is same as max, if yes then increment the count*/
						if (max==termlist.get(k).get(j).Docid)
						{
							count++;
							indexl[k]++;
							flag=0;
							
							/*If the count reaches the total number of terms given then add in the resultant*/
							if(count==terms.length)
							{
								DocList obj1= new DocList (max);
						    	doclist.add(obj1);
						    	count=0;
						    }
						}
						
						/*If Doc ID not equal to max then check if it is greater than max and update the max*/
						else
						{
							if (max<termlist.get(k).get(j).Docid)
							{
								max=termlist.get(k).get(j).Docid;
								indexl[k]++;
							}
							
							/*if the Doc ID is not equal to max then update flag to 1 for further processing*/
							flag=1;
						}
					}
					
					/*If index reaches beyond the posting list size then break loop and go to next term*/
					else
					{
						indexl[k]++;
						break;
					}
				}	
				
				/*if Doc ID not equal to max value then process further*/
				if (flag==1)
				{
					/*increment the index value for each term and compare with new Doc IDs*/
					for (int m=0; m<terms.length; m++)
					{
						for (int n=j+1; n<termlist.get(m).size(); n++) 
						{
							comp++;
							
							/*If Doc ID is equal to Max then increment the count, 
							 and if count reaches the total number of terms then add to output*/
							if (termlist.get(m).get(n).Docid==max)
							{
								count++;
								indexl[m]++;
								if (count>=terms.length)
								{
									DocList obj1= new DocList (max);
							    	doclist.add(obj1);
							    	count=0;
								}
							}
							
							/*if Doc ID is greater than max then go to next term's index*/
							else if (termlist.get(m).get(n).Docid>max)
							{
								indexl[m]++;
								break;
							}
							indexl[m]=n;
						}
					}
				}
			}
		}
		/*in all of the above loops we have incremented the index value for each term using indexl[]*/
		
		/*Taking End time*/
		long timetaken = System.currentTimeMillis() - startTime;
		float timetakensec= timetaken/1000;
	    
		/*Displaying docs found, comparisons made and time taken*/
		System.out.print("\n"+doclist.size()+" documents are found");
	    System.out.print("\n"+comp+" comparisions are made");
	    System.out.print("\n"+timetakensec+" seconds are used"); 
	    
	    /*If any Doc ID is present in output then display*/
		if (doclist.size()>=1 )
		{				
			System.out.print("\nResult: ");
			for (int x=0; x<doclist.size();x++)
			{
				System.out.print(doclist.get(x).Docid);
				if (x<doclist.size()-1)
	    			System.out.print(", ");
			}
			return;
		}
		
		/*If no Doc ID is present in output then display No document found*/
		else
		{
			System.out.print("\nResult: Document not found.");
			return;
		}

	}
	
	public static void docAtATimeQueryOr (HashMap<String, LinkedList<EachTermList>> hashMap_bydocid, 
			String[] termsgiven)
	{	
		/*Taking start time*/
		long startTime = System.currentTimeMillis();
		
		System.out.print("\nFUNCTION: docAtATimeQueryOr "); 
		{
			for (int i=0; i<termsgiven.length; i++)
			{
				System.out.print(termsgiven[i]);
				if (i<termsgiven.length-1)
				{
					System.out.print(", ");
				}
			}
		}
		
		/*Checking if at least one term exist in Index. If not then output 'Terms not found'*/
		int c=0;
		int d=termsgiven.length;
		for (int i=0; i<termsgiven.length; i++)
		{
			if (!hashMap_bydocid.containsKey(termsgiven[i]))
			{ 
			c++;
			d--;
			if (c==termsgiven.length)
			{
			System.out.print("\nTerms not found.");
			return;
			}
			}
		}
		
		/*Taking only the terms that exist in Index into new String*/
		String[] terms=new String[d];
		int e=0;
		for (int i=0; i<termsgiven.length; i++)
		{
			if (hashMap_bydocid.containsKey(termsgiven[i]))
			{ 
			terms[e]=termsgiven[i];
			e++;
			}
		}
		
		int comp=0;
		
		/*Creating new Linked List to save resultant Doc IDs*/
		LinkedList<DocList> doclist=new LinkedList<DocList>();
		
		/*Creating a new Linked List to save the terms and its corresponding posting list as a node
		 to traverse each posting list in parallel*/
		LinkedList<LinkedList<EachTermList>> termlist= new LinkedList<LinkedList<EachTermList>>();
		
		
		int max=0;
		/*creating new Int array to store all indexes of each term*/
		int[] indexl= new int[terms.length]; 
		
		/*Populating the Linked List having terms and posting lists*/
		for (int i=0; i<terms.length; i++)
		{
			indexl[i]=0;
			LinkedList<EachTermList> Index1= hashMap_bydocid.get(terms[i]);
			termlist.add(Index1);
		}
		
		for (int i=0; i<terms.length; i++)
		{	
			for (int j=indexl[i]; j<termlist.get(i).size(); j++)
			{   
				for (int k=0; k<terms.length; k++)
				{
					comp++;
					
					/*checking if the index is within the posting list size*/
					if (indexl[k]<termlist.get(k).size())
					{
						/*checking for each Doc ID if it is greater than the max value, 
						 if yes then add in the output*/
						if (termlist.get(k).size()>max)
						{	
							max=termlist.get(k).get(indexl[k]).Docid;
							DocList obj1= new DocList (max);
							doclist.add(obj1);
							indexl[k]++;
						}
						
						/*if it is equal to the max then ignore as the element would already be in output*/
						else if (termlist.get(k).size()==max)
						{
							indexl[k]++;
						}
						
						/*if the Doc ID is less than the max then check the output file,
						 if Doc ID does not exist in output file then add the Doc ID */
						else
						{  
							int flag=0;
							for (int x=0; x<doclist.size(); x++)
							{	comp++;
								if (termlist.get(k).get(indexl[k]).Docid==doclist.get(x).Docid)
								{
									flag=1;
								}
							}
							if (flag==1)
							{
								indexl[k]++;
							}
							else
							{
								DocList obj1= new DocList(termlist.get(k).get(indexl[k]).Docid);
								doclist.add(obj1);
								indexl[k]++;	
							}
						}
					}
				}
			}
		}
		Collections.sort(doclist, new SortDocid());
		
		/*Taking End Time*/
		long timetaken = System.currentTimeMillis() - startTime;
		float timetakensec= timetaken/1000;
	    
		/*Displaying docs found, comparisons made and time taken*/
		System.out.print("\n"+doclist.size()+" documents are found");
	    System.out.print("\n"+comp+" comparisions are made");
	    System.out.print("\n"+timetakensec+" seconds are used"); 
		
	    /*If any Doc ID is present in output then display*/
		if (doclist.size()>=1 )
		{				
			System.out.print("\nResult: ");
			for (int x=0; x<doclist.size();x++)
			{
				System.out.print(doclist.get(x).Docid);
				if (x<doclist.size()-1)
	    			System.out.print(", ");
			}
			return;
		}
		
		/*If no Doc ID is present in output then display No document found*/
		else
		{
			System.out.println("\nResult: Document not found.");
			return;
		}
		
	}
}	
	

