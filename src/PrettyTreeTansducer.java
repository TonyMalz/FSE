import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the main PrettyTreeTransducer class. It is used to transduce a tree.
 *
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
		String tree;
		try {
			Node root = parseInput(input);
			tree = getOutputTreeFormatted(root);
		} catch (ParseException e) {
			tree = e.getMessage();
		}

		return tree;
	}

	private String getOutputTreeFormatted(Node root) {
		return getOutputTreeRec(root, 0);
	}

	private String getOutputTreeRec(Node node, int level) {
		String childPrefix = "+- ";
		String linePrefix = "";

		int counter = level;
		while (--counter >= 0) {
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

		StringBuilder out = new StringBuilder(String.valueOf(node.id) + " : "
				+ dataOrOp + " -> " + result + "\n");
		for (Node child : node.children) {
			out.append(linePrefix + "|\n");
			out.append(linePrefix + childPrefix
					+ getOutputTreeRec(child, level + 1));
		}

		return out.toString();
	}

	private String dataToString(int[] data) {
		String out = "{";
		if (data != null) {
			for (int i = 0; i < data.length; i++) {
				out += data[i];
				if (i + 1 < data.length) {
					out += ", ";
				}
			}
		}
		return out + "}";
	}

	private Node parseInput(ArrayList<String> input) throws ParseException {
		return parseInput(input, false);
	}

	private Node parseInput(ArrayList<String> input, boolean printDebug)
			throws ParseException {
		// expected format: <Int>,<Int>|<whitespace>,<IntList>|<Operation>
		Pattern regex = Pattern
				.compile("(\\d+),((\\d+)|(\\s*)),(((\\d+,?)+)|(U|I))");
		StringBuilder debugStr = new StringBuilder();
		Matcher m;
		int id, parentId;
		boolean isRoot;
		Node node, root = null;
		HashMap<Integer, Node> parsedNodes = new HashMap<>();

		// Parse tokens
		boolean foundRoot = false;
		for (String line : input) {
			node = new Node();
			m = regex.matcher(line);
			if (m.find()) {
				debugStr.append(m.group(0) + "\n");
				debugStr.append("ID: " + m.group(1) + "\n");
				id = Integer.valueOf(m.group(1));
				node.id = id;

				debugStr.append("PID: " + m.group(3) + "\n");
				if (m.group(3) != null) {
					parentId = Integer.valueOf(m.group(3));
					node.parentId = parentId;
				}

				debugStr.append("ROOT: " + m.group(4) + "\n");
				isRoot = m.group(4) != null;
				node.isRoot = isRoot;

				debugStr.append("DATA: " + m.group(6) + "\n");
				if (m.group(6) != null) {
					String[] dataValues = m.group(6).split(",");
					int[] values = new int[dataValues.length];
					for (int i = 0; i < dataValues.length; i++) {
						values[i] = Integer.valueOf(dataValues[i]);
					}
					node.setData(values);
				}

				debugStr.append("OP: " + m.group(8) + "\n");
				if (m.group(8) != null) {
					node.operation = m.group(8);
				}

				// checks //

				// check for duplicates
				if (parsedNodes.containsKey(node.id)) {
					Node n = parsedNodes.get(node.id);

					if (n.isInitialized == false) {
						// node was already referenced by another node
						// and thus created this dummy node and added itself to
						// it as a child node in a prior step
						node.children = n.children;
						parsedNodes.remove(node.id);
					} else {
						// TODO how to handle this case? overwrite old node/
						// throw error/ discard node?
						// System.out.println("INVALID TOKEN: node was already set: "
						// + line );
						throw new ParseException(
								"INVALID TOKEN: node with same id was already set: "
										+ line);
						// continue;
					}
				}

				// check if we already have a root node
				if (node.isRoot && foundRoot == true) {
					// TODO how to handle this case? overwrite old node/ throw
					// error/ discard node?
					// System.out.println("INVALID TOKEN: root was already set: "
					// + line );
					throw new ParseException(
							"INVALID TOKEN: only one root node is allowed: "
									+ line);
					// continue;
				} else if (node.isRoot) {
					foundRoot = true;
					root = node;
				} else {
					// now determine parent node since this is not a root node
					// but first check for cyclic references
					if (node.id == node.parentId) {
						// TODO how to handle this case? throw error/ discard
						// node?
						// System.out.println("INVALID TOKEN: id == parent id: "
						// + line );
						throw new ParseException(
								"INVALID TOKEN: cyclic reference id == parent id in line: "
										+ line);
						// continue;
					}

					if (parsedNodes.containsKey(node.parentId)) {
						parsedNodes.get(node.parentId).children.add(node);
					} else {
						// create dummy parent
						Node parent = new Node();
						parent.id = node.parentId;
						parent.children.add(node);
						parent.isInitialized = false;
						parsedNodes.put(parent.id, parent);
					}
				}

				// no errors
				node.isInitialized = true;
				parsedNodes.put(node.id, node);
				debugStr.append("\n");
			} else {
				// invalid token
				// TODO how to handle this case? throw error/ discard node?
				// System.out.println("INVALID TOKEN: invalid format: " + line
				// );
				throw new ParseException(
						"INVALID TOKEN: invalid format in line: " + line);
				// continue;
			}
		}

		if (printDebug) {
			System.out.println(debugStr.toString());
		}

		if (foundRoot == false) {
			throw new ParseException(
					"PARSE ERROR: no root element was specified!");
		}

		for (Node parsedNode : parsedNodes.values()) {
			if (parsedNode.isInitialized == false) {
				// TODO how to handle this case? throw error/ discard node?
				// System.out.println("PARSE ERROR: found uninitialized node: "
				// + parsedNode);
				throw new ParseException(
						"PARSE ERROR: missing parent node with id: "
								+ parsedNode.id);
			}
		}

		return root;
	}

	public static void main(String[] args) {
		FileReaderImpl myFileReader = new FileReaderImpl();
		ArrayList<String> inputList = new ArrayList<String>();
		PrettyTreeTansducer treeTransducer = new PrettyTreeTansducer();

		// iterate over the arguments and print for each file the corresponding
		// tree
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
