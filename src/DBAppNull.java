import java.io.Serializable;

public class DBAppNull implements Serializable, Comparable{
    public String toString(){
    return "null";
    }
    public boolean equals(Object x){
        if(x instanceof DBAppNull)
        return true;
        return false;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
