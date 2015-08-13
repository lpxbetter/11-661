import java.io.BufferedReader;
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
