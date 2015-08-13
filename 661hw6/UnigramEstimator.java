import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UnigramEstimator {
	private static String base = System.getProperty("user.dir");

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//System.out.println(Math.log(8) / Math.log(2));
		File train = new File(base + "/files/hw6-WSJ-2.tags");
		int tokenNum = 0;
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		
		try {
			FileReader reader = new FileReader(train);
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			
			while ((str = br.readLine()) != null) {
				String[] tags = str.trim().split(" ");
				tokenNum += tags.length;
				
	            for(int i = 0; i < tags.length; i++){
	            	if(!map.containsKey(tags[i]))
	            		map.put(tags[i], 1);
	            	else{
	            		int newNum = map.get(tags[i]);
	            		map.put(tags[i], newNum + 1);
	            	}
	            }
			}			
			br.close();
            reader.close();
            
            double all = 0;
            double perplexity = 0;
            int mapSize = map.size();
            
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                
                Integer num = (Integer)pairs.getValue();
                double likelihood = (double)num.intValue() / (double)tokenNum;
                all += likelihood * Math.log(likelihood) / Math.log(2);
                
                it.remove(); // avoids a ConcurrentModificationException
            }
            
            perplexity = Math.pow(2, -all);
            
            System.out.println(mapSize);
            System.out.println(tokenNum);
            System.out.println(all);
            System.out.println(perplexity);
            
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}