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
