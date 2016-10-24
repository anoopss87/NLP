import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class Bigram
{
	private static HashMap<String, Integer> uGramMap = new HashMap<String, Integer>();
	private static HashMap<String, Integer> biGramMap = new HashMap<String, Integer>();		
	
	private static String s1 = "The president has relinquished his control of the company's board";
	private static String s2 = "The chief executive officer said the last year revenue was good";
	
	private static int vocabSize;
	private static int GT_N_S1;
	private static int GT_N_S2;	
	private static int tokenSize;
	
	public static void displayCount(int[][] tab)
	{
		for(int i=0;i<tab.length;++i)
		{
			for(int j=0;j<tab.length;++j)
			{
				System.out.format("%4d ", tab[i][j]);
			}
			System.out.println();
		}
	}
	
	public static void displayCountD(double[][] tab)
	{
		for(int i=0;i<tab.length;++i)
		{
			for(int j=0;j<tab.length;++j)
			{
				System.out.format("%f ", tab[i][j]);
			}
			System.out.println();
		}
	}
	
	public static int sumCount(int[][] tab, HashMap<Integer, Integer> GTMap)
	{
		int sum = 0;
		for(int i=0;i<tab[0].length;++i)
		{
			for(int j=0;j<tab[0].length;++j)
			{
				sum += tab[i][j];
				if(GTMap.containsKey(tab[i][j]))
				{
					GTMap.put(tab[i][j], GTMap.get(tab[i][j])+1);
				}
				else
				{
					GTMap.put(tab[i][j], 1);
				}
			}
		}		
		return sum;
	}
	
	public static String[] removeDup(String line)
	{
		LinkedHashSet<String> hs = new LinkedHashSet<>();
		String[] words = line.split("\\s+");
		for(String word : words)
		{
			hs.add(word);			
		}
		return hs.toArray(new String[hs.size()]);
	}	
	
	
	public static void buildGTTable(String[] words, int N, int[][] tab, HashMap<Integer, Integer> GTMap, int sent)
	{
		int size = words.length;
		double[][] countTab = new double[size][size];
		double[][] probTab = new double[size][size];
		for(int i=0;i<size;++i)
		{
			for(int j=0;j<size;++j)
			{
				int count = tab[i][j] + 1;
				if(GTMap.get(count) == null || GTMap.get(count-1) == null)
				{
					countTab[i][j] = 0;
					probTab[i][j] = 0;					
				}
				else
				{					
					countTab[i][j] = count * (GTMap.get(count) / (double)GTMap.get(count-1));
					probTab[i][j] = countTab[i][j] / N;					
				}				
			}
		}
		
		double prob = (double)uGramMap.get(words[0]) / tokenSize;
		for(int i=1;i<size;++i)
		{
			prob *= probTab[i][i-1];			
		}
		System.out.format("\nBigram counts table using GT for the sentence S%s is as below:\n", sent);
		displayCountD(countTab);
		System.out.format("\nBigram probabilities table using GT for the sentence S%d is as below:\n", sent);
		displayCountD(probTab);
		System.out.format("\nJoint probability for the sentence S%d is " +  prob + "\n", sent);
	}
	public static int[][] buildTable(String[] words, String type, int sent)
	{		
		int size = words.length;
		int[][] countTab = new int[size][size];
		double[][] probTab = new double[size][size];
		for(int i=0;i<size;++i)
		{
			for(int j=0;j<size;++j)
			{
				String pair = words[i] + " " + words[j];
				if(biGramMap.get(pair) == null)
				{
					if(type.equals("mle"))
					{
						countTab[i][j] = 0;
						probTab[i][j] = countTab[i][j];
					}
					else if(type.equals("aos"))
					{
						countTab[i][j] = 1;						
						if(uGramMap.get(words[i]) == null)
						{
							probTab[i][j] = 0.00001;
						}
						else
						{
							probTab[i][j] = countTab[i][j] / (double)(uGramMap.get(words[i]) + vocabSize);							
						}
					}					
				}
				else
				{
					if(type.equals("mle"))
					{
						countTab[i][j] = biGramMap.get(pair);
						probTab[i][j] = countTab[i][j] / (double)uGramMap.get(words[i]);
					}
					else if(type.equals("aos"))
					{
						countTab[i][j] = biGramMap.get(pair) + 1;						
						probTab[i][j] = countTab[i][j] / (double)(uGramMap.get(words[i]) + vocabSize);
					}
				}
			}
		}
		
		double prob = (double)uGramMap.get(words[0]) / tokenSize;
		for(int i=1;i<size;++i)
		{
			prob *= probTab[i][i-1];			
		}
		System.out.format("\nBigram counts table using %s for the sentence S%d is as below\n", type, sent);
		displayCount(countTab);
		System.out.format("\nBigram probabilities table using %s for the sentence S%d is as below\n", type, sent);
		displayCountD(probTab);
		System.out.format("\nJoint probability for the sentence S%d is " +  prob + "\n", sent);
		return countTab;
	}
	
	public static void buildUniGram(String[] words)
	{
		//unigrams
		for(String word : words)
		{			
			if(uGramMap.containsKey(word))
			{
				uGramMap.put(word, uGramMap.get(word) + 1);
			}
			else
			{
				uGramMap.put(word, 1);
			}
		}
	}
	
	public static void buildBiGram(String[] words)
	{
		//bigrams
		for(int i=1;i<words.length;++i)
		{
			String pair = words[i-1] + " " + words[i];
			if(biGramMap.containsKey(pair))
			{
				biGramMap.put(pair, biGramMap.get(pair) + 1);
			}
			else
			{
				biGramMap.put(pair, 1);
			}
		}
	}
	
	public static void main(String[] args) throws URISyntaxException, IOException 
	{
		URL path = ClassLoader.getSystemResource("Corpus.txt");
		if(path==null)
		{
		     System.out.println("Input file not found");
		     System.exit(1);
		}		
		BufferedReader reader = new BufferedReader(new FileReader(args[0]));
		String line = reader.readLine();
		
		while(line != null)
		{
			String[] words = line.toLowerCase().split("\\s+");
			tokenSize += words.length;
			
			buildUniGram(words);
			buildBiGram(words);
			
			line = reader.readLine();
		}
		vocabSize = uGramMap.size();
		
		String[] s1_arr = removeDup(s1.toLowerCase());
		String[] s2_arr = removeDup(s2.toLowerCase());
		
		int[][] countTabMleS1 = new int[s1_arr.length][s1_arr.length];
		int[][] countTabMleS2 = new int[s2_arr.length][s2_arr.length];		
		
		HashMap<Integer, Integer> GTMapS1 = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> GTMapS2 = new HashMap<Integer, Integer>();
		
		
		countTabMleS1 = buildTable(s1_arr, "mle", 1);
		GT_N_S1 = sumCount(countTabMleS1, GTMapS1);
		System.out.println();
		
		buildTable(s1_arr, "aos", 1);
		System.out.println();
		
		buildGTTable(s1_arr, GT_N_S1, countTabMleS1, GTMapS1, 1);
		System.out.println();
		
		countTabMleS2 = buildTable(s2_arr, "mle", 2);
		GT_N_S2 = sumCount(countTabMleS2, GTMapS2);
		System.out.println();
		
		buildTable(s2_arr, "aos", 2);
		System.out.println();
		
		buildGTTable(s2_arr, GT_N_S2, countTabMleS2, GTMapS2, 2);
		
		reader.close();
	}
}
