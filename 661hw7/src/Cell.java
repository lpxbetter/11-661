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
}