import java.util.ArrayList;
import java.util.HashSet;

public class Node {
    public int id;
    public int parentId;
    public int[] data;
    public boolean isRoot=false;
    public String operation;
    public ArrayList<Node> children = new ArrayList<>();
    public boolean isInitialized = false;

    private HashSet<Integer> resultSet;
    
    public int[] getData(){
        if (operation == null) {
            return data;
        }

        // only calculate data if there was an operation specified
        if (data == null) {
            calcDataSetsRecursively();
            updateData();
        }

        return data;
    }

    private void updateData(){
        data = new int[resultSet.size()];
        int i=0;
        for (Integer val : resultSet) {
            data[i++]=val;
        }
    }

    private HashSet<Integer> calcDataSetsRecursively(){
        if (resultSet == null) {
            resultSet = new HashSet<>();

            if (data == null) {
                switch (operation) {
                    case "U":
                        for (Node child : children){
                            resultSet.addAll(child.calcDataSetsRecursively());
                        }
                        break;
                    case "I":
                        if (!children.isEmpty()){
                            resultSet = children.get(0).calcDataSetsRecursively();
                            for (int i = 1; i < children.size() ; i++) {
                                resultSet.retainAll(children.get(i).calcDataSetsRecursively());
                            }
                        }
                        break;
                }
                updateData();

            } else {
                for (int val : data) {
                    resultSet.add(val);
                }
            }
        }
        return resultSet;
    }

    public String toString(){
        return id + ", " + parentId;
    }
}
