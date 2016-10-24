import java.util.HashMap;

public class Viterbi
{
	static HashMap<String, Double> prob = new HashMap<String, Double>();
	static String sequence;
	static int seqLength;
	static String[] states = {"H", "C"};
	static int numOfStates;	
	
	public static int getMaxIndex(double[] arr)
	{
		int index = -1;
		double max = -1.0;
		for(int i=0;i<arr.length;++i)
		{
			if(arr[i] > max)
			{
				max = arr[i];
				index = i;
			}
		}
		return index;
	}
	
	public static void viterb()
	{
		double[] temp = new double[numOfStates];
		double[][] tab = new double[seqLength][numOfStates];
		int[][] back = new int[seqLength][numOfStates];
		for(int i = 0;i<seqLength;++i)
		{
			for(int j = 0;j<numOfStates;++j)
			{
				if(i == 0)
				{
					String trans = states[j] + "S";
					String obs = sequence.charAt(i) + states[j];
					tab[i][j] = prob.get(trans) * prob.get(obs);	
					//System.out.println(i + "." + j + "->" + tab[i][j]);
				}
				else
				{
					int best_state = -1;
					temp = new double[numOfStates];
					for(int k=0;k<numOfStates;++k)
					{
						String trans = states[k] + states[j];
						String obs = sequence.charAt(i) + states[j];
						temp[k] = prob.get(trans) * prob.get(obs) * tab[i-1][k];							
					}
					best_state = getMaxIndex(temp);
					tab[i][j] = temp[best_state];
					back[i][j] = best_state;	
					
					//System.out.println(i + "." + j + "->" + temp[0] + " " + temp[1]);
					//System.out.println(i + "." + j + "->" + best_state);
				}
			}			
		}
		int final_state = getMaxIndex(tab[seqLength-1]);
		
		int cur = final_state;
		String res = states[final_state];
		for(int i=seqLength-1;i>0;--i)
		{			
			cur = back[i][cur];
			res += states[cur];
		}
		System.out.format("The most likely weather sequence for the given input is %s\n", new StringBuilder(res).reverse());
	}
	public static void main(String[] args)
	{		
		prob.put("1H", 0.2);
		prob.put("2H", 0.4);
		prob.put("3H", 0.4);
		
		prob.put("1C", 0.5);
		prob.put("2C", 0.4);
		prob.put("3C", 0.1);
		
		prob.put("HS", 0.8);
		prob.put("HH", 0.7);
		prob.put("HC", 0.4);
		
		prob.put("CS", 0.2);
		prob.put("CC", 0.6);
		prob.put("CH", 0.3);
		
		sequence = args[0];
		seqLength = sequence.length();
		numOfStates = states.length;
		viterb();		
	}
}