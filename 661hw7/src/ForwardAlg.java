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
