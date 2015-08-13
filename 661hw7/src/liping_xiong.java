import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;


public class BackwardAlg {
	private int BUF_SIZE = 30000;
	public double pcll = 0D;
	private static Double s1 = 0D;
	private static Double s2 = 1D;
	private Double[] factors = new Double[BUF_SIZE];
	private Map<String,Double> spaceMap;
	private Map<String,Double> charMap;
	private double[][] transMatrix = {{0.1,0.9},{0.2,0.8}};  // space->space: ; char->space:0.2, char->char:0.8
	
	public BackwardAlg(){
		spaceMap = new HashMap<String, Double>();
		charMap = new HashMap<String, Double>();
	}
	
	public BackwardAlg(double[][] matrix, Map<String, Double> map1, Map<String, Double> map2, Double[] facts){
		spaceMap = new HashMap<String, Double>();
		charMap = new HashMap<String, Double>();
		this.spaceMap = map1;
		this.charMap = map2;
		this.transMatrix = matrix;
		this.factors = facts;
	}
	
	public static void main(String[] args) throws IOException{
		BackwardAlg ins = new BackwardAlg();
		ins.getEmission();
		ins.getTrellis();
		return;
	}
	
	public ArrayList<Cell> getTrellis() throws IOException{
		File dataFile = new File(System.getProperty("user.dir") + "/train.txt");
		BufferedReader br = new BufferedReader(new FileReader(dataFile));
		ArrayList<Cell> res = new ArrayList<Cell>();
		char[] buf = new char[BUF_SIZE];
		
		br.read(buf);
		br.close();
		
		for(int i=BUF_SIZE-1;i>=1;i--){
			buf[i]=buf[i-1];
		}
		
		double factor = 0D;
		double C = 0D;
		int size = factors.length;
		
		//reverse the data
		int i=buf.length-1;
		while(buf[i]=='\0' || buf[i]=='\n') i--;
		
		int T=i;
		res.add(new Cell(String.valueOf(buf[T]), s1, s2));
		i--;
		// i points to the last observation
		
		while(i>=0){
			//System.out.printf("backward: i=%d,buf[i]=[%s]\n",i, buf[i]);
			
			if(i==0 || buf[i] == ' ' || (buf[i] <= 'Z' && buf[i] >= 'A')){
			String s = String.valueOf(buf[i+1]);
			//System.out.println("s="+s);
			if(s.equals(" ")) s = "SPACE";
			//System.out.printf("backward: i=%d,buf[i+1]=[%s],s=%s\n",i, buf[i+1],s);
			//System.out.println("Backward: i="+i+"s="+ s);
			double temp1 = s1; // store s1 to a temp variable
			double temp2 = s2; // store s2 to a temp variable
				//update s1, s2
				//update s1,sum over all paths that point to s1, s1 is SPACE.
			s1 = temp1 * transMatrix[0][0] * spaceMap.get(s) + temp2 * transMatrix[0][1] * charMap.get(s);
			s2 = temp1 * transMatrix[1][0] * spaceMap.get(s) + temp2 * transMatrix[1][1] * charMap.get(s); 
			//System.out.printf("s1=%s,s2=%s\n",s1,s2);
			
			//C = s1+s2;
			C = this.factors[i+1];
			factor += Math.log(C);
			//System.out.printf("factors[i+1]=%s,factor=%s\n",factors[i+1],factor);
			s1 = s1/C;
			s2 = s2/C;
			
			res.add(new Cell(s, s1, s2));
			//System.out.printf("s=%s,s1=%s,s2=%s\n",s,s1,s2);
			}//end if
			i--;
		}//end while
		
		double sum = factor + Math.log(s1);
		//System.out.printf("backward pc-ll=%s,s1=%s,s2=%s,factor=%s,T=%d,sum=%s\n",sum / T,s1,s2,factor,T,sum);
		this.pcll = sum / T;
		
		//Reverse beta
		int k=0; int m=res.size()-1;
		while(k<m){
			Cell temp = res.get(k);
			res.set(k, res.get(m));
			res.set(m,temp);
			k++;m--;
		}
		
		return res;
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
import java.util.ArrayList;

public class Cell {
	String obs;
	ArrayList<Double> columnVal;
	public Cell(String s, double state1, double state2){
		obs = s;
		columnVal = new ArrayList<Double>();
		columnVal.add(state1);
		columnVal.add(state2);
	}
}import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ForBackAlg{
	private static final boolean DEBUG = false;
	private static final boolean debug = false;
	private static final boolean trellis = false;
	private static int BUF_SIZE = 30000;
	static Map<String, Double> spaceMap;
	static Map<String, Double> charMap;
	private static Double[] factors = new Double[BUF_SIZE];
	
	private double s1 = 0;//state1
	private double s2 = 0;//state2
	private static double transMatrix[][] = new double[2][2];//{{0.1,0.9},{0.2,0.8}};
	private double emission[][] = new double[2][27];
	public ArrayList<Cell> alpha;
	public ArrayList<Cell> beta;
	static char[] buf;
	static String[] chArr=new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
			"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z","SPACE"};
	
	public Double[][][] table;
	public Double[][] gama;
	
	public ForBackAlg(){
		spaceMap = new HashMap<String, Double>();
		charMap = new HashMap<String, Double>();
	}
	
	public static void main(String[] args) throws IOException{
		ForBackAlg ins = new ForBackAlg();
		File dataFile = new File(System.getProperty("user.dir") + "/train.txt");
		BufferedReader br = new BufferedReader(new FileReader(dataFile));
		buf = new char[BUF_SIZE];
		br.read(buf);
		br.close();
		for(int i=BUF_SIZE-1;i>=1;i--){
			buf[i]=buf[i-1];
		}
		ins.getTrans();
		ptrTrans();
		ins.getEmission();
		int it = 0;
		double pre,cur;
		double backPcll;
		
		ForwardAlg f = new ForwardAlg(ins.transMatrix, ins.spaceMap, ins.charMap);
		f.getEmission();
		ins.alpha = f.getTrellis();
		factors = f.factors;
		pre = f.pcll;
		
		BackwardAlg b = new BackwardAlg(ins.transMatrix, ins.spaceMap, ins.charMap, factors);
		b.getEmission();
		ins.beta = b.getTrellis();
		backPcll=b.pcll;
		
		//next = ins.getTrellis();
		System.out.printf("it=%d,cur=%s\n",it,pre);
		ins.EStep();
		ins.MStep();
		
		it++;
		
		while(true){
			f = new ForwardAlg(ins.transMatrix, ins.spaceMap, ins.charMap);
			//f.getEmission();
			ins.alpha = f.getTrellis();
			factors = f.factors;
			cur = f.pcll;
			
			b = new BackwardAlg(ins.transMatrix, ins.spaceMap, ins.charMap, factors);
			//b.getEmission();
			ins.beta = b.getTrellis();
			backPcll=b.pcll;
			
			//next = ins.getTrellis();
			System.out.printf("it=%d,cur=%s\n",it,cur);
//			System.out.printf("it=%d,cur=%s,backPcll=%s\n",it,cur,backPcll);
			
			ins.EStep();
			ins.MStep();
			
			if(it>2 &&  Math.abs(cur-pre)/Math.abs(pre) < 0.0001 ) break;
			pre = cur;
			it++;
		}
		ptrTrans();
		ptrEmission();
		
	}
	
	public void EStep(){
		//System.out.printf("alpha.size()=%d,beta.size()=%d\n",alpha.size(),beta.size());
		table = new Double[alpha.size()][2][2]; //t,i,j
		double denom = 0D;
		for(int t = 0; t < table.length -1 ; t++){
			for(int i = 0; i < 2; i++){
				for(int j = 0; j < 2; j++){
					
					//if(DEBUG) System.out.printf("t=%d,i=%d,j=%d,buf[t]=%s\n",t,i,j,buf[t]);
					//if(DEBUG) System.out.println(buf[3]);
					
					double Ati = alpha.get(t).columnVal.get(i); // time t, state i,alhpa value
					double Aij = transMatrix[i][j]; // transition prob from i to j 
					
					double EjOt1 = 0; // j, t+1 emission prob
					String s= (String) ((buf[t+1]==' ') ? "SPACE":String.valueOf(buf[t+1]));
					//System.out.printf("s=%s\n",s);
					if(j == 0){
						EjOt1 = spaceMap.get(s); //when state is 0, Obs is buf[t+1],
					}
					else{
						EjOt1 = charMap.get(s);
					}
					double Bt1j = beta.get(t+1).columnVal.get(j) ; // time t+1, state j
					/*double denom = alpha.get(t).columnVal.get(0) * beta.get(t).columnVal.get(0) + 
									alpha.get(t).columnVal.get(1) * beta.get(t).columnVal.get(1);
					*/
					double nume = ( Ati * Aij * EjOt1 * Bt1j) / factors[t+1];
					//denom += nume;
					
					table[t][i][j] = nume;
					//if(debug)System.out.printf("t=%d,i=%d,j=%d\n",t,i,j);
					//System.out.printf("Estep nume=%s,denom=%s\n",nume,denom);
					//System.out.printf("Estep t=%d,i=%d,j%d,table[t][i][j]=%s\n",t,i,j,table[t][i][j]);
					//if(table[t][i][j] == 0D) System.out.printf("Estep t=%d,i=%d,j%d,table[t][i][j]=%s\n",t,i,j,table[t][i][j]);
					
				}//end i loop
			}//end j loop
		}//end t loop
	}//end function
	
	public void MStep(){
		if(debug)System.out.println("Mstep");
		for(int i = 0; i < 2; i++){
			for(int j = 0; j < 2; j++){
				double nume = 0D;
				
				for(int t = 0; t < table.length - 1; t++){ // 0 to T-1
					  nume += table[t][i][j];
					  //System.out.printf("table[t][i][j]=%s\n",table[t][i][j]);
				}
				double denom = 0D;
				for(int t = 0; t < table.length - 1; t++){
					 denom += table[t][i][0] + table[t][i][1];
				}
				
				transMatrix[i][j] = nume/denom;
				if(debug)System.out.printf("update transition, nume=%s,denom=%s\n",nume,denom);
				if(debug)System.out.printf("transMatrix[i][j]=%s\n",transMatrix[i][j]);
			}
			//this.ptrTrans();
			//this.ptrEmission();
		}//end update transMatrix
		
		//update emission probability
		for(int j=0;j<2;j++){
			for(int k=0;k<27;k++){
				double nume = 0D;
				double denom = 0D;
				
				for(int t = 0; t < table.length - 1; t++){ // 0 to T-1
					//System.out.printf("alpha.get(t).obs=%s,chArr[k]=%s\n", alpha.get(t).obs, chArr[k]);
					if(alpha.get(t+1).obs.equals(chArr[k])) { // Ot == vk
						if(debug)System.out.printf("equal alpha.get(t).obs=%s,chArr[k]=%s\n", alpha.get(t).obs, chArr[k]);
						nume += table[t][0][j] + table[t][1][j];
						if(debug)System.out.printf("update emission,nume=%s\n",nume);
					}
				}
				for(int t = 0; t < table.length - 1; t++){ // 0 to T-1
					//System.out.printf("t=%d,j=%d\n",t,j);
					denom += table[t][0][j] + table[t][1][j];
				}
				//emission[j][k] = nume / denom;
				if(debug)System.out.printf("update emission,nume=%s,denom=%s\n",nume,denom);
				if(j==0){ //update space emission prob 
					spaceMap.put(chArr[k],nume/denom);
				}
				else{
					charMap.put(chArr[k],nume/denom);
				}
			}// end k loop
		}//end j loop
	}
	
	public double getTrellis() throws IOException{
		if(debug) System.out.println("getTrellis");
		int i=0;
		double factor = 0D;
		double C = 0D;
		while(buf[i] != '\0'){
			//System.out.println("buf[i]=="+ buf[i]);
			if(buf[i] == ' ' || (buf[i] <= 'Z' && buf[i] >= 'A')){
			String s = String.valueOf(buf[i]);
			if(buf[i] == ' ') s = "SPACE";
			//start state
			if(i==0){
				if(s.equals("SPACE")) {
					s1 = 1D; // start with Space 
					s2 = 0D;
				}
				else{ // start with non space
					s1 = 0D;
					s2 = 1D;
				}
			}
			else{
				double temp1 = s1; // store s1 to a temp variable
				double temp2 = s2; // store s2 to a temp variable
				//update s1, s2
				//update s1,sum over all paths that point to s1, s1 is SPACE.
				s1 = (temp1 * transMatrix[0][0] + temp2 * transMatrix[1][0]) * spaceMap.get(s);
				s2 = (temp1 * transMatrix[0][1] + temp2 * transMatrix[1][1]) * charMap.get(s);
				if(trellis)System.out.printf("00=%s,%s,%s,%s\n",transMatrix[0][0],transMatrix[1][0],transMatrix[0][1],transMatrix[1][1]);
				if(trellis)System.out.printf("%s,%s\n",spaceMap.get(s),charMap.get(s));
			}
			
			C = s1+s2;
			if(trellis)System.out.printf("C=%s,s1=%s,s2=%s\n",C,s1,s2);
			factor += Math.log(C);
			s1 = s1/C;
			s2 = s2/C;
			
			if(trellis)System.out.printf("s=%s,s1=%s,s2=%s\n",s,s1,s2);
			}//end if
			i++;
		}//end while
		//How do you calculate the per-character-log-likelihood (1/T*log(p(o1....o_T|\lambda)))?
		double sum = Math.log(s1+s2) + factor;
		System.out.println("FB pc-ll="+sum / i);
		return sum/i;
	}
	
	public static void ptrTrans(){
		for(int i=0;i<2;i++){
			for(int j=0;j<2;j++){
				System.out.printf("i,=%d,j=%d,transMatrix[i][j]=%s\n",i,j,transMatrix[i][j]);
			}
		}
	}
	public static void ptrEmission(){
		System.out.println("state1:");
		for(int k=0;k<27;k++){
			System.out.printf("%s\t%s\n",chArr[k],spaceMap.get(chArr[k]) );
		}
		System.out.println("state2:");
		for(int k=0;k<27;k++){
			System.out.printf("%s\t%s\n",chArr[k],charMap.get(chArr[k]) );
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
import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;


public class ForwardAlg {
	private int BUF_SIZE = 30000;
	public double pcll = 0D;
	public Double[] factors = new Double[BUF_SIZE];
	private static Double s1 = 1D;
	private static Double s2 = 0D;
	private Map<String,Double> spaceMap;
	private Map<String,Double> charMap;
	private double[][] transMatrix = {{0.1,0.9},{0.2,0.8}};  // space->space: ; char->space:0.2, char->char:0.8
	static String[] chArr=new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
		"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z","SPACE"};
	
	public ForwardAlg(){
		spaceMap = new HashMap<String, Double>();
		charMap = new HashMap<String, Double>();
	}
	
	public ForwardAlg(double[][] matrix, Map<String, Double> map1, Map<String, Double> map2){
		spaceMap = new HashMap<String, Double>();
		charMap = new HashMap<String, Double>();
		this.spaceMap = map1;
		this.charMap = map2;
		this.transMatrix = matrix;
	}

	public static void main(String[] args) throws IOException{
		ForwardAlg ins = new ForwardAlg();
		ins.getTrans();
		ins.getEmission();
		ins.ptrTrans();
		ins.ptrEmission();
		ins.getTrellis();
		return;
	}
	
	public ArrayList<Cell> getTrellis() throws IOException{
		File dataFile = new File(System.getProperty("user.dir") + "/train.txt");
		BufferedReader br = new BufferedReader(new FileReader(dataFile));
		char[] buf = new char[BUF_SIZE];
		
		br.read(buf);
		br.close();
		for(int i=BUF_SIZE-1;i>=1;i--){
			buf[i]=buf[i-1];
		}
		 
		ArrayList<Cell> res = new ArrayList<Cell>();
		double factor = 0D;
		double C = 0D;
		factors[0]=(s1+s2);
		res.add(new Cell("", s1, s2));
		int T=0;
		int i=1; //char index start from 1
		while(buf[i] != '\0'){
			/*
			if(buf[i]=='\n') {
				T = i-1;
				break; 
				}
			*/
			//System.out.printf("i=%d,buf[i]=[%s]\n",i, buf[i]);
			
			if(buf[i] == ' ' || (buf[i] <= 'Z' && buf[i] >= 'A')){
			String s = String.valueOf(buf[i]);
			if(buf[i] == ' ') s = "SPACE";
			//start state
				double temp1 = s1; // store s1 to a temp variable
				double temp2 = s2; // store s2 to a temp variable
				//update s1, s2
				//update s1,sum over all paths that point to s1, s1 is SPACE.
				s1 = (temp1 * transMatrix[0][0] + temp2 * transMatrix[1][0]) * spaceMap.get(s);
				s2 = (temp1 * transMatrix[0][1] + temp2 * transMatrix[1][1]) * charMap.get(s);
			//System.out.printf("s1=%s,s2=%s\n",s1,s2);
			
			C = s1+s2;
			factors[i] = C;
			factor += Math.log(C);
			//System.out.printf("i=%d,factors[i]=%s,factor=%s\n",i,factors[i],factor);
			s1 = s1/C;
			s2 = s2/C;
			
			res.add(new Cell(s, s1, s2));
			//System.out.printf("s=%s,s1=%s,s2=%s\n",s,s1,s2);
			}//end if
			i++; //T=i;
		}//end while
		T = i-1;
		//How do you calculate the per-character-log-likelihood (1/T*log(p(o1....o_T|\lambda)))?
		double sum = factor + Math.log(s2) ;
		System.out.printf("forward pc-ll=%s,s1=%s,s2=%s,factor=%s,T=%d,sum=%s,C=%s\n",sum / T,s1,s2,factor,T,sum,C);
		this.pcll = sum / T;
		
		return res;
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
	
	public void ptrTrans(){
		for(int i=0;i<2;i++){
			for(int j=0;j<2;j++){
				System.out.printf("i,=%d,j=%d,transMatrix[i][j]=%s\n",i,j,transMatrix[i][j]);
			}
		}
	}
	public void ptrEmission(){
		System.out.println("state1:");
		for(int k=0;k<27;k++){
			System.out.printf("%s\t%s\n",chArr[k],spaceMap.get(chArr[k]) );
		}
		System.out.println("state2:");
		for(int k=0;k<27;k++){
			System.out.printf("%s\t%s\n",chArr[k],charMap.get(chArr[k]) );
		}
		
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

import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;


public class NaturalModel {
	private static int BUF_SIZE = 30000;
	
	public static void main(String[] args) throws IOException{
		NaturalModel ins = new NaturalModel();
		File dataFile = new File(System.getProperty("user.dir") + "/train.txt");
		BufferedReader br = new BufferedReader(new FileReader(dataFile));
		char[] buf = new char[BUF_SIZE];
		
		br.read(buf);
		br.close();
		int i=0;
		while(buf[i] != '\0'){
			System.out.println(buf[i]);
			i++;
		}
	}
	
}import java.io.File;
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
