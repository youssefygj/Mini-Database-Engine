import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

public class
node implements Serializable {
    Object min_x;
    Object max_x;
    Object min_y;
    Object max_y;
    Object min_z;
    Object max_z;
    int n;
    node[] children= new node[8];
    Vector<Vector<Object>> data;
    Vector<Vector<Vector<Object>>> duplicates;
    public node(Object min_x,Object max_x,Object min_y,Object max_y,Object min_z,Object max_z){
        this.min_x=min_x;
        this.max_x=max_x;
        this.min_y=min_y;
        this.max_y=max_y;
        this.min_z=min_z;
        this.max_z=max_z;
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("src/resources/DBApp.config"));
        } catch (IOException e) {
        }
        n = Integer.parseInt(properties.getProperty(String.valueOf(properties.stringPropertyNames().toArray()[1])));
        data=new Vector<>();
        duplicates = new Vector<Vector<Vector<Object>>>();
    }
    public void insert(Object x,Object y,Object z,String ref){
        if(data.size()==n) {
            //if needs to be split
            if (children[0] == null) {
                //if it still hasnt been split yet
                Object midx=null;
                Object midy=null;
                Object midz=null;
                //deciding middle
                if (min_x instanceof Integer) {
                    midx=((Integer)max_x+(Integer) min_x)/2;
                } else if (min_x instanceof Double) {
                    midx=((Double)max_x+(Double) min_x)/2;
                } else if (min_x instanceof String) {

                    midx=returnMiddleString((String)max_x,(String)min_x);

                } else {
                    long millisecondsBetween = Math.abs(((Date)(min_x)).getTime() - ((Date)(max_x)).getTime());
                    long millisecondsToAdd = millisecondsBetween / 2;
                    midx = new Date(((Date)(min_x)).getTime() + millisecondsToAdd);
                }
                if (min_y instanceof Integer) {
                    midy=((Integer)max_y+(Integer) min_y)/2;
                } else if (min_y instanceof Double) {
                    midy=((Double)max_y+(Double) min_y)/2;
                } else if (min_y instanceof String) {

                    midy=returnMiddleString((String)max_y,(String)min_y);

                } else {
                    long millisecondsBetween = Math.abs(((Date)(min_y)).getTime() - ((Date)(max_y)).getTime());
                    long millisecondsToAdd = millisecondsBetween / 2;
                    midy = new Date(((Date)(min_y)).getTime() + millisecondsToAdd);
                }
                if (min_z instanceof Integer) {
                    midz=((Integer)max_z+(Integer) min_z)/2;
                } else if (min_z instanceof Double) {
                    midz=((Double)max_z+(Double) min_z)/2;
                } else if (min_z instanceof String) {

                    midz=returnMiddleString((String)max_z,(String)min_z);

                } else {
                    long millisecondsBetween = Math.abs(((Date)(min_z)).getTime() - ((Date)(max_z)).getTime());
                    long millisecondsToAdd = millisecondsBetween / 2;
                    midz = new Date(((Date)(min_z)).getTime() + millisecondsToAdd);
                }
                children[0]= new node(min_x,midx,min_y,midy,min_z,midz);
                children[1]= new node(min_x,midx,min_y,midy,midz,max_z);
                children[2]= new node(min_x,midx,midy,max_y,min_z,midz);
                children[3]= new node(min_x,midx,midy,max_y,midz,max_z);
                children[4]= new node(midx,max_x,min_y,midy,min_z,midz);
                children[5]= new node(midx,max_x,min_y,midy,midz,max_z);
                children[6]= new node(midx,max_x,midy,max_y,min_z,midz);
                children[7]= new node(midx,max_x,midy,max_y,midz,max_z);
                int index=this.indexchild(x,y,z).get(0);
                children[index].insert(x,y,z,ref);
                //inserting the old values in the node before splitting into the children
                for(int i=0;i<data.size();i++){
                    for(int j=0;j<duplicates.size();j++){
                        if(duplicates.get(j).get(0).get(0).equals(data.get(i).get(0))&&duplicates.get(j).get(0).get(1).equals(data.get(i).get(1))&&duplicates.get(j).get(0).get(2).equals(data.get(i).get(2))){
                            for(int k=0;k<duplicates.get(j).size();k++){
                                index=this.indexchild(data.get(i).get(0),data.get(i).get(1),data.get(i).get(2)).get(0);
                                children[index].insert(data.get(i).get(0),data.get(i).get(1),data.get(i).get(2),(String)duplicates.get(j).get(k).get(3));
                            }
                        }
                    }
                    index=this.indexchild(data.get(i).get(0),data.get(i).get(1),data.get(i).get(2)).get(0);
                    children[index].insert(data.get(i).get(0),data.get(i).get(1),data.get(i).get(2),(String)data.get(i).get(3));
                }
            } else {
                //if it has children already
                int index=this.indexchild(x,y,z).get(0);
                children[index].insert(x,y,z,ref);
            }
        }
        else {
            //if it doesnt need to be split
            Vector<Object> tobeadded= new Vector<>();
            tobeadded.add(x);
            tobeadded.add(y);
            tobeadded.add(z);
            tobeadded.add(ref);
            boolean dup=false;
            for(int i=0;i<data.size();i++){
                //searching if its a duplicate value
                boolean found=false;
                if(data.get(i).get(0).equals(tobeadded.get((0)))&&data.get(i).get(1).equals(tobeadded.get((1)))&&data.get(i).get(2).equals(tobeadded.get((2)))){
                    dup=true;
                    for(int j=0;j<duplicates.size();j++){
                        //searching if there is a list for that duplicate already
                        if(duplicates.get(j).get(0).get(0).equals(tobeadded.get(0)) && duplicates.get(j).get(0).get(1).equals(tobeadded.get(1))&& duplicates.get(j).get(0).get(2).equals(tobeadded.get(2))){
                            duplicates.get(j).add(tobeadded);
                            found = true;
                        }
                    }
                    if(!found){
                        //no list so i need to create the list my self
                        Vector<Vector<Object>> tmp=new Vector<>();
                        tmp.add(tobeadded);
                        duplicates.add(tmp);
                    }
                }
            }
            //if it isnt a duplicate then i need to insert it into the data normally else it will only be inserted in the duplicate list
            if(!dup)
            data.add(tobeadded);
        }
    }
    static String returnMiddleString(String S, String T)
    {
        // Stores the base 26 digits after addition
        int N= Math.min(S.length(),T.length());
        S=S.toLowerCase();
        T=T.toLowerCase();
        int[] a1 = new int[N + 1];
        for (int i = 0; i < N; i++) {
            a1[i + 1] = (int)S.charAt(i) - 97
                    + (int)T.charAt(i) - 97;
        }
        // Iterate from right to left
        // and add carry to next position
        for (int i = N; i >= 1; i--) {
            a1[i - 1] += (int)a1[i] / 26;
            a1[i] %= 26;
        }

        // Reduce the number to find the middle
        // string by dividing each position by 2
        for (int i = 0; i <= N; i++) {

            // If current value is odd,
            // carry 26 to the next index value
            if ((a1[i] & 1) != 0) {

                if (i + 1 <= N) {
                    a1[i + 1] += 26;
                }
            }

            a1[i] = (int)a1[i] / 2;
        }
        String res="";
        for(int i=1;i<a1.length;i++){
            res=res+(char)(a1[i]+97);
        }
        if(S.length()>T.length()){
            for(int i=T.length();i<S.length();i++){
                res=res+S.charAt(i);
            }
        }
        else{
            for(int i=S.length();i<T.length();i++){
                res=res+T.charAt(i);
            }
        }

        return res;
    }
    public Vector<Integer> indexchild(Object x, Object y, Object z){
        Vector<Integer> indices = new Vector<>();
        for (int i = 0; i < children.length; i++) {
            boolean flagx=false;
            boolean flagy=false;
            boolean flagz=false;
            boolean inserted = false;
            if(x instanceof DBAppNull){
                flagx=true;
            }
            if(y instanceof DBAppNull){
                flagy=true;
            }if(z instanceof DBAppNull){
                flagz=true;
            }
            if(!flagx)
                if (x instanceof Integer) {
                    if (((Integer) x).compareTo((Integer) children[i].min_x) > 0 && ((Integer) x).compareTo((Integer) children[i].max_x) <= 0) {
                        flagx = true;
                    }
                } else if (x instanceof Double) {
                    if (((Double) x).compareTo((Double) children[i].min_x) > 0 && ((Double) x).compareTo((Double) children[i].max_x) <= 0) {
                        flagx = true;
                    }
                } else if (x instanceof String) {
                    if (((String) x).compareToIgnoreCase((String) children[i].min_x) > 0 && ((String) x).compareToIgnoreCase((String) children[i].max_x) <= 0) {
                        flagx = true;
                    }
                } else {
                    if (((Date) x).compareTo((Date) children[i].min_x) > 0 && ((Date) x).compareTo((Date) children[i].max_x) <= 0) {
                        flagx = true;
                    }
                }

            if(!flagy)
                if (y instanceof Integer) {
                    if (((Integer) y).compareTo((Integer) children[i].min_y) > 0 && ((Integer) y).compareTo((Integer) children[i].max_y) <= 0) {
                        flagy = true;
                    }
                } else if (y instanceof Double) {
                    if (((Double) y).compareTo((Double) children[i].min_y) > 0 && ((Double) y).compareTo((Double) children[i].max_y) <= 0) {
                        flagy = true;
                    }
                } else if (y instanceof String) {
                    if (((String) y).compareToIgnoreCase((String) children[i].min_y) > 0 && ((String) y).compareToIgnoreCase((String) children[i].max_y) <= 0) {
                        flagy = true;
                    }
                } else {
                    if (((Date) y).compareTo((Date) children[i].min_y) > 0 && ((Date) y).compareTo((Date) children[i].max_y) <= 0) {
                        flagy = true;
                    }
                }

            if(!flagz)
                if (z instanceof Integer) {
                    if (((Integer) z).compareTo((Integer) children[i].min_z) > 0 && ((Integer) z).compareTo((Integer) children[i].max_z) <= 0) {
                        flagz = true;
                    }
                } else if (z instanceof Double) {
                    if (((Double) z).compareTo((Double) children[i].min_z) > 0 && ((Double) z).compareTo((Double) children[i].max_z) <= 0) {
                        flagz = true;
                    }
                } else if (z instanceof String) {
                    if (((String) z).compareToIgnoreCase((String) children[i].min_z) > 0 && ((String) z).compareToIgnoreCase((String) children[i].max_z) <= 0) {
                        flagz = true;
                    }
                } else {
                    if (((Date) z).compareTo((Date) children[i].min_z) > 0 && ((Date) z).compareTo((Date) children[i].max_z) <= 0) {
                        flagz = true;
                    }
                }
                if(!(flagx && flagy && flagz)){
                    if(x instanceof DBAppNull){
                        flagx=true;
                    }
                    if(y instanceof DBAppNull){
                        flagy=true;
                    }if(z instanceof DBAppNull){
                        flagz=true;
                    }
                    if(!flagx)
                        if (x instanceof Integer) {
                            if (((Integer) x).compareTo((Integer) children[i].min_x) >= 0 && ((Integer) x).compareTo((Integer) children[i].max_x) < 0) {
                                flagx = true;
                            }
                        } else if (x instanceof Double) {
                            if (((Double) x).compareTo((Double) children[i].min_x) >= 0 && ((Double) x).compareTo((Double) children[i].max_x) < 0) {
                                flagx = true;
                            }
                        } else if (x instanceof String) {
                            if (((String) x).compareToIgnoreCase((String) children[i].min_x) >= 0 && ((String) x).compareToIgnoreCase((String) children[i].max_x) < 0) {
                                flagx = true;
                            }
                        } else {
                            if (((Date) x).compareTo((Date) children[i].min_x) >= 0 && ((Date) x).compareTo((Date) children[i].max_x) < 0) {
                                flagx = true;
                            }
                        }

                    if(!flagy)
                        if (y instanceof Integer) {
                            if (((Integer) y).compareTo((Integer) children[i].min_y) >= 0 && ((Integer) y).compareTo((Integer) children[i].max_y) < 0) {
                                flagy = true;
                            }
                        } else if (y instanceof Double) {
                            if (((Double) y).compareTo((Double) children[i].min_y) >= 0 && ((Double) y).compareTo((Double) children[i].max_y) < 0) {
                                flagy = true;
                            }
                        } else if (y instanceof String) {
                            if (((String) y).compareToIgnoreCase((String) children[i].min_y) >= 0 && ((String) y).compareToIgnoreCase((String) children[i].max_y) < 0) {
                                flagy = true;
                            }
                        } else {
                            if (((Date) y).compareTo((Date) children[i].min_y) >= 0 && ((Date) y).compareTo((Date) children[i].max_y) < 0) {
                                flagy = true;
                            }
                        }

                    if(!flagz)
                        if (z instanceof Integer) {
                            if (((Integer) z).compareTo((Integer) children[i].min_z) >= 0 && ((Integer) z).compareTo((Integer) children[i].max_z) < 0) {
                                flagz = true;
                            }
                        } else if (z instanceof Double) {
                            if (((Double) z).compareTo((Double) children[i].min_z) >= 0 && ((Double) z).compareTo((Double) children[i].max_z) < 0) {
                                flagz = true;
                            }
                        } else if (z instanceof String) {
                            if (((String) z).compareToIgnoreCase((String) children[i].min_z) >= 0 && ((String) z).compareToIgnoreCase((String) children[i].max_z) < 0) {
                                flagz = true;
                            }
                        } else {
                            if (((Date) z).compareTo((Date) children[i].min_z) >= 0 && ((Date) z).compareTo((Date) children[i].max_z) < 0) {
                                flagz = true;
                            }
                        }
                }
                if (flagx && flagy && flagz) {
                    indices.add(i);
                }
        }
        return indices;
    }
    public Vector<Object> delete(Object x, Object y, Object z,boolean partial){
        Vector<Object> ref =new Vector<>();
        boolean x_match = false;
        boolean y_match = false; //these 3 boolean values will be used to check for the duplicates when deleting
        boolean z_match = false;
        if(partial){
        if(this.children[0]!= null) {//if the node has children
            Vector<Integer> index = this.indexchild(x, y, z); //index to the possible nodes to be deleted from
            for(int j = 0; j < index.size();j++) {
                if (this.children[index.get(j)].children[0] != null) { //if the node's child j has children
                    ref.addAll(children[index.get(j)].delete(x, y, z,partial)); //recursively delete from the child
                } else {
                    for (int i = 0; i < children[index.get(j)].data.size(); i++) {
                        if (children[index.get(j)].data.get(i).get(0).equals(x) ||x instanceof DBAppNull) {
                            if (children[index.get(j)].data.get(i).get(1).equals(y) || y instanceof DBAppNull) {
                                if (children[index.get(j)].data.get(i).get(2).equals(z) || z instanceof DBAppNull) {
                                    ref.add((children[index.get(j)].data.get(i)));//add the reference of the node to the ref vector b4 deleting
                                    for(int l = 0; l < children[index.get(j)].duplicates.size(); l++){ //loop over each duplicates' vector
                                        for(int k = 0; k < children[index.get(j)].duplicates.get(l).size(); k++){ //loop over each duplicate vector
                                            if(x instanceof DBAppNull)
                                                x_match = true;
                                            if(y instanceof DBAppNull)
                                                y_match = true;
                                            if(z instanceof DBAppNull)
                                                z_match = true;
                                            if(!x_match)
                                                if(children[index.get(j)].duplicates.get(l).get(k).get(0).equals(x))
                                                    x_match = true;
                                            if(!y_match)
                                                if(children[index.get(j)].duplicates.get(l).get(k).get(1).equals(y))
                                                    y_match = true;
                                            if(!z_match)
                                                if(children[index.get(j)].duplicates.get(l).get(k).get(2).equals(z))
                                                    z_match = true;
                                            if(x_match && y_match && z_match){
                                                ref.add(children[index.get(j)].duplicates.get(l).get(k).get(3));
                                                children[index.get(j)].duplicates.get(l).remove(k);
                                                k--;// decrement k to match everything
                                            }
                                            x_match = false; y_match = false; z_match = false; //reset the matches back to false
                                        }
                                        if(children[index.get(j)].duplicates.get(l).size()==0){
                                            children[index.get(j)].duplicates.remove(l);
                                            l--;
                                        }
                                    }
                                    children[index.get(j)].data.remove(i); //if we are at a leaf node and the three columns match, delete the node
                                    i--;//decrement the counter to prevent array out of bounds exception
                                }
                            }
                        }
                    }
                }
            }
            boolean flag=false;
            for(int i=0;i<children.length;i++){
                if (children[i].children[0] != null) {
                    flag = true; //set the flag when any child in the node has a child
                    break;
                }
            }
            if(!flag) {
                int size = 0;
                for (int i = 0; i < children.length; i++) {
                    size += children[i].data.size();//sum all children's sizes
                }
                if (size <= n) {//if the size summation of all children is less than or eqaul the max of the parent
                    data.removeAllElements();
                    for (int i = 0; i < children.length; i++) {
                        for (int k = 0; k < children[i].data.size(); k++) {
                            data.add(children[i].data.get(k));//add the data of the node's children to the node
                        }
                    }
                    duplicates.removeAllElements();
                    for (int i = 0; i < children.length; i++) {
                        duplicates.addAll(children[i].duplicates);
                    }
                    children = new node[8];//delete the children
                }
            }
        }else{//if the node doesn't have children
            for (int i = 0; i < data.size(); i++) {
                if(data.get(i).get(0).equals(x)||x instanceof DBAppNull){
                    if(data.get(i).get(1).equals(y)|| y instanceof DBAppNull){
                        if(data.get(i).get(2).equals(z)|| z instanceof DBAppNull){
                            ref.add(data.get(i)); //remove the node and add the reference of the node to ref vector
                            for(int l = 0; l < duplicates.size(); l++){ //loop over each duplicates' vector
                                for(int k = 0; k < duplicates.get(l).size(); k++){ //loop over each duplicate vector
                                    if(x instanceof DBAppNull)
                                        x_match = true;
                                    if(y instanceof DBAppNull)
                                        y_match = true;
                                    if(z instanceof DBAppNull)
                                        z_match = true;
                                    if(!x_match)
                                        if(duplicates.get(l).get(k).get(0).equals(x)) //if the duplicate matches the toBeDeleted
                                            x_match = true;
                                    if(!y_match)
                                        if(duplicates.get(l).get(k).get(1).equals(y))
                                            y_match = true;
                                    if(!z_match)
                                        if(duplicates.get(l).get(k).get(2).equals(z))
                                            z_match = true;
                                    if(x_match && y_match && z_match){
                                        ref.add(duplicates.get(l).get(k).get(3)); //add the duplicate's reference to the ref vector
                                        duplicates.get(l).get(k).remove(k); //remove from duplicates vector
                                        k--;// decrement k to match everything
                                    }
                                    x_match = false; y_match = false; z_match = false; //reset the xyz_matches back to false
                                }
                                if(duplicates.get(l).size()==0){
                                    duplicates.remove(l);
                                    l--;
                                }
                            }
                            data.remove(i);
                            i--;
                        }
                    }
                }
            }
        }
        }
        else{
            if(this.children[0]!= null) {//if the node has children
                Vector<Integer> index = this.indexchild(x, y, z); //index to the possible nodes to be deleted from
                for(int j = 0; j < index.size();j++) {
                    if (this.children[index.get(j)].children[0] != null) { //if the node's child j has children
                        ref.addAll(children[index.get(j)].delete(x, y, z,partial)); //recursively delete from the child
                    } else {
                        for (int i = 0; i < children[index.get(j)].data.size(); i++) {
                            if (children[index.get(j)].data.get(i).get(0).equals(x)) {
                                if (children[index.get(j)].data.get(i).get(1).equals(y)) {
                                    if (children[index.get(j)].data.get(i).get(2).equals(z)) {
                                        ref.add((children[index.get(j)].data.get(i).get(3)));//add the reference of the node to the ref vector b4 deleting
                                        for(int l = 0; l < children[index.get(j)].duplicates.size(); l++){ //loop over each duplicates' vector
                                            for(int k = 0; k < children[index.get(j)].duplicates.get(l).size(); k++){ //loop over each duplicate vector
                                                    if(children[index.get(j)].duplicates.get(l).get(k).get(0).equals(x))
                                                        x_match = true;
                                                    if(children[index.get(j)].duplicates.get(l).get(k).get(1).equals(y))
                                                        y_match = true;
                                                    if(children[index.get(j)].duplicates.get(l).get(k).get(2).equals(z))
                                                        z_match = true;
                                                if(x_match && y_match && z_match){
                                                    ref.add(children[index.get(j)].duplicates.get(l).get(k).get(3));
                                                    children[index.get(j)].duplicates.get(l).remove(k);
                                                    k--;// decrement k to match everything
                                                }
                                                x_match = false; y_match = false; z_match = false; //reset the matches back to false
                                            }
                                            if(children[index.get(j)].duplicates.get(l).size()==0){
                                                children[index.get(j)].duplicates.remove(l);
                                                l--;
                                            }
                                        }
                                        children[index.get(j)].data.remove(i); //if we are at a leaf node and the three columns match, delete the node
                                        i--;//decrement the counter to prevent array out of bounds exception
                                    }
                                }
                            }
                        }

                    }
                }
                boolean flag=false;
                for(int i=0;i<children.length;i++){
                    if(children[i].children[0]!=null){
                        flag=true; //set the flag when any child in the node has a child
                    }
                }
                if(!flag) {
                    int size = 0;
                    for (int i = 0; i < children.length; i++) {
                        size += children[i].data.size();//sum all children's sizes
                    }
                    if (size <= n) {//if the size summation of all children is less than or eqaul the max of the parent
                        data.removeAllElements();
                        for (int i = 0; i < children.length; i++) {
                            for (int k = 0; k < children[i].data.size(); k++) {
                                data.add(children[i].data.get(k));//add the data of the node's children to the node
                            }
                        }
                        duplicates.removeAllElements();
                        for (int i = 0; i < children.length; i++) {
                            duplicates.addAll(children[i].duplicates);
                        }
                        children = new node[8];//delete the children
                    }
                }
            }else{//if the node doesn't have children
                for (int i = 0; i < data.size(); i++) {
                    if(data.get(i).get(0).equals(x)){
                        if(data.get(i).get(1).equals(y)){
                            if(data.get(i).get(2).equals(z)){
                                ref.add(data.get(i).get(3)); //remove the node and add the reference of the node to ref vector
                                for(int l = 0; l < duplicates.size(); l++){ //loop over each duplicates' vector
                                    for(int k = 0; k < duplicates.get(l).size(); k++){ //loop over each duplicate vector
                                            if(duplicates.get(l).get(k).get(0).equals(x)) //if the duplicate matches the toBeDeleted
                                                x_match = true;
                                            if(duplicates.get(l).get(k).get(1).equals(y))
                                                y_match = true;
                                            if(duplicates.get(l).get(k).get(2).equals(z))
                                                z_match = true;
                                        if(x_match && y_match && z_match){
                                            ref.add(duplicates.get(l).get(k).get(3)); //add the duplicate's reference to the ref vector
                                            duplicates.get(l).get(k).remove(k); //remove from duplicates vector
                                            k--;// decrement k to match everything
                                        }
                                        x_match = false; y_match = false; z_match = false; //reset the xyz_matches back to false
                                    }
                                    if(duplicates.get(l).size()==0){
                                        duplicates.remove(l);
                                        l--;
                                    }
                                }
                                data.remove(i);
                                i--;
                            }
                        }
                    }
                }
            }
        }
        return ref;
    }
    public Vector<Object> search(Object x, Object y, Object z,boolean partial){
        Vector<Object> ref =new Vector<>();
        boolean x_match = false;
        boolean y_match = false; //these 3 boolean values will be used to check for the duplicates when deleting
        boolean z_match = false;
        if(partial){
            if(this.children[0]!= null) {//if the node has children
                Vector<Integer> index = this.indexchild(x, y, z); //index to the possible nodes to be deleted from
                for(int j = 0; j < index.size();j++) {
                    if (this.children[index.get(j)].children[0] != null) { //if the node's child j has children
                        ref.addAll(children[index.get(j)].search(x, y, z,partial)); //recursively delete from the child
                    } else {
                        for (int i = 0; i < children[index.get(j)].data.size(); i++) {
                            if (children[index.get(j)].data.get(i).get(0).equals(x) ||x instanceof DBAppNull) {
                                if (children[index.get(j)].data.get(i).get(1).equals(y) || y instanceof DBAppNull) {
                                    if (children[index.get(j)].data.get(i).get(2).equals(z) || z instanceof DBAppNull) {
                                        ref.add((children[index.get(j)].data.get(i).get(3)));//add the reference of the node to the ref vector b4 deleting
                                        for(int l = 0; l < children[index.get(j)].duplicates.size(); l++){ //loop over each duplicates' vector
                                            for(int k = 0; k < children[index.get(j)].duplicates.get(l).size(); k++){ //loop over each duplicate vector
                                                if(x instanceof DBAppNull)
                                                    x_match = true;
                                                if(y instanceof DBAppNull)
                                                    y_match = true;
                                                if(z instanceof DBAppNull)
                                                    z_match = true;
                                                if(!x_match)
                                                    if(children[index.get(j)].duplicates.get(l).get(k).get(0).equals(x))
                                                        x_match = true;
                                                if(!y_match)
                                                    if(children[index.get(j)].duplicates.get(l).get(k).get(1).equals(y))
                                                        y_match = true;
                                                if(!z_match)
                                                    if(children[index.get(j)].duplicates.get(l).get(k).get(2).equals(z))
                                                        z_match = true;
                                                if(x_match && y_match && z_match){
                                                    ref.add(children[index.get(j)].duplicates.get(l).get(k).get(3));
                                                }
                                                x_match = false; y_match = false; z_match = false; //reset the matches back to false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }else{//if the node doesn't have children
                for (int i = 0; i < data.size(); i++) {
                    if(data.get(i).get(0).equals(x)||x instanceof DBAppNull){
                        if(data.get(i).get(1).equals(y)|| y instanceof DBAppNull){
                            if(data.get(i).get(2).equals(z)|| z instanceof DBAppNull){
                                ref.add(data.get(i).get(3)); //remove the node and add the reference of the node to ref vector
                                for(int l = 0; l < duplicates.size(); l++){ //loop over each duplicates' vector
                                    for(int k = 0; k < duplicates.get(l).size(); k++){ //loop over each duplicate vector
                                        if(x instanceof DBAppNull)
                                            x_match = true;
                                        if(y instanceof DBAppNull)
                                            y_match = true;
                                        if(z instanceof DBAppNull)
                                            z_match = true;
                                        if(!x_match)
                                            if(duplicates.get(l).get(k).get(0).equals(x)) //if the duplicate matches the toBeDeleted
                                                x_match = true;
                                        if(!y_match)
                                            if(duplicates.get(l).get(k).get(1).equals(y))
                                                y_match = true;
                                        if(!z_match)
                                            if(duplicates.get(l).get(k).get(2).equals(z))
                                                z_match = true;
                                        if(x_match && y_match && z_match){
                                            ref.add(duplicates.get(l).get(k).get(3)); //add the duplicate's reference to the ref vector
                                        }
                                        x_match = false; y_match = false; z_match = false; //reset the xyz_matches back to false
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else{
            if(this.children[0]!= null) {//if the node has children
                Vector<Integer> index = this.indexchild(x, y, z); //index to the possible nodes to be deleted from
                for(int j = 0; j < index.size();j++) {
                    if (this.children[index.get(j)].children[0] != null) { //if the node's child j has children
                        ref.addAll(children[index.get(j)].search(x, y, z,partial)); //recursively delete from the child
                    } else {
                        for (int i = 0; i < children[index.get(j)].data.size(); i++) {
                            if (children[index.get(j)].data.get(i).get(0).equals(x)) {
                                if (children[index.get(j)].data.get(i).get(1).equals(y)) {
                                    if (children[index.get(j)].data.get(i).get(2).equals(z)) {
                                        ref.add((children[index.get(j)].data.get(i).get(3)));//add the reference of the node to the ref vector b4 deleting
                                        for(int l = 0; l < children[index.get(j)].duplicates.size(); l++){ //loop over each duplicates' vector
                                            for(int k = 0; k < children[index.get(j)].duplicates.get(l).size(); k++){ //loop over each duplicate vector
                                                if(children[index.get(j)].duplicates.get(l).get(k).get(0).equals(x))
                                                    x_match = true;
                                                if(children[index.get(j)].duplicates.get(l).get(k).get(1).equals(y))
                                                    y_match = true;
                                                if(children[index.get(j)].duplicates.get(l).get(k).get(2).equals(z))
                                                    z_match = true;
                                                if(x_match && y_match && z_match){
                                                    ref.add(children[index.get(j)].duplicates.get(l).get(k).get(3));
                                                }
                                                x_match = false; y_match = false; z_match = false; //reset the matches back to false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }else{//if the node doesn't have children
                for (int i = 0; i < data.size(); i++) {
                    if(data.get(i).get(0).equals(x)){
                        if(data.get(i).get(1).equals(y)){
                            if(data.get(i).get(2).equals(z)){
                                ref.add(data.get(i).get(3)); //remove the node and add the reference of the node to ref vector
                                for(int l = 0; l < duplicates.size(); l++){ //loop over each duplicates' vector
                                    for(int k = 0; k < duplicates.get(l).size(); k++){ //loop over each duplicate vector
                                        if(duplicates.get(l).get(k).get(0).equals(x)) //if the duplicate matches the toBeDeleted
                                            x_match = true;
                                        if(duplicates.get(l).get(k).get(1).equals(y))
                                            y_match = true;
                                        if(duplicates.get(l).get(k).get(2).equals(z))
                                            z_match = true;
                                        if(x_match && y_match && z_match){
                                            ref.add(duplicates.get(l).get(k).get(3)); //add the duplicate's reference to the ref vector
                                        }
                                        x_match = false; y_match = false; z_match = false; //reset the xyz_matches back to false
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ref;
    }
    public Vector<String> searchSelect(Object x,Object y,Object z, String[] operators){
        Vector<String> ref = new Vector<>();
        if(children[0]==null){
            Map<String, Predicate<Object>> conditions = parseConditions(operators, new Object[]{x, y, z},new String[]{"x","y","z"});
            Predicate<Object> conditionX = conditions.get("x");
            Predicate<Object> conditionY = conditions.get("y");
            Predicate<Object> conditionZ = conditions.get("z");
            for(int i = 0; i<data.size();i++){
                if(conditionX.test(data.get(i).get(0))&&conditionY.test(data.get(i).get(1))&&conditionZ.test(data.get(i).get(2))){
                    ref.add((String) data.get(i).get(3));
                    for(int j = 0; j<duplicates.size();j++){
                        if(conditionX.test(duplicates.get(j).get(0).get(0))&&conditionY.test(duplicates.get(j).get(0).get(1))&&conditionZ.test(duplicates.get(j).get(0).get(2))){
                            for(int k = 0; k<duplicates.get(j).size();k++){
                                ref.add((String) duplicates.get(j).get(j).get(3));
                            }
                        }
                    }
                }
            }
        }else{
            Vector<Integer> indices = selectIndex(new Object[]{x,y,z},operators);
            for(int i = 0; i<indices.size();i++)
                ref.addAll(children[indices.get(i)].searchSelect(x,y,z,operators));
        }
        return ref;
    }
    private Vector<Integer> selectIndex(Object[] values,String []operators){
        Vector<Integer> indices = new Vector<>();
        boolean[] nodes = new boolean[]{true,true,true,true,true,true,true,true};
//        Map<String, Predicate<Object>> conditions = parseConditions(operators, new Object[]{x, y, z},new String[]{"x","y","z"});
        for(int i = 0 ; i<children.length;i++){
            for(int j = 0; j < operators.length;j++){
                switch (operators[j]){
                    case("<"):
                    case ("<="): Object xMin = children[i].min_x;Object yMin = children[i].min_y; Object zMin = children[i].min_z;
                    Predicate<Object>[] lessThan = new Predicate[]{(obj -> ((Comparable)obj).compareTo(xMin)>0),(obj -> ((Comparable)obj).compareTo(yMin)>0),(obj -> ((Comparable)obj).compareTo(zMin)>0)};
                    nodes[i] = nodes[i] && lessThan[j].test(values[j]); break;
                    case(">="):
                    case(">"):Object xMax = children[i].max_x;Object yMax = children[i].max_y; Object zMax = children[i].max_z;
                        Predicate<Object>[] biggerThan = new Predicate[]{(obj -> ((Comparable)obj).compareTo(xMax)<=0),(obj -> ((Comparable)obj).compareTo(yMax)<=0),(obj -> ((Comparable)obj).compareTo(zMax)<=0)};
                        nodes[i] = nodes[i] && biggerThan[j].test(values[j]); break;
                    case("="):if(values[j] instanceof  DBAppNull){break;}
                        Object xMin2 = children[i].min_x;Object yMin2 = children[i].min_y; Object zMin2 = children[i].min_z;
                        Predicate<Object>[] lessThan2 = new Predicate[]{(obj -> ((Comparable)obj).compareTo(xMin2)>0),(obj -> ((Comparable)obj).compareTo(yMin2)>0),(obj -> ((Comparable)obj).compareTo(zMin2)>0)};
                        Object xMax2 = children[i].max_x;Object yMax2 = children[i].max_y; Object zMax2 = children[i].max_z;
                        Predicate<Object>[] biggerThan2 = new Predicate[]{(obj -> ((Comparable)obj).compareTo(xMax2)<=0),(obj -> ((Comparable)obj).compareTo(yMax2)<=0),(obj -> ((Comparable)obj).compareTo(zMax2)<=0)};
                        nodes[i] = nodes[i] && biggerThan2[j].test(values[j]) && lessThan2[j].test(values[j]); break;
                    case("!="): break;
                }
            }
        }
        for(int i = 0;i<nodes.length;i++){
            if(nodes[i])
                indices.add(i);
        }
        return indices;
    }
    private Map<String, Predicate<Object>> parseConditions(String[] operators,Object[] values,String[] key) {
        Map<String, Predicate<Object>> parsedConditions = new HashMap<>();
        // create a predicate that evaluates the condition
        for(int i = 0; i<key.length;i++) {
            Object value = values[i];
            if (operators[i].equals("=")) {
                parsedConditions.put(key[i], x -> x.equals(value));
            } else if (operators[i].equals("!=")) {
                parsedConditions.put(key[i], x -> !x.equals(value));
            } else if (operators[i].equals(">")) {
                parsedConditions.put(key[i], x -> ((Comparable) x).compareTo(value) > 0);
            } else if (operators[i].equals("<")) {
                parsedConditions.put(key[i], x -> ((Comparable) x).compareTo(value) < 0);
            } else if (operators[i].equals(">=")) {
                parsedConditions.put(key[i], x -> ((Comparable) x).compareTo(value) >= 0);
            } else if (operators[i].equals("<=")) {
                parsedConditions.put(key[i], x -> ((Comparable) x).compareTo(value) <= 0);
            }
        }
        return parsedConditions;
    }
    public void deleteOnce(Object x, Object y, Object z, String ref){
        if (children[0] == null) {
            //no children
            for (int i = 0; i < data.size(); i++) {
                int j;
                boolean duplicateFound =false;
                //loops over data inside node, if one of them matches we search over the duplicates if one of the duplicates has the same reference
                if (data.get(i).get(0).equals(x) && data.get(i).get(1).equals(y) && data.get(i).get(2).equals(z)) {
                    for (j = 0; j < duplicates.size(); j++) {
                        if (duplicates.get(j).get(0).get(0).equals(x) && duplicates.get(j).get(0).get(1).equals(y) && duplicates.get(j).get(0).get(2).equals(z)) {
                            duplicateFound =true;
                            for(int k = 0; k<duplicates.get(j).size();k++){
                                if(duplicates.get(j).get(k).get(3).equals(ref)){
                                    duplicates.get(j).remove(k);
                                    if(duplicates.get(j).size()==0){
                                        duplicates.remove(j);
                                    }
                                    return;
                                    //if found we delete and return
                                }
                            }
                            break;
                        }
                    }
                    //if no duplicate had the reference or if it doesn't have any duplicate we remove the actual data
                    if( data.get(i).get(3).equals(ref)) {
                        data.remove(i);
                    }
                    if(duplicates.size()!=0 && duplicateFound) {
                        data.add(duplicates.get(j).remove(0));
                        if (duplicates.get(j).size() == 0) {
                            duplicates.remove(j);
                        }
                    }
                }
            }
        }
        else{
            //has children then we see which child has the value we are looking for and recursively call deleteOnce on it
            Vector<Integer> index = this.indexchild(x, y, z); //index to the possible nodes to be deleted from
            for(int j = 0; j < index.size();j++) {
                if (this.children[index.get(j)].children[0] != null) { //if the node's child j has children
                   children[index.get(j)].deleteOnce(x, y, z,ref); //recursively delete from the child
                } else {
                    boolean deleted = false;
                    for (int i = 0; i < children[index.get(j)].data.size(); i++) {
                         int l;
                        //loops over data inside node, if one of them matches we search over the duplicates if one of the duplicates has the same reference
                        if (children[index.get(j)].data.get(i).get(0).equals(x) && children[index.get(j)].data.get(i).get(1).equals(y) && children[index.get(j)].data.get(i).get(2).equals(z)) {
                            for (l = 0; l < children[index.get(j)].duplicates.size() && !deleted; l++) {
                                if (children[index.get(j)].duplicates.get(l).get(0).get(0).equals(x) && children[index.get(j)].duplicates.get(l).get(0).get(1).equals(y)
                                        && children[index.get(j)].duplicates.get(l).get(0).get(2).equals(z)) {
                                    for(int k = 0; k<children[index.get(j)].duplicates.get(l).size();k++){
                                        if(children[index.get(j)].duplicates.get(l).get(k).get(3).equals(ref)){
                                            children[index.get(j)].duplicates.get(l).remove(k);
                                            deleted = true;
                                            if(children[index.get(j)].duplicates.get(l).size()==0){
                                                children[index.get(j)].duplicates.remove(l);
                                            }
                                            break;
                                            //if found we delete and return
                                        }
                                    }
                                    break;
                                }
                            }
                            boolean dataDeleted =false;
                            //if no duplicate had the reference or if it doesn't have any duplicate we remove the actual data
                            if( children[index.get(j)].data.get(i).get(3).equals(ref) && !deleted) {
                                children[index.get(j)].data.remove(i);
                                dataDeleted = true;
                            }
                            if(dataDeleted && l < children[index.get(j)].duplicates.size()){
                                children[index.get(j)].data.add(children[index.get(j)].duplicates.get(l).remove(0));
                                if(children[index.get(j)].duplicates.get(l).size()==0){
                                    children[index.get(j)].duplicates.remove(l);
                                }
                            }
                        }
                    }
                }
            }
            boolean flag=false;
            for(int i=0;i<children.length;i++){
                if (children[i].children[0] != null) {
                    flag = true; //set the flag when any child in the node has a child
                    break;
                }
            }
            if(!flag) {
                int size = 0;
                for (int i = 0; i < children.length; i++) {
                    size += children[i].data.size();//sum all children's sizes
                }
                if (size <= n) {//if the size summation of all children is less than or eqaul the max of the parent
                    data.removeAllElements();
                    for (int i = 0; i < children.length; i++) {
                        for (int k = 0; k < children[i].data.size(); k++) {
                            data.add(children[i].data.get(k));//add the data of the node's children to the node
                        }
                    }
                    duplicates.removeAllElements();
                    for (int i = 0; i < children.length; i++) {
                        duplicates.addAll(children[i].duplicates);
                    }
                    children = new node[8];//delete the children
                }
            }

//            children[index.get(0)].deleteOnce(x, y, z, ref);
        }
    }


    public void updateRef(Object x, Object y, Object z, String ref,String refNew){
        if (children[0] == null) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).get(0).equals(x) && data.get(i).get(1).equals(y) && data.get(i).get(2).equals(z)) {
                    for (int j = 0; j < duplicates.size(); j++) {
                        if (duplicates.get(j).get(0).get(0).equals(x) && duplicates.get(j).get(0).get(1).equals(y) && duplicates.get(j).get(0).get(2).equals(z)) {
                            for(int k = 0; k<duplicates.get(j).size();k++){
                                if(duplicates.get(j).get(k).get(3).equals(ref)){
                                    duplicates.get(j).get(k).set(3,refNew);
                                    return;
                                }
                            }

                        }
                    }
                    if( data.get(i).get(3).equals(ref))
                        data.get(i).set(3,refNew);
                }
            }

        }
        else{
            Vector<Integer> index = indexchild(x, y, z);
            children[index.get(0)].updateRef(x, y, z, ref,refNew);
        }
    }
}
