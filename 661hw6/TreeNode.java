import java.util.ArrayList;


public class TreeNode {
	private TreeNode yes;
	private TreeNode no;
	
	private ArrayList<Data> content;

	public TreeNode getYes() {
		return yes;
	}

	public void setYes(TreeNode yes) {
		this.yes = yes;
	}

	public TreeNode getNo() {
		return no;
	}

	public void setNo(TreeNode no) {
		this.no = no;
	}

	public ArrayList<Data> getContent() {
		return content;
	}

	public void setContent(ArrayList<Data> content) {
		this.content = content;
	}
}