import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

public class Viterbi {
	private int BUF_SIZE = 30000;
	private double M_LN2 = 0.693147180559945309417;//-Math.LN2(2); 
	public double pcll = 0D;
	public Double[] factors = new Double[BUF_SIZE];
	private static Double s1 = 1D;
	private static Double s2 = 0D;
	private Map<String,Double> spaceMap;
	private Map<String,Double> charMap;
//	private double[][] transMatrix = {{0.1,0.9},{0.2,0.8}}; 
	private double[][] transMatrix = new double[2][2];// = {{0.6,0.4},{0,1.0}};
	
	public Viterbi(){
		spaceMap = new HashMap<String, Double>();
		charMap = new HashMap<String, Double>();
	}
	
	public Viterbi(double[][] matrix, Map<String, Double> map1, Map<String, Double> map2){
		spaceMap = new HashMap<String, Double>();
		charMap = new HashMap<String, Double>();
		this.spaceMap = map1;
		this.charMap = map2;
		this.transMatrix = matrix;
	}

	public static void main(String[] args) throws IOException{
		Viterbi ins = new Viterbi();
		System.out.println(Math.log(-4.395819956805218));
		ins.getTrans();
		ins.getEmission();
		ins.getTrellis();
		return;
	}
	
	public ArrayList<Integer> getTrellis() throws IOException{
		File dataFile = new File(System.getProperty("user.dir") + "/train.txt");
		BufferedReader br = new BufferedReader(new FileReader(dataFile));
		char[] buf = new char[BUF_SIZE];
		
		br.read(buf);
		br.close();
		for(int i=BUF_SIZE-1;i>=1;i--){
			buf[i]=buf[i-1];
		}
		
		/*
		  buf[1]='A';
	        buf[2]='A';
	        buf[3]='B';
	        spaceMap.clear();
	        spaceMap.put("A", 0.8);
	        spaceMap.put("B", 0.2);
	        charMap.clear();
	        charMap.put("A", 0.3);
	        charMap.put("B", 0.7);		
		*/
		 
		ArrayList<Integer> res = new ArrayList<Integer>();
		
		int T=0;
		int i=1; //char index start from 1
		
		ArrayList<Integer> liPre1 = new ArrayList<Integer>();
		liPre1.add(0);
		ArrayList<Integer> liPre2 = new ArrayList<Integer>();
		liPre2.add(0);
		
		while(buf[i] != '\0'){
			if(buf[i] == ' ' || (buf[i] <= 'Z' && buf[i] >= 'A')){
			String s = String.valueOf(buf[i]);
			if(buf[i] == ' ') s = "SPACE";
			
			double temp1 = s1; // store s1 to a temp variable
			double temp2 = s2; // store s2 to a temp variable
			// To state1
			if(i==1){
				liPre1.add(1);
				liPre2.add(1);
				s1=Math.log(temp1) + Math.log(transMatrix[0][0]) + Math.log(spaceMap.get(s));;
				s2=Math.log(temp1) + Math.log(transMatrix[0][1]) + Math.log(charMap.get(s));
			}
			else{
				System.out.printf("temp1=%s,%s,%s,%s\n",temp1,Math.log(temp1) , Math.log(transMatrix[0][0]) , Math.log(spaceMap.get(s)));
				double val1=Math.log(temp1) + Math.log(transMatrix[0][0]) + Math.log(spaceMap.get(s));
				double val2=Math.log(temp2) + Math.log(transMatrix[1][0]) + Math.log(spaceMap.get(s));
				
				if(val1 > val2 ){
					s1 = val1;
					liPre1.add(1);
				}
				else{
					s1 = val2;
					liPre1.add(2);
				}
				
				//To state2
				val1=Math.log(temp1) + Math.log(transMatrix[0][1]) + Math.log(charMap.get(s));
				val2=Math.log(temp2) + Math.log(transMatrix[1][1]) + Math.log(charMap.get(s));
				
				if(val1>val2 ){
					s2= val1;
					liPre2.add(1);
				}
				else{
					s2=val2;
					liPre2.add(2);
				}
			}
			
			System.out.printf("i=%d,s1=%s,s2=%s\n",i,s1,s2);
			if(i>20) break;
			}//end if
			i++; //T=i;
			
		}//end while
		
		T = i-1;
		//System.out.println("T="+T);
		
		int k = liPre2.size() - 1;
		int pre;
		if(s2>s1){
			res.add(2);
			pre=liPre2.get(k);
		}
		else{
			res.add(1);
			pre=liPre1.get(k);
		}
		res.add(pre);
		k--;
		while(k>=1){
			//System.out.println("k="+k);
			if(pre == 1){
				pre = liPre1.get(k);
			}
			else{
				pre=liPre2.get(k);
			}
			res.add(pre);
			k--;
		}
		for(int j=0;j<res.size();j++){
			//System.out.printf("j=%d,resj=%d,T=%d,T-j=%d\n",j,res.get(j),T,T-j);
			if(res.get(j)==1)System.out.println(buf[T-j]);
		}
		return res;
	}
	
	/* Evaluate log(exp(left) + exp(right)) more accurately.
	 * log(exp(left) + exp(right))
	 *    = log(exp(left)) + log(1 + exp(right) / exp(left))
	 *    = left + log(1 + exp(right - left))
	 * Note: log1p(x) accurately computes log(1+x) for small x.
	 */
	double LogAdd(double left, double right) {
	  if (right < left) {
	    return left + Math.log1p(Math.exp(right - left));
	  } else if (right > left) {
	    return right + Math.log1p(Math.exp(left - right));
	  } else {
	    return left + M_LN2;
	  }
	}
	
	public void getTrans() throws IOException{
		File transFile = new File(System.getProperty("user.dir")+"/trans.txt");
		BufferedReader br = new BufferedReader(new FileReader(transFile));
		String s = null;
		s = br.readLine();
		String[] arr = s.split("\t");
		transMatrix[0][0]=Double.parseDouble(arr[0]);
		transMatrix[0][1]=Double.parseDouble(arr[1]);
		s = br.readLine();
		arr = s.split("\t");
		transMatrix[1][0]=Double.parseDouble(arr[0]);
		transMatrix[1][1]=Double.parseDouble(arr[1]);
	}
	
	
	public void getEmission() throws IOException{
		File charFile = new File(System.getProperty("user.dir")+"/char.txt");
		File spaceFile = new File(System.getProperty("user.dir")+"/space.txt");
		
		BufferedReader br = new BufferedReader(new FileReader(charFile));
		String s = null;
		while((s = br.readLine()) != null){
			String[] arr = s.split("\t");
			charMap.put(arr[0], Double.valueOf(arr[1]));
		}
		br.close();
		br = new BufferedReader(new FileReader(spaceFile));
		while((s = br.readLine()) != null){
			String[] arr=s.split("\t");
			spaceMap.put(arr[0], Double.valueOf(arr[1]));
		}
		br.close();
	}
		
}
