import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;

public class POS 
{
	static HashMap<String, Integer> htWordCount = new HashMap<String, Integer>();
	static HashMap<String, HashMap<String, Integer>> htTagCount = new HashMap<String, HashMap<String, Integer>>();
	static HashMap<String, Integer> htPosCount = new HashMap<String, Integer>();
	static HashMap<String, String> wordMaxTag = new HashMap<String, String>();
	static HashMap<String, String> lowProbTag = new HashMap<String, String>();
	static HashSet<String> maxErrWord = new HashSet<String>();
	
	static HashMap<String, Integer> firstErrCount = new HashMap<String, Integer>();
	static HashMap<String, Integer> correctedErrCount = new HashMap<String, Integer>();	
	
	private static void buildHTables(String fName) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(fName));
		String line = reader.readLine();
		while(line != null)
		{
			String[] words = line.split("\\s+");
			
			for(String word : words)
			{
				String[] data = word.split("_");
				
				//ignore if the word contains only non alpha-numeric
				if(!Pattern.matches("[\\W]+", data[0]))
				{
					if(htWordCount.containsKey(data[0]))
                    {
						htWordCount.put(data[0], htWordCount.get(data[0])+1);
                    }
                    else
                    {
                    	htWordCount.put(data[0], 1);
                    }
					
					if(htTagCount.containsKey(data[0]))
					{
						HashMap<String, Integer> temp = htTagCount.get(data[0]);
						if(temp.containsKey(data[1]))
						{
							temp.put(data[1], temp.get(data[1])+1);
						}
						else
						{							
							temp.put(data[1], 1);							
						}
						htTagCount.put(data[0], temp);
					}
					else
					{
						HashMap<String, Integer> temp = new HashMap<String, Integer>(); 
						temp.put(data[1], 1);
						htTagCount.put(data[0], temp);
					}
				}				
			}			
			line = reader.readLine();
		}		
		
		for(String k : htTagCount.keySet())
		{
			HashMap<String, Integer> temp = htTagCount.get(k);
			String maxCountTag = "";
			int maxCount = 0;			
			
			for(String s : temp.keySet())
			{
				int cnt = temp.get(s);				
				String pos = k + "_" + s;
				//System.out.println(pos + " " + cnt);
				htPosCount.put(pos, cnt);
				
				if(maxCount < cnt)
				{
					maxCount = cnt;
					maxCountTag = s;
				}
			}			
			wordMaxTag.put(k, maxCountTag);
		}
		reader.close();
	}
	
	private static void getLowProbTags()
	{
		for(String s : maxErrWord)
		{			
			HashMap<String, Integer> temp = htTagCount.get(s);
			String ignoreTag = wordMaxTag.get(s);
			String val = "";
			for(String k : temp.keySet())
			{
				if(!(k.equals(ignoreTag)))					
					val += k + "\\";
			}
			lowProbTag.put(s, val);			
		}			
	}
	
	private static <K, V extends Comparable<? super V>> Map<K, V> sortByValues(Map<K, V> ht)
	{
		List<Map.Entry<K, V>> al = new LinkedList<Map.Entry<K, V>>(ht.entrySet());
		Collections.sort(al, new Comparator<Map.Entry<K, V>>()
				{
					public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2)
					{
						return e2.getValue().compareTo(e1.getValue());
					}
				});
		Map<K, V> result = new LinkedHashMap<K, V>();
		
		for(Map.Entry<K, V> e : al)
		{
			result.put(e.getKey(), e.getValue());
		}
		return result;
	}
	
	private static void compareTags(String f1, String f2) throws FileNotFoundException, IOException
	{
		BufferedReader reader1 = new BufferedReader(new FileReader(f1));
		BufferedReader reader2 = new BufferedReader(new FileReader(f2));
		HashMap<String, Integer> errCount = new HashMap<String, Integer>();
		
		String line1 = reader1.readLine();
		String line2 = reader2.readLine();
				
		boolean firstIter = false;
		
		if(maxErrWord.isEmpty())
			firstIter = true;
		
		while(line1 != null && line2 != null)
		{
			if(line2.trim().equals(line2))
			{
				
			}
			else
			{
				String[] w1 = line1.split("\\s+");
				String[] w2 = line2.split("\\s+");
				
				if(w1.length == w2.length)
				{
					for(int i=0;i<w1.length;++i)
					{
						if(w1[i].equals(w2[i]))
						{
							
						}
						else
						{
							String[] data = w2[i].split("_");						
							if(firstIter)
							{							
								if(errCount.containsKey(data[0]))
								{
									errCount.put(data[0], errCount.get(data[0])+1);
								}
								else
								{
									errCount.put(data[0], 1);									
								}								
							}
							else
							{
								if(maxErrWord.contains(data[0]))
								{
									if(errCount.containsKey(data[0]))
									{
										errCount.put(data[0], errCount.get(data[0])+1);
									}
									else
									{
										errCount.put(data[0], 1);
									}
								}
							}
						}
					}
				}
				else
				{
					//control should not come here
				}
			}
			line1 = reader1.readLine();
			line2 = reader2.readLine();
		}
		Map<String, Integer> res = new HashMap<String, Integer>();
		int count = 0;
		if(firstIter)
		{
			res = sortByValues(errCount);
		}
		else
		{
			res = errCount;
		}
		
		for(String k : res.keySet())
		{
			//System.out.println(k + " " + res.get(k) + " " + (double)res.get(k) / htWordCount.get(k));
			if(firstIter)
			{
				firstErrCount.put(k, res.get(k));
				maxErrWord.add(k);
			}
			else
				correctedErrCount.put(k, res.get(k));
			
			count++;
			if(count >= 5)
				break;
		}
		reader1.close();
		reader2.close();
	}
	
	private static void reTagErr() throws IOException
	{
		PrintWriter file = new PrintWriter("retagErr.txt", "UTF-8");
		BufferedReader reader = new BufferedReader(new FileReader("retag.txt"));
		String line = reader.readLine();
				
		while(line != null)
		{
			String[] words = line.split("\\s+");	
			String prevTag = "";
			for(String word : words)
			{
				String[] data = word.split("_");
				if(!Pattern.matches("[\\W]+", data[0]))
				{					
					if(data[0].equals("that"))
					{
						if(prevTag.contains("NNS"))
						{
							file.print(data[0] + "_" + "WDT ");							
						}
						else
						{
							file.print(data[0] + "_" + data[1] + " ");
						}
					}
					else if(data[0].equals("have"))
					{
						if(prevTag.contains("TO"))
						{
							file.print(data[0] + "_" + "VB ");
						}
						else
						{
							file.print(data[0] + "_" + data[1] + " ");
						}
					}
					else if(data[0].equals("more"))
					{
						if(prevTag.contains("VB"))
						{
							file.print(data[0] + "_" + "RBR ");
						}
						else
						{
							file.print(data[0] + "_" + data[1] + " ");
						}
					}
					else if(data[0].equals("'s"))
					{
						if(prevTag.contains("PRP") || prevTag.contains("DT"))
						{
							file.print(data[0] + "_" + "VBZ ");
						}
						else
						{
							file.print(data[0] + "_" + data[1] + " ");
						}
					}
					else if(data[0].equals("plans"))
					{
						if(prevTag.contains("VB"))
						{
							file.print(data[0] + "_" + "NNS ");
						}
						else
						{
							file.print(data[0] + "_" + data[1] + " ");
						}
					}
					else
					{
						file.print(data[0] + "_" + data[1] + " ");
					}
					prevTag = data[1];
				}
				else
				{
					file.print(data[0] + "_" + data[1] + " ");
				}
			}
			file.println();
			line = reader.readLine();
		}		
		reader.close();		
		file.close();
	}
	
	private static void reTag(String fName)
			throws FileNotFoundException, IOException
	{
		PrintWriter file = new PrintWriter("retag.txt", "UTF-8");
		BufferedReader reader = new BufferedReader(new FileReader(fName));
		String line = reader.readLine();
		
		while(line != null)
		{
			String[] words = line.split("\\s+");			
			for(String word : words)
			{
				String[] data = word.split("_");
				if(Pattern.matches("[\\W]+", data[0]))
				{
					file.print(word + " ");					
				}
				else
				{
					String temp = data[0] + "_" + wordMaxTag.get(data[0]);
					file.print(temp + " ");
				}
			}
			file.println();
			line = reader.readLine();
		}
		reader.close();		
		file.close();
	}
	
	private static void displayResult()
	{
		System.out.format("Word" + "\t" + "	Majority Tag"+ "\t" + "	Error Tags" + "\t" + "	First Error Count" + "\t" +	"Corrected Error Count\n");
		for(String word : maxErrWord)
		{
			System.out.format("%s" + "\t" + "%15s" + "\t" + "%26s" + "\t" + "%15s" + "\t" + "%25s" + "\n", word, wordMaxTag.get(word), lowProbTag.get(word), firstErrCount.get(word), correctedErrCount.get(word));
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		buildHTables(args[0]);		
		reTag(args[0]);
		
		compareTags(args[0], "retag.txt");		
		
		reTagErr();
		
		compareTags(args[0], "retagErr.txt");
		
		getLowProbTags();
		displayResult();
	}	
}
