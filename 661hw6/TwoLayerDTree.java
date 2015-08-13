import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TwoLayerDTree {
	private static String base = System.getProperty("user.dir");
	private ArrayList<Data> data;
	private ArrayList<String> vocab;
	private static int SENTENCE_LENGTH = 8;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TwoLayerDTree test = new TwoLayerDTree();
		
		ArrayList<MI> res = new ArrayList<MI>();
		//System.out.println(test.getEntropy(test.getData()));
		ArrayList<MI> c1 = test.c1();
		ArrayList<MI> c2 = test.c2();
		ArrayList<MI> c3 = test.c3();
		ArrayList<MI> c4 = test.c4();
		
		res.addAll(c1);
		res.addAll(c2);
		res.addAll(c3);
		res.addAll(c4);
		
		System.out.println(test.getInfo(c1));
		System.out.println(test.getInfo(c2));
		System.out.println(test.getInfo(c3));
		System.out.println(test.getInfo(c4));
		
		
		double allInfo = test.getInfo(c1) * c1.size() 
				+ test.getInfo(c2) * c2.size() 
				+ test.getInfo(c3) * c3.size()
				+ test.getInfo(c4) * c4.size();
		
		System.out.println(allInfo / (c1.size() + c2.size() + c3.size() + c4.size()));
		
		Collections.sort(res, new Comparator<MI>(){
			public int compare(MI m1, MI m2){
				double mm = m1.mi - m2.mi;
				if(mm > 0)
					return 0;
				else
					return 1;
			}
		});
		
		for(int i = 0; i < Math.min(100,res.size()); i++){
			System.out.println(res.get(i).question+"\t"+ res.get(i).mi);
		}
		
		System.out.println("---------------------------------");
		
		for(int i = 0; i < Math.min(100,res.size()); i++){
			System.out.println(res.get(res.size() - i - 1).question + "\t"+ res.get(res.size() - i - 1).mi);
		}
	}

	public ArrayList<Data> getData() {
		return data;
	}

	public void setData(ArrayList<Data> data) {
		this.data = data;
	}

	public TwoLayerDTree() {
		data = new ArrayList<Data>();
		vocab = new ArrayList<String>();
		
		File train = new File(base + "/files/hw6-WSJ-1.tags");
		int indice = 0;
		
		try {
			FileReader reader = new FileReader(train);
			BufferedReader br = new BufferedReader(reader);
			String str = null;

			while ((str = br.readLine()) != null) {
				String[] tags = str.trim().split(" ");
				
				for (int i = 0; i < tags.length; i++) {
					if(!vocab.contains(tags[i]))
						vocab.add(tags[i]);
					
					Data temp = new Data(indice, tags[i]);
					//System.out.println(indice+"\t"+tags[i]);
					data.add(temp);
					indice++;
				}
			}
			System.out.println(vocab.size());
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
	
	public ArrayList<MI> c1(){
		ArrayList<MI> res = new ArrayList<MI>();	
		/*
		int index = -1;
		double max = Double.MIN_VALUE;
		*/
		
		for(int i = 0; i < vocab.size(); i++){
			TreeNode root = new TreeNode();
			root.setContent(data);
			
			TreeNode yes = new TreeNode();
			yes.setContent(new ArrayList<Data>());
			
			TreeNode no = new TreeNode();
			no.setContent(new ArrayList<Data>());			
			
			for(int j = 0; j < data.size(); j++){
				if(j != 0){
					if(data.get(j - 1).getTags().equals(vocab.get(i))){
						yes.getContent().add(data.get(j));
					}else{
						no.getContent().add(data.get(j));
					}
				}else
					no.getContent().add(data.get(0));
			}//split data
			
			int size = data.size();
			int size1 = yes.getContent().size();
			int size2 = no.getContent().size();
			
			double w1 = (double)size1 / (double)size;
			double w2 = (double)size2 / (double)size;
			
			double mi = getEntropy(data) - getEntropy(yes.getContent()) * w1 - getEntropy(no.getContent()) * w2;
			res.add(new MI("Is previous tag: " + vocab.get(i), mi));
			//System.out.println(mi);
			/*
			if(mi >max){
				max = mi;
				index = i;
			}
			//System.out.println(mi);
			 */
		}// traverse vocab end
		
		//System.out.println(vocab.get(index));
		return res;
	}
	
	public ArrayList<MI> c2(){
		ArrayList<MI> res = new ArrayList<MI>();
		
		for(int i = 0; i < vocab.size(); i++){
			for(int j = 0; j < vocab.size(); j++){
				TreeNode root = new TreeNode();
				root.setContent(data);
				
				TreeNode yes = new TreeNode();
				yes.setContent(new ArrayList<Data>());
				
				TreeNode no = new TreeNode();
				no.setContent(new ArrayList<Data>());
				
				for(int z = 0; z < data.size(); z++){
					if(z > 1){
						if(data.get(z - 2).getTags().equals(vocab.get(i)) && data.get(z - 1).getTags().equals(vocab.get(j))){
							yes.getContent().add(data.get(z));
						}else{
							no.getContent().add(data.get(z));
						}
					}else{
						no.getContent().add(data.get(z));
					}
				}
				
				int size = data.size();
				int size1 = yes.getContent().size();
				int size2 = no.getContent().size();
				
				double w1 = (double)size1 / (double)size;
				double w2 = (double)size2 / (double)size;
	
				double mi = getEntropy(data) - getEntropy(yes.getContent()) * w1 - getEntropy(no.getContent()) * w2;
				//System.out.println(mi);
				res.add(new MI("Is the previous sequence of two tags: " + vocab.get(i) + " " + vocab.get(j), mi));
			}
		}//traverse vocab end
		return res;
	}
	
	public ArrayList<MI> c3(){
		ArrayList<MI> res = new ArrayList<MI>();
		/*
		int index = -1;
		double max = Double.MIN_VALUE;
		*/
		
		for(int i = 0; i < vocab.size(); i++){
			TreeNode root = new TreeNode();
			root.setContent(data);
			
			TreeNode yes = new TreeNode();
			yes.setContent(new ArrayList<Data>());
			
			TreeNode no = new TreeNode();
			no.setContent(new ArrayList<Data>());			
			
			for(int j = 0; j < data.size(); j++){
				if(j != 0){
					boolean isContained = false;
					if(j < SENTENCE_LENGTH){
						for(int z = 0; z < j; z++){
							if(data.get(z).tags.equals(vocab.get(i))){
								isContained = true;
								break;
							}
						}
					}else{
						for(int z = j - SENTENCE_LENGTH; z < j; z++){
							if(data.get(z).tags.equals(vocab.get(i))){
								isContained = true;
								break;
							}
						}
					}
					if(isContained)
						yes.getContent().add(data.get(j));
					else
						no.getContent().add(data.get(j));
				}else
					no.getContent().add(data.get(0));
			}
			
			int size = data.size();
			int size1 = yes.getContent().size();
			int size2 = no.getContent().size();
			
			double w1 = (double)size1 / (double)size;
			double w2 = (double)size2 / (double)size;
			
			double mi = getEntropy(data) - getEntropy(yes.getContent()) * w1 - getEntropy(no.getContent()) * w2;
			res.add(new MI("Does the sentence contain tags: " + vocab.get(i), mi));
			/*
			if(mi >max){
				max = mi;
				index = i;
			}
			//System.out.println(mi);
			 */
		}
		
		//System.out.println(vocab.get(index));
		return res;		
	}
	
	public ArrayList<MI> c4(){
		ArrayList<MI> res = new ArrayList<MI>();
		
		String[][] clusters = {{"JJ", "JJR", "JJS"}, {"RB", "RP", "RBR"}, {"DT"}, {"CD"}, {"NNS", "NN", "NNP"}, {"MD", "CC", "IN"}, {"VBN", "VBG", "VBD", "VBP", "VBZ", "VB"}, {"PRP", "PRP$"}, {"POS"}, {"WP", "WDT", "WRB"}, {"TO", "EX"}};
		
		for(int i = 0; i < clusters.length; i++){			
			TreeNode root = new TreeNode();
			root.setContent(data);
			
			TreeNode yes = new TreeNode();
			yes.setContent(new ArrayList<Data>());
			
			TreeNode no = new TreeNode();
			no.setContent(new ArrayList<Data>());
			
			for(int j = 0; j < data.size(); j++){
				if(j == 0)
					no.getContent().add(data.get(j));
				else{
					boolean isContained = false;
					
					for(int z = 0; z < clusters[i].length; z++){
						if(data.get(j - 1).tags.equals(clusters[i][z])){
							isContained = true;
							break;
						}
					}
					if(isContained)
						yes.getContent().add(data.get(j));
					else
						no.getContent().add(data.get(j));
				}
			}
			
			int size = data.size();
			int size1 = yes.getContent().size();
			int size2 = no.getContent().size();
			
			double w1 = (double)size1 / (double)size;
			double w2 = (double)size2 / (double)size;
			
			double mi = getEntropy(data) - getEntropy(yes.getContent()) * w1 - getEntropy(no.getContent()) * w2;
			
			String question = "Is the previous tag belongs to ";
			
			for(int j = 0; j < clusters[i].length; j++){
				question += (" " + clusters[i][j]);
			}
			
			res.add(new MI(question, mi));
		}//end traverse clusters
				
		return res;
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
	
	public double getInfo(ArrayList<MI> list){
		double res = 0;
		
		for(int i = 0; i < list.size(); i++){
			res += list.get(i).mi;
		}
		
		return res / list.size();
	}
}