import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

public class Table implements Serializable {
    private String TableName;
    private Vector<String> pages;
    private Vector<String> columns;
    private int pageNum;
    private Vector<String> indices;
    public String getTableName() {
        return TableName;
    }

    public Vector<String> getPages() {
        return pages;
    }

    public Table(String name){
        TableName = name;
        pages = new Vector<String>();
        indices = new Vector<String>();
    }

    public Vector<String> getColumns() {
        return columns;
    }

    public void setColumns(Vector<String> columns) {
        this.columns = columns;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public Vector<String> getIndices() {
        return indices;
    }
}
