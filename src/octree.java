import java.io.Serializable;
import java.util.*;

// IN OCTREE IF A VALUE IS EQUAL TO THE MAX OF THE REGION WE PUT IT INTO THAT REGION
// IN OCTREE IF A VALUE IS EQUAL TO THE MAX OF THE REGION WE PUT IT INTO THAT REGION
// IN OCTREE IF A VALUE IS EQUAL TO THE MAX OF THE REGION WE PUT IT INTO THAT REGION
// IN OCTREE IF A VALUE IS EQUAL TO THE MAX OF THE REGION WE PUT IT INTO THAT REGION

public class octree implements Serializable{
    node root;
    Hashtable<String,String> htblRefernces;
    int maxKey = 0;
    public void insert(Object x, Object y, Object z, String ref) {
        if(x instanceof String)
            x = ((String)x).toLowerCase();
        if(y instanceof String)
            y = ((String)y).toLowerCase();
        if(z instanceof String)
            z = ((String)z).toLowerCase();
        for(String keys: htblRefernces.keySet()){   //search in the references table for the input reference
            if(htblRefernces.get(keys).equals(ref)){ //if the reference found in the table insert then leave
                root.insert(x, y, z, keys);
                return;
            }
        }//if not, this means that the ref is new to the index
        htblRefernces.put(maxKey +"",ref); //add the reference to the table
        root.insert(x, y, z, maxKey +""); //insert in the root
        maxKey++; //add the key max value
    }

    public Vector<Object> delete(Object x, Object y, Object z, boolean partial) {
        Vector<Object> ref = root.delete(x, y, z,partial); //get the references of the deleted records
        Vector<Object> ref2 = new Vector<>(); //make a new vector to get the corresponding page reference
        for(Object o: ref){
            ref2.add(htblRefernces.get((String) o));//add the corresponding reference
        }
        return ref2;
    }

    public void deleteOnce(Object x, Object y, Object z, String ref){
        for(String keys: htblRefernces.keySet()){
            if(htblRefernces.get(keys).equals(ref)){ //search in the table for the key of the page reference
                root.deleteOnce(x,y,z,keys); //delete it from the root
            }
        }
    }

    public Vector<Object> search(Object x, Object y, Object z, boolean partial) { //same as delete(Object x, Object y, Object z, boolean partial)
        Vector<Object> ref = root.search(x, y, z,partial);
        Vector<Object> ref2 = new Vector<>();
        for(Object o: ref){
            ref2.add(htblRefernces.get((String) o));
        }
        return ref2;
    }
    public octree(Object min_x,Object max_x,Object min_y,Object max_y,Object min_z,Object max_z){
        if(min_x instanceof String){
         min_x = ((String)min_x).toLowerCase();
         max_x = ((String)max_x).toLowerCase();
        }
        if(min_y instanceof String){
            min_y = ((String)min_y).toLowerCase();
            max_y = ((String)max_y).toLowerCase();
        }
        if(min_z instanceof String){
            min_z = ((String)min_z).toLowerCase();
            max_z = ((String)max_z).toLowerCase();
        }
        root=new node(min_x,max_x,min_y,max_y,min_z,max_z);
        htblRefernces = new Hashtable<>();
    }
    public void deleteRefFromHtbl(String ref){
        String key = "";
        for(String keys: htblRefernces.keySet()){
            if(htblRefernces.get(keys).equals(ref)){ //search in the table for the key of the page reference
                key = (keys); //when found delete from the table
            }
        }
        htblRefernces.remove(key);
    }
    public void updateHtbl(Vector<String>pages){
        int i = 0;
        Set<String> keys = htblRefernces.keySet();
        for(int j  = 0; j<maxKey;j++){
            if(keys.contains(j+"")){
                htblRefernces.put(j+"",pages.get(i)); //update the table pages references to be as the given vector
                i++;
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        drawTree(sb, root, 0);
        return sb.toString();
    }
    public void update(Object x,Object y,Object z,String ref,Object xNew,Object yNew,Object zNew,String refNew){
        for(String keys: htblRefernces.keySet()){
            if(htblRefernces.get(keys).equals(ref)){
                root.deleteOnce(x,y,z,keys);
            }
        }

        for(String keys: htblRefernces.keySet()){
            if(htblRefernces.get(keys).equals(refNew)){
                root.insert(xNew,yNew,zNew,keys);
                return;
            }
        }
        htblRefernces.put(maxKey +"",refNew);
        root.insert(xNew,yNew,zNew, maxKey +"");
        maxKey++;
    }
    public void updateRef(Object x,Object y,Object z,String ref,String refNew){ //updateRef is a method that takes the new reference and update it automatically in the tree by giving it the new reference
        String newRef = " ";
        for(String keys: htblRefernces.keySet()){
            if(htblRefernces.get(keys).equals(refNew)){
                newRef = keys; //search for the new Reference in the hashtable if found put in string newRef
            }
        }
        if(newRef.equals(" ")){
            htblRefernces.put(maxKey +"",refNew); //if the new Ref not found in the htbl that means that the reference is new, so we need to add to the htbl
            newRef = maxKey +""; //put the new key on the newRef
            maxKey++;
        }
        for(String keys: htblRefernces.keySet()){
            if(htblRefernces.get(keys).equals(ref)){ //search for the old reference in the htbl and use key in method updateRef of the root node
                root.updateRef(x,y,z,keys,newRef);
            }
        }
    }
    public Vector<String> searchSelect(Object x, Object y, Object z, String xOp , String yOp,String zOp){
        Vector<String> ref = root.searchSelect(x,y,z,new String[]{xOp,yOp,zOp});
        Vector<String> ref2 = new Vector<>();
        for(String s: ref){
            ref2.add(htblRefernces.get(s));
        }
        return ref2;
    }
    private void drawTree(StringBuilder sb, node currentNode, int level) {
        if (currentNode == null) {
            return;
        }

        sb.append("  ".repeat(level));
        sb.append("Node ");
        sb.append(level);
        sb.append(": ");

        if (currentNode.data.size() > 0) {
            sb.append("minx =" +currentNode.min_x+" | "+"maxx =" +currentNode.max_x+" | "+"miny ="  +currentNode.min_y+" | "+"maxy =" +currentNode.max_y+" | "+"minz =" + currentNode.min_z+" | "+"maxz =" +currentNode.max_z+" ||||| ");
            sb.append(currentNode.data.toString());
            sb.append(currentNode.duplicates.toString());
            sb.append("\n");
        } else {
            sb.append("Empty\n");
        }

        for (int i = 0; i < 8; i++) {
            drawTree(sb, currentNode.children[i], level + 1);
        }
    }

    public static void main(String[] args) {
        octree t = new octree("a","ZZZZZZZ",0,100,0,100);
        t.insert("ahmed",2,11,"mima");
        t.insert("ahmed",2,11,"kika");
        t.insert("ahmed",2,11,"noob");
        t.insert("kika",8,1,"kika");
        t.insert("maged",81,21,"kika");
        t.insert("safwat",22,41,"mima");
        t.insert("safwat",22,41,"mima");
        t.insert(new DBAppNull(),new DBAppNull(),new DBAppNull(),"mima");
        t.insert(new DBAppNull(),new DBAppNull(),new DBAppNull(),"kika");
        t.insert(new DBAppNull(),new DBAppNull(),new DBAppNull(),"damn");
        t.insert("damn",new DBAppNull(),9,"damn");
        System.out.println(t);
        t.deleteOnce("ahmed",2,11,"noob");
//        t.deleteOnce("ahmed",2,11,"mima");
       // System.out.println(t.delete(new DBAppNull(),new DBAppNull(),new DBAppNull(),false));
        System.out.println(t.htblRefernces);
        System.out.println(t);
    }
}

