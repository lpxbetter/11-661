
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
	
}