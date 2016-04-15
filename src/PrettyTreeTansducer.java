import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This is the main PrettyTreeTransducer class. It is used to transduce a tree.
 * @author Johannes Gareis
 *
 */
public class PrettyTreeTansducer {
	
	/**
	 * Function to read a input file in and return the corresponding ASCII-style
	 * string of the tree
	 * 
	 * @param input
	 *            the tree as an string-array list
	 * @return an ASCII-formated, well evaluated tree as an string
	 */
	public String printTreePretty(ArrayList<String> input) {
		//input.add("20,10,I");
		String tree = null;
		Node root = parseInput(input);
		if (root != null) {
			tree = getOutputTreeFormatted(root);
		}
		return tree;
	}

	private String getOutputTreeFormatted(Node root){
		return getOutputRec(root,0);
	}

	private String getOutputRec(Node node, int level){
		String childPrefix = "+- ";
		String linePrefix = "";

		int counter = level;
		while(--counter >= 0){
			linePrefix += "|  ";
		}

		String dataOrOp;
		String result;
		if (node.operation != null) {
			dataOrOp = node.operation;
			result = dataToString(node.getData());
		} else {
			dataOrOp = dataToString(node.getData());
			result = dataOrOp;
		}

		String out = String.valueOf(node.id) + " : " + dataOrOp + " -> " + result + "\n";
		for(Node child : node.childs) {
			out += linePrefix + "|\n";
			out += linePrefix + childPrefix + getOutputRec(child,level+1);
		}

		return out;
	}

	private String dataToString(int[] data){
		String out="{";
		if (data != null) {
			for (int i = 0; i < data.length; i++) {
				out += data[i];
				if (i + 1 < data.length) {
					out += " ,";
				}
			}
		}
		return out + "}";
	}

	private Node parseInput(ArrayList<String> input) {
		// expected format: <Int>,<Int>|<whitespace>,<IntList>|<Operation>
		Pattern regex = Pattern.compile("(\\d+),((\\d+)|(\\s*)),(((\\d+,?)+)|(U|I))");

		Matcher m;
		int id,parentId;
		boolean isRoot;
		Node node,root=null;
		HashMap<Integer,Node> parsedNodes = new HashMap<>();

		// Parse tokens
		boolean foundRoot = false;
		for(String line : input){
			node = new Node();
			m = regex.matcher(line);
			if (m.find()){
				System.out.println(m.group(0));

				System.out.println("ID: " + m.group(1));// id
				id = Integer.valueOf(m.group(1));
				node.id = id;
				//System.out.println(m.group(2));// pid or root
				System.out.println("PID: " + m.group(3));// pid
				if (m.group(3) != null) {
					parentId = Integer.valueOf(m.group(3));
					node.parentId = parentId;
				}

				System.out.println("ROOT: " + m.group(4));// root
				isRoot = m.group(4) != null;
				node.isRoot = isRoot;

				//System.out.println("ID: " + m.group(5));// data or operation
				System.out.println("DATA: " + m.group(6));// data
				if (m.group(6) != null) {
					String[] dataValues = m.group(6).split(",");
					int[] values = new int[dataValues.length];
					for (int i = 0; i < dataValues.length; i++) {
						values[i] = Integer.valueOf(dataValues[i]);
					}
					node.data = values;
				}
				//System.out.println("ID: " + m.group(7));// last data elem
				System.out.println("OP: " + m.group(8));// operation
				if (m.group(8) != null) {
					node.operation = m.group(8);
				}

				// checks //

				// check for duplicates
				if (parsedNodes.containsKey(node.id)) {
					Node n = parsedNodes.get(node.id);

					if (n.isInitialized == false) {
						// node was already referenced by another node
						// and thus created this dummy node and added itself to it as a child node in a prior step
						node.childs = n.childs;
						parsedNodes.remove(node.id);
					} else {
						//TODO how to handle this case? overwrite old node/ throw error/ discard node?
						System.out.println("INVALID TOKEN: node was already set: " + line );
						continue;
					}
				}

				// check if we already have a root node
				if (node.isRoot && foundRoot == true){
					//TODO how to handle this case? overwrite old node/ throw error/ discard node?
					System.out.println("INVALID TOKEN: root was already set: " + line );
					continue;
				} else if (node.isRoot) {
					foundRoot = true;
					root = node;
				} else {
					// determine parent node since this is not a root node

					//check for cyclic references
					if (node.id == node.parentId){
						System.out.println("INVALID TOKEN: id == parent id: " + line );
						continue;
					}

					if (parsedNodes.containsKey(node.parentId)) {
						parsedNodes.get(node.parentId).childs.add(node);
					} else {
						// create dummy parent
						Node parent = new Node();
						parent.id = node.parentId;
						parent.childs.add(node);
						parent.isInitialized = false;
						parsedNodes.put(parent.id, parent);
					}
				}

				// no errors
				node.isInitialized = true;
				parsedNodes.put(node.id,node);

				System.out.println();
			} else {
				// invalid token
				System.out.println("INVALID TOKEN: invalid format: " + line );
			}
		}

		if (foundRoot == false) {
			System.out.println("PARSE ERROR: no root element specified");
		}

		for (Node parsedNode : parsedNodes.values()) {
			if (parsedNode.isInitialized == false){
				System.out.println("PARSE ERROR: found uninitialized node: " + parsedNode);
			}
		}

		return root;
	}

	
	public static void main(String[] args) {
		FileReaderImpl myFileReader = new FileReaderImpl();
		ArrayList<String> inputList = new ArrayList<String>();
		PrettyTreeTansducer treeTransducer = new PrettyTreeTansducer();

		// iterate over the arguments and print for each file the corresponding tree
		for (int i = 0; i < args.length; i++) {
			System.out.println("Printing Tree for File " + args[i]);
			// read the file in
			inputList = myFileReader.readFileToStringList(args[i]);
			// get the tree as an ASCII string
			String result = treeTransducer.printTreePretty(inputList);
			// print the tree to stout
			System.out.println(result);
		}

	}

}
