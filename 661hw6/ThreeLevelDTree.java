import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ThreeLevelDTree {
	private static String base = System.getProperty("user.dir");
	private ArrayList<Data> data;
	private ArrayList<String> vocab;
	
	public static void main(String[] args){
		ThreeLevelDTree test = new ThreeLevelDTree();
		test.start();
	}
	
	public ThreeLevelDTree() {
		data = new ArrayList<Data>();
		vocab = new ArrayList<String>();

		File train = new File(base + "/files/hw6-WSJ-2.tags");
		int indice = 0;

		try {
			FileReader reader = new FileReader(train);
			BufferedReader br = new BufferedReader(reader);
			String str = null;

			while ((str = br.readLine()) != null) {
				String[] tags = str.trim().split(" ");

				for (int i = 0; i < tags.length; i++) {
					if (!vocab.contains(tags[i]))
						vocab.add(tags[i]);

					Data temp = new Data(indice, tags[i]);
					data.add(temp);
					indice++;
				}
			}
			// System.out.println(vocab.size());
			br.close();
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start() {
		TreeNode root = new TreeNode();
		root.setContent(data);

		TreeNode yes = new TreeNode();
		yes.setContent(new ArrayList<Data>());

		TreeNode no = new TreeNode();
		no.setContent(new ArrayList<Data>());
		
		ArrayList<String> ss = new ArrayList<String>();
		ss.add("NNS");
		ss.add("NN");
		ss.add("NNP");

		for (int j = 0; j < data.size(); j++) {
			if (j == 0)
				no.getContent().add(data.get(j));
			else {
				if(ss.contains(data.get(j - 1).tags))
					yes.getContent().add(data.get(j));
				else
					no.getContent().add(data.get(j));
			}
		}
		
		TreeNode yesYes = new TreeNode();
		yesYes.setContent(new ArrayList<Data>());
		
		TreeNode yesNo = new TreeNode();
		yesNo.setContent(new ArrayList<Data>());
		
		for(int i = 0; i < yes.getContent().size(); i++){
			if (i == 0)
				yesNo.getContent().add(data.get(i));
			else {
				if(data.get(i - 1).tags.equals("DT"))
					yesYes.getContent().add(data.get(i));
				else
					yesNo.getContent().add(data.get(i));
			}
		}		
		
		TreeNode noYes = new TreeNode();
		noYes.setContent(new ArrayList<Data>());
		TreeNode noNo = new TreeNode();
		noNo.setContent(new ArrayList<Data>());
		
		for(int i = 0; i < yes.getContent().size(); i++){
			if (i == 0)
				noNo.getContent().add(data.get(i));
			else {
				if(data.get(i - 1).tags.equals("DT"))
					noYes.getContent().add(data.get(i));
				else
					noNo.getContent().add(data.get(i));
			}
		}
		int size = data.size();
		int size1 = yesYes.getContent().size();
		int size2 = yesNo.getContent().size();
		int size3 = noYes.getContent().size();
		int size4 = noNo.getContent().size();
		
		double w1 = (double) size1 / (double) size;
		double w2 = (double) size2 / (double) size;
		double w3 = (double) size3 / (double) size;
		double w4 = (double) size4 / (double) size;
		
		double all = -(w1 * getEntropy(yesYes.getContent()) + w2 * getEntropy(yesNo.getContent()) + w3 * getEntropy(noYes.getContent()) + w4 * getEntropy(noNo.getContent()));
		
		double perplexity = Math.pow(2, -all);
		
		System.out.println(all);
		System.out.println(perplexity);
	}
	
	public double getEntropy(ArrayList<Data> list){
		double entropy = 0;
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		
		for(int i = 0; i < list.size(); i++){
			if(map.containsKey(list.get(i).tags)){
				int num = map.get(list.get(i).tags);
				map.put(list.get(i).getTags(), num + 1);
			}else{
				map.put(list.get(i).getTags(), 1);
			}
		}

        int mapSize = map.size();
		
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            
            Integer num = (Integer)pairs.getValue();
            double likelihood = (double)num.intValue() / (double)list.size();
            entropy += likelihood * Math.log(likelihood) / Math.log(2);
            
            it.remove(); // avoids a ConcurrentModificationException
        }
		
		return -entropy;
	}
}