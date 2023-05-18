import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Iterator;

public class DBApp {

    public static int binarySearch(Vector<Vector<Object>> vec, Object target, int i) {
        int left = 0;
        int right = vec.size() - 1;
        if (vec.get(0).get(i) instanceof String) {
            while (left <= right) {
                int mid = left + (right - left) / 2;
                if (((String) vec.get(mid).get(i)).compareTo((String) target) == 0) {
                    return mid;
                } else if (((String) vec.get(mid).get(i)).compareTo((String) target) < 0) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        }
        if (vec.get(0).get(i) instanceof Date) {
            while (left <= right) {
                int mid = left + (right - left) / 2;
                if (((Date) vec.get(mid).get(i)).compareTo((Date) target) == 0) {
                    return mid;
                } else if (((Date) vec.get(mid).get(i)).compareTo((Date) target) < 0) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        }
        if (vec.get(0).get(i) instanceof Integer) {
            while (left <= right) {
                int mid = left + (right - left) / 2;
                if (((Integer) vec.get(mid).get(i)).compareTo((Integer) target) == 0) {
                    return mid;
                } else if (((Integer) vec.get(mid).get(i)).compareTo((Integer) target) < 0) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        } else {
            while (left <= right) {
                int mid = left + (right - left) / 2;
                if (((Double) vec.get(mid).get(i)).compareTo((Double) target) == 0) {
                    return mid;
                } else if (((Double) vec.get(mid).get(i)).compareTo((Double) target) < 0) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        }


        //target not found
        return -1;
    }

    public static void serialize_table(Table e) {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(e.getTableName() + ".ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(e);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static void serialize_page(String tableName, Vector<Vector<Object>> e, int pageNum) throws DBAppException {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(tableName + "Page" + pageNum + ".ser"); //to be decided
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(e);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static void serialize_index(String indexName, octree t) throws DBAppException {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(indexName); //to be decided
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(t);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static Table deserialize_table(String tableName) throws DBAppException {
        Table e = null;

        try {
            FileInputStream fileIn = new FileInputStream(tableName + ".ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            e = (Table) in.readObject();
            in.close();
            fileIn.close();
            return e;
        } catch (IOException | ClassNotFoundException i) {
            throw new DBAppException("table not found");
        }
    }

    public static Vector<Vector<Object>> deserialize_page(Table table, int i) {
        Vector<Vector<Object>> p = null;
        try {
            FileInputStream fileIn = new FileInputStream(table.getPages().get(i));
            ObjectInputStream in = new ObjectInputStream(fileIn);
            p = (Vector<Vector<Object>>) in.readObject();

            in.close();
            fileIn.close();
            return p;
        } catch (IOException | ClassNotFoundException o) {
            o.printStackTrace();
        }
        return null;
    }

    public static octree deserialize_index(String indexName) throws DBAppException {
        octree p = null;
        try {
            FileInputStream fileIn = new FileInputStream(indexName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            p = (octree) in.readObject();

            in.close();
            fileIn.close();
            return p;
        } catch (IOException | ClassNotFoundException o) {
            o.printStackTrace();
        }
        return null;
    }

    public static void pageFixing(Table t) {
        for (int i = 0; i < t.getPages().size(); i++) {
            File f = new File(t.getPages().get(i));
            boolean renamed = f.renameTo(new File(t.getTableName() + "Page" + i + ".ser"));
            t.getPages().set(i, t.getTableName() + "Page" + i + ".ser");
        }
    }

    public static void maxMinCheck(Hashtable<String, Object> values, String tableName) throws DBAppException, IOException, ParseException {
        BufferedReader reader = new BufferedReader(new FileReader("metadata.csv"));
        String line1 = null;
        reader.readLine();
        while ((line1 = reader.readLine()) != null) {
            String[] data = line1.split(", ");
            if (data[0].equals(tableName)) {
                Object v = values.get(data[1]);
                if (v == null || v instanceof DBAppNull)
                    continue;
                if (v instanceof String) {
                    if (data[6].compareToIgnoreCase((String) v) > 0 || data[7].compareToIgnoreCase((String) v) < 0) {
                        throw new DBAppException("Value not in Min/Max constraint");
                    }
                } else if (v instanceof Integer) {
                    if (((Integer) Integer.parseInt(data[6])).compareTo((Integer) v) > 0 || ((Integer) Integer.parseInt(data[7])).compareTo((Integer) v) < 0) {
                        throw new DBAppException("Value not in Min/Max constraint");
                    }

                } else if (v instanceof Double) {
                    if (Double.parseDouble(data[6]) > ((Double) v) || Double.parseDouble(data[7]) < ((Double) v)) {
                        throw new DBAppException("Value not in Min/Max constraint");
                    }
                } else {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    Date date1 = formatter.parse(data[6]);
                    Date date2 = formatter.parse(data[7]);
                    if (date1.after((Date) v) || date2.before((Date) v)) {

                        throw new DBAppException("Value not in Min/Max constraint");
                    }
                }
            }
        }
        reader.close();
    }

    private Map<String, Predicate<Object>> parseConditions(String[] operators, Object[] values, String[] key) {
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
    public void init() {
        //Create the CSV File
        String csvFilePath = "metadata.csv";
        try {
            FileWriter fileWriter = new FileWriter(csvFilePath, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("Table Name, Column Name, Column Type, ClusteringKey, IndexName,IndexType, min, max");
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createTable(String strTableName, String strClusteringKeyColumn, Hashtable<String,
            String> htblColNameType, Hashtable<String, String> htblColNameMin, Hashtable<String, String> htblColNameMax) throws DBAppException {

        Set<String> keys = htblColNameType.keySet(); //create a set which contain the names of all the columns
        Vector<String> columns = new Vector<>(); //create a vector that will contain the name of the columns inorder
        if (!(htblColNameType.size() == htblColNameMin.size() && htblColNameType.size() == htblColNameMax.size())) { // we check if any of the data max/min/type is missing
            throw new DBAppException("missing min/max/type of a column");
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader("metadata.csv"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (strTableName.equals(data[0])) {
                    throw new DBAppException("the table is already available"); //we check if the table is already existing in our database using the metadata
                }
            }

            reader.close();
            FileWriter fileWriter = new FileWriter("metadata.csv", true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            if (keys.size() < htblColNameType.size())
                throw new DBAppException("there are two columns with the same name"); //we check if the user inserted 2 columns with the same name
            boolean primExist = false;
            for (String key : keys) {
                String type = htblColNameType.get(key);
                String min = htblColNameMin.get(key);
                String max = htblColNameMax.get(key);
                if (type.equals("java.lang.String")) {  //we check if Min is greater than max in all Types
                    if (min.compareTo(max) > 0) {
                        throw new DBAppException("Min greater than max");
                    }
                } else if (type.equals("java.lang.Integer")) {
                    if (((Integer) Integer.parseInt(min)).compareTo(Integer.parseInt(max)) > 0) {
                        throw new DBAppException("Min greater than max");
                    }

                } else if (type.equals("java.lang.Double")) {
                    if (((Double) Double.parseDouble(min)).compareTo(Double.parseDouble(max)) > 0) {
                        throw new DBAppException("Min greater than max");
                    }
                } else {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    Date date1 = formatter.parse(min);
                    Date date2 = formatter.parse(max);
                    if (date1.after(date2)) {
                        throw new DBAppException("Min greater than max");
                    }
                }
                if (key.equals(strClusteringKeyColumn)) {
                    primExist = true;  //we see if the column that is supposed to be the primary key is existing within the columns we are creating
                }
            }
            if (!primExist) {
                throw new DBAppException("Primary key is not stated"); //if the primary key does not exist we throw exception
            }
            for (String key : keys) { //this for loop we insert our info in the metadata
                String type = htblColNameType.get(key);
                String min = htblColNameMin.get(key);
                String max = htblColNameMax.get(key);
                bufferedWriter.write(strTableName + ", " + key + ", " + type + ", " + key.equals(strClusteringKeyColumn) + ", " + "null, null, " + min + ", " + max);
                bufferedWriter.newLine();
                columns.add(key);
            }
            bufferedWriter.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        Table tx = new Table(strTableName);
        tx.setColumns(columns); //we then make our new table and give it the rows with the right order of columns then we serialize it
        serialize_table(tx);
    }

    public void createIndex(String strTableName, String[] strarrColName) throws DBAppException {
        if (strarrColName.length != 3) { //we check if the inserted columns are less than or larger than 3 if yes throw exception
            throw new DBAppException("the index is created on less or more than 3 columns");
        }
        String filePath = "metadata.csv";
        Table t = deserialize_table(strTableName);

        File inputFile = new File(filePath);
        File tempFile = new File("metadatanew.csv");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            Object[] max = new Object[6]; //this array will contain the min and max of each column used in the index
            String currentLine;
            while ((currentLine = reader.readLine()) != null) { //this for loop is used to take the old data from the metadata and edit in the columns which we will create index using it by
                String[] row = currentLine.split(", ");
                for (int i = 0; i < strarrColName.length; i++) {
                    if (row[0].equals(strTableName) && row[1].equals(strarrColName[i])) { // check for row contents if it is the column I use to create the index or not adn check if it is the same table
                        // Modify the contents of the row
                        row[4] = strarrColName[0] + "_" + strarrColName[1] + "_" + strarrColName[2] + "_" + "Index";
                        row[5] = "Octree";
                        //get the max and the min and put it in array "Max"
                        if (row[2].equals("java.lang.Integer")) {
                            max[i * 2] = Integer.parseInt(row[6]);
                            max[(i * 2) + 1] = Integer.parseInt(row[7]);
                        } else if (row[2].equals("java.lang.Double")) {
                            max[i * 2] = Double.parseDouble(row[6]);
                            max[(i * 2) + 1] = Double.parseDouble(row[7]);
                        } else if (row[2].equals("java.util.Date")) {
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                            max[i * 2] = formatter.parse(row[6]);
                            max[(i * 2) + 1] = formatter.parse(row[7]);
                        } else {
                            max[i * 2] = row[6];
                            max[(i * 2) + 1] = row[7];
                        }
                    }
                }
                currentLine = "";
                //add the content of array ro to a string to add this as a line in the metadata
                for (int j = 0; j < row.length; j++) {
                    if (j != row.length - 1)
                        currentLine = currentLine + row[j] + ", ";
                    else
                        currentLine = currentLine + row[j];
                }
                // Write the modified row to the new file
                writer.write(currentLine);
                writer.newLine();
            }
            //add the index name to the vector of Indices names of the table
            t.getIndices().add(strTableName + "_" + strarrColName[0] + "_" + strarrColName[1] + "_" + strarrColName[2] + "_" + "Index.ser");

            octree x = new octree(max[0], max[1], max[2], max[3], max[4], max[5]);

            writer.close();
            reader.close();
            inputFile.delete();
            // Delete the old file and rename the new file
            Files.copy(tempFile.toPath(), inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            tempFile.delete();
            for (int i = 0; i < t.getPages().size(); i++) { //this for loop adds every record in the table to new Index
                Vector<Vector<Object>> page = deserialize_page(t, i);
                for (int j = 0; j < page.size(); j++) {
                    Object first = page.get(j).get(t.getColumns().indexOf(strarrColName[0]));
                    Object second = page.get(j).get(t.getColumns().indexOf(strarrColName[1]));
                    Object third = page.get(j).get(t.getColumns().indexOf(strarrColName[2]));
                    if (first instanceof DBAppNull) {
                        first = null;
                    }
                    if (second instanceof DBAppNull) {
                        second = null;
                    }
                    if (third instanceof DBAppNull) {
                        third = null;
                    }
                    x.insert(first, second, third, t.getPages().get(i));
                }
            }
            serialize_index(strTableName + "_" + strarrColName[0] + "_" + strarrColName[1] + "_" + strarrColName[2] + "_" + "Index.ser", x);
            serialize_table(t);
        } catch (IOException | ParseException e) {
            throw new DBAppException(e.getMessage());
        }
    }

    public void insertIntoTable(String strTableName, Hashtable<String, Object> htblColNameValue)
            throws DBAppException {

        Set<String> keys = htblColNameValue.keySet();
        for(String key: keys){
            if(htblColNameValue.get(key) instanceof String){
                htblColNameValue.put(key,((String) htblColNameValue.get(key)).toLowerCase());
            }
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader("metadata.csv"));
            String line = null;
            String prim = null; //this string holds the name of the primary key
            line = reader.readLine();
            Vector types = new Vector(); //this vector will hold the correct types of the table
            Properties properties = new Properties(); //Properties will load the config file, so we get the max of the pages
            try {
                properties.load(new FileInputStream("src/resources/DBApp.config"));
            } catch (IOException e) {
                throw new DBAppException(e.getMessage());
            }
            int n = Integer.parseInt(properties.getProperty(String.valueOf(properties.stringPropertyNames().toArray()[0])));
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data[0].equals(strTableName) && data[3].equalsIgnoreCase(" True")) {
                    prim = data[1]; //we get the name of the primary key column
                }
                if (data[0].equals(strTableName)) {
                    types.add(data[2]); //get the type of the column and insert it in Types
                }
            }
            reader.close();
            Vector<Object> newRecord = new Vector<>(); //this vector will hold the data of the new tuple that we will add
            Table table = null;

            table = deserialize_table(strTableName);
            for (String key : keys) {
                if (!table.getColumns().contains(key)) {
                    throw new DBAppException("Column doesn't exist"); //we check if the user inserted any column that do not exist in the table
                }
            }

            for (int i = 0; i < table.getColumns().size(); i++)
                newRecord.add(null); //we add nulls to fill the tuple, so we can insert to specific index in the vector

            for (int i = 0; i < table.getColumns().size(); i++) {

                if (keys.contains(table.getColumns().get(i))) {
                    newRecord.add(i, htblColNameValue.get(table.getColumns().get(i))); //we add the values of the new record to var tuple
                } else {
                    if ((" " + table.getColumns().get(i)).equals(prim)) {
                        throw new DBAppException("Please insert the primary key"); //check if the primary key is inserted if not throw exception

                    } else {
                        newRecord.add(i, new DBAppNull()); //if any column (except the primary key) is not inserted put its value as DBAppNull
                    }

                }
                newRecord.remove(null); //remove the filling null that we inserted before;
            }

            if (htblColNameValue.get(prim.split(" ")[1]) instanceof DBAppNull) {
                throw new DBAppException("Primary key cannot be null"); //we check if the user inserted the primary key value with DBAppNull if yes throw exception
            }
            BufferedReader reader1 = new BufferedReader(new FileReader("metadata.csv"));
            String line1 = null;
            int v = 0;
            reader1.readLine();
            while ((line1 = reader1.readLine()) != null) { //this for loop is used to check the data types of the new record if it matches the original table types
                String[] data = line1.split(",");
                if (newRecord.get(v) instanceof DBAppNull) { //check if the value of this column in the new record is DBAppNull if yes skip it
                    v++;
                    continue;
                }
                if (data[0].equals(strTableName) && !data[2].equalsIgnoreCase(" " + (newRecord.get(v).getClass() + "").split(" ")[1])) { //check the data type
                    throw new DBAppException("DataType incompatible");
                }
                v++;
            }
            reader1.close();
            maxMinCheck(htblColNameValue, strTableName); //this function checks if the values of the new record is between the correct boundries of the table
            Vector<Vector<Object>> page = new Vector<>();
            int pageNum = 0;
            if (table.getPages().size() == 0) { //we check if the table is Empty and contains zero record in it
                page.add(newRecord);
                serialize_page(strTableName, page, 0); //create new page and add the record and serialize the page
                table.getPages().add(strTableName + "Page0.ser");//add the reference of the page to the table pages vector
                for (int i = 0; i < table.getIndices().size(); i++) { //add the new record to the indices of the table
                    octree tree = deserialize_index(table.getIndices().get(i));
                    String[] colNameIndex = table.getIndices().get(i).split("_");  //get the columns name of the index
                    tree.insert(newRecord.get(table.getColumns().indexOf(colNameIndex[1])), newRecord.get(table.getColumns().indexOf(colNameIndex[2])),
                            newRecord.get(table.getColumns().indexOf(colNameIndex[3])), table.getPages().get(0)); //insert the record to the index then serialize the index
                    serialize_index(table.getIndices().get(i), tree);
                }
            } else { //if the table already contains records
                int indexOfPrim = table.getColumns().indexOf(prim.split(" ")[1]);  //we get the index of the primary key
                for (int i = 0; i < table.getPages().size(); i++) { //this for loop checks if this record's primary key already exists in the table
                    Vector<Vector<Object>> p = null;
                    p = deserialize_page(table, i);
                    if (binarySearch(p, newRecord.get(indexOfPrim), indexOfPrim) != -1) //if binarySearch returns any number it means that this primary key already exists
                        throw new DBAppException("this primary key is already used");
                }
                int[] insertLoc = new int[2]; //this array will hold the value of the page (in index 0) and the index inside this page (in index 1)
                boolean insertInBetween = false; //this boolean will tell us if the new record will be inserted in last place in the table or not
                for (int i = 0; i < table.getPages().size(); i++) { //this for loop tells us where will we insert the new record
                    Vector<Vector<Object>> p = null;
                    p = deserialize_page(table, i); //we sreach in one page
                    for (int j = 0; j < p.size(); j++) { //in every record in the page we check if the primary key of this record is larger than the new record's, if no contniue sreaching
                        if (p.get(j).get(indexOfPrim) instanceof String x) {
                            if (x.compareTo((String) newRecord.get(indexOfPrim)) > 0) { //if the larger primary key found put the location in the array and set insertInBetween
                                insertLoc[0] = i;
                                insertLoc[1] = j;
                                insertInBetween = true;
                                break;
                            }

                        } else if (p.get(j).get(indexOfPrim) instanceof Date x) {
                            if (x.compareTo((Date) newRecord.get(indexOfPrim)) > 0) {
                                insertLoc[0] = i;
                                insertLoc[1] = j;
                                insertInBetween = true;
                                break;
                            }
                        } else if (p.get(j).get(indexOfPrim) instanceof Integer x) {
                            if (x.compareTo((Integer) newRecord.get(indexOfPrim)) > 0) {
                                insertLoc[0] = i;
                                insertLoc[1] = j;
                                insertInBetween = true;
                                break;
                            }
                        } else {
                            Double x = (Double) p.get(j).get(indexOfPrim);
                            if (x.compareTo((Double) newRecord.get(indexOfPrim)) > 0) {
                                insertLoc[0] = i;
                                insertLoc[1] = j;
                                insertInBetween = true;
                                break;
                            }
                        }
                    }
                    if (insertInBetween) {
                        break;
                    }
                }
                if (!insertInBetween) { //check if we already found a loaction if not that means that the new record will be inserted in the last place in the table
                    insertLoc[0] = table.getPages().size() - 1;
                    Vector<Vector<Object>> p = deserialize_page(table, insertLoc[0]);
                    insertLoc[1] = p.size();
                }
                if (insertInBetween && insertLoc[1] == 0 && insertLoc[0] != 0) { //this if statment checks that if the InsertLocation in the first place in the row, we check if there is place in the page before it, so we will insert it there if yes
                    Vector<Vector<Object>> p = deserialize_page(table, insertLoc[0] - 1);
                    if (p.size() < n) { //check if the previous page has space in it
                        insertLoc[0] = insertLoc[0] - 1;
                        insertLoc[1] = p.size(); //change the location to this page
                    }
                }
                Vector<Vector<Object>> p = deserialize_page(table, insertLoc[0]);
                p.add(insertLoc[1], newRecord); //add the record to the page and the place we already got
                for (int i = 0; i < table.getIndices().size(); i++) { //add the record to the indices (explained in line 455)
                    octree tree = deserialize_index(table.getIndices().get(i));
                    String[] colNameIndex = table.getIndices().get(i).split("_");
                    tree.insert(newRecord.get(table.getColumns().indexOf(colNameIndex[1])), newRecord.get(table.getColumns().indexOf(colNameIndex[2])), newRecord.get(table.getColumns().indexOf(colNameIndex[3])), table.getPages().get(insertLoc[0]));
                    serialize_index(table.getIndices().get(i), tree);
                }  //TODO need to check
                if (p.size() == n + 1) { //check if the page was full
                    boolean makeNewPage = false; //this will tell us if we need to make a new page or not
                    Vector<Object> temp = p.remove(n); //we remove the last record in the page and put it to temp
                    serialize_page(strTableName, p, insertLoc[0]); //serialize the old page
                    for (int i = insertLoc[0] + 1; i < table.getPages().size(); i++) { //this for loop will go to every page after the page we inserted in before
                        p = deserialize_page(table, i);
                        p.add(0, temp); //we add the temp in the first place of the page
                        for (int j = 0; j < table.getIndices().size(); j++) { // we will change the reference of temp record in all the indices
                            octree tree = deserialize_index(table.getIndices().get(j));
                            String[] cols = table.getIndices().get(j).split("_");
                            //updateRef takes the cloumns value and the old refernece to change it to the new refernece
                            tree.updateRef(temp.get(table.getColumns().indexOf(cols[1])), temp.get(table.getColumns().indexOf(cols[2])),
                                    temp.get(table.getColumns().indexOf(cols[3])), table.getPages().get(i-1), table.getPages().get(i));
                            serialize_index(table.getIndices().get(j), tree);
                        }
                        if (p.size() != n + 1) { //if the page is not full again
                            serialize_page(strTableName, p, i); //serialize the page and leave
                            break;
                        } else { //if the page is full again
                            temp = p.remove(n); //remove the last record in the page
                            if (i == table.getPages().size() - 1) { //if this page is the last page in the table we set makeNewPage
                                makeNewPage = true;
                            }
                            serialize_page(strTableName, p, i); //serialize the page and continue
                        }
                    }

//                    if (insertLoc[0] + 1 > table.getPages().size() - 1 || makeNewPage) {
//                        Vector<Vector<Object>> newPage = new Vector<>();
//                        newPage.add(temp);
//                        serialize_page(strTableName, newPage, insertLoc[0] + 1);
//                        int i = insertLoc[0] + 1;
//                        table.getPages().add(strTableName + "Page" + i + ".ser");
//                        for (int j = 0; j < table.getIndices().size(); j++) {
//                            octree tree = deserialize_index(table.getIndices().get(j));
//                            String[] colNameIndex = table.getIndices().get(j).split("_");
//                            tree.updateRef(temp.get(table.getColumns().indexOf(colNameIndex[1])), temp.get(table.getColumns().indexOf(colNameIndex[2])), temp.get(table.getColumns().indexOf(colNameIndex[3])), table.getPages().get(i - 1), strTableName + "Page" + i + ".ser");
//                            serialize_index(table.getIndices().get(j), tree);
//                        }
//                    }
                    if (insertLoc[0] + 1 > table.getPages().size() - 1 || makeNewPage) { //if the place of the new record is not in the table pages (AKA: in a new page)
                        Vector<Vector<Object>> newPage = new Vector<>(); //make a new page
                        newPage.add(temp); //insert the temp to it
                        serialize_page(strTableName, newPage, table.getPages().size());
                        table.getPages().add(strTableName + "Page" + table.getPages().size() + ".ser"); //add the page to the table
                        for (int j = 0; j < table.getIndices().size(); j++) { // update the reference of the temp in the indices
                            octree tree = deserialize_index(table.getIndices().get(j));
                            String[] colNameIndex = table.getIndices().get(j).split("_");
                            tree.updateRef(temp.get(table.getColumns().indexOf(colNameIndex[1])), temp.get(table.getColumns().indexOf(colNameIndex[2])), temp.get(table.getColumns().indexOf(colNameIndex[3])), table.getPages().get(table.getPages().size()-2), strTableName + "Page" + (table.getPages().size()-1) + ".ser");
                            serialize_index(table.getIndices().get(j), tree);
                        }
                    }
                } else //if the page that we inserted the new record into is not full serialize it
                    serialize_page(strTableName, p, insertLoc[0]);
            }

            serialize_table(table); //serialize the table
        } catch (IOException | ParseException e) {
            throw new DBAppException(e.getMessage());
        }
    }
    public void deleteFromTable(String strTableName, Hashtable<String, Object> htblColNameValue)
            throws DBAppException {
        boolean useIndex = false;
        Table t = deserialize_table(strTableName);
        Set<String> keys = htblColNameValue.keySet();

        for(String key: keys){
            if(htblColNameValue.get(key) instanceof String){
                htblColNameValue.put(key,((String) htblColNameValue.get(key)).toLowerCase());
            }
        }
        Vector<Integer> index = new Vector<>();
        Vector<Object> values = new Vector<>();
        Vector<String> types = new Vector<>();
        for (String key : keys) {
            index.add(t.getColumns().indexOf(key));//index to the columns in the tobedeleted hashtable
            values.add(htblColNameValue.get(key));//values to the columns in the tobedeleted hashtable
        }
        for (String key : keys) {
            if (!t.getColumns().contains(key)) {//check if column exists
                throw new DBAppException("Column doesn't exist");
            }
        }
        for (int i = 0; i < t.getIndices().size(); i++) {
            String[] cols = t.getIndices().get(i).split("_");//check if we need to use the index or not
            if (keys.contains(cols[1]) || keys.contains(cols[2]) || keys.contains(cols[3])) {
                useIndex = true; //if at least one column in htblColNameValue exists in any of the indices use the index
                break;
            }
        }
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("metadata.csv"));
            String line1 = null;
            reader1.readLine();
            for (String key : keys) {
                while ((line1 = reader1.readLine()) != null) {
                    String[] data = line1.split(", ");
                    if (data[0].equals(strTableName) && data[1].equals(key)) {
                        types.add(data[2]);//looping over the metadata to get the types of the tobedeleted columns
                    }
                }
                reader1 = new BufferedReader(new FileReader("metadata.csv"));
            }
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) instanceof DBAppNull)
                    continue;
                if (!(types.get(i).equalsIgnoreCase((values.get(i).getClass() + "").split(" ")[1]))) {
                    throw new DBAppException("DataType incompatible");//check if the tobedeleted data types columns' types match the table
                }
            }
            reader1.close();
        } catch (IOException e) {
            throw new DBAppException(e.getMessage());
        }
        if (!useIndex) { //if we wont use the index
            int i;
            for (i = 0; i < t.getPages().size(); i++) { //we will loop on every page in the table
                Vector<Vector<Object>> page = deserialize_page(t, i);
                int j;
                for (j = 0; j < page.size(); j++) { //loop on every record in the page
                    boolean toBeDeleted = true;
                    for (int l = 0; l < index.size(); l++) { //loop on every value in the record
                        if (!page.get(j).get(index.get(l)).equals(values.get(l))) //check if there is a value in the row that is not equal to the toBeDeleted values do not delete the record
                            toBeDeleted = false;
                    }
                    if (toBeDeleted) { //if we will delete the record
                        Vector<Object> row = page.get(j); //save the record values
                        page.remove(j);//remove the record from teh page
                        for (int q = 0; q < t.getIndices().size(); q++) { //remove the record from every index aswell
                            String[] cols = t.getIndices().get(q).split("_"); //get the names of the columns in the index
                            Object x = row.get(t.getColumns().indexOf(cols[1])); //get the values of each column from the record
                            Object y = row.get(t.getColumns().indexOf(cols[2]));
                            Object z = row.get(t.getColumns().indexOf(cols[3]));
                            octree tree = deserialize_index(t.getIndices().get(q));
                            tree.deleteOnce(x, y, z, t.getPages().get(i)); //delete this record from the index
                            serialize_index(t.getIndices().get(q), tree); //serialize the index
                        }
                        if (page.size() == 0) { //if the page is empty
                            File myObj = new File(t.getPages().get(i));
                            myObj.delete(); //delete the page from the disk
                            for (int k = 0; k < t.getIndices().size(); k++) { //delete the reference of this page from the indices
                                octree tree = deserialize_index(t.getIndices().get(k));
                                tree.deleteRefFromHtbl(t.getPages().get(i)); //this function take the refernece of the page and remove it from the index's data
                                serialize_index(t.getIndices().get(k), tree);
                            }
                            t.getPages().remove(i); //remove the page from the table as well
                            i--; //this will prevent any skiping from the pages
                        } else {
                            serialize_page(strTableName, page, i);
                        }
                        j--;
                    }
                }
            }
        } else {
            Set<String> toBeDeletedRefs = new HashSet<>();//a set to store the references which we will search in to delete the entries;
            for (int i = 0; i < t.getIndices().size(); i++) {
                String[] xyz = new String[3];
                String[] cols = t.getIndices().get(i).split("_");//get the indices of the current table and split using _
                if (keys.contains(cols[1]))//if the keys contain one of the index columns, insert it into xyz[] and so on
                    xyz[0] = cols[1];
                if (keys.contains(cols[2]))
                    xyz[1] = cols[2];
                if (keys.contains(cols[3]))
                    xyz[2] = cols[3];
                if (xyz[0] != null || xyz[1] != null || xyz[2] != null) {
                    Object[] xyzVal = new Object[3];
                    boolean partial = false;
                    for (int j = 0; j < xyz.length; j++) {
                        if (xyz[j] == null) {
                            xyzVal[j] = new DBAppNull();//if any of the xyz[] are nulls, switch with DBAppNull
                            partial = true;
                        } else
                            xyzVal[j] = htblColNameValue.get(xyz[j]);
                    }
                    octree tree = deserialize_index(t.getIndices().get(i));
                    Vector<Object> ref = tree.search(xyzVal[0], xyzVal[1], xyzVal[2], partial);//return all refs that match xyz
                    if (toBeDeletedRefs.size() == 0)
                        for (int j = 0; j < ref.size(); j++)
                            toBeDeletedRefs.add((String) ref.get(j));//insert the refs again into a set to remove duplicates
                    else {
                        Set<String> set2 = new HashSet<>();
                        for (int j = 0; j < ref.size(); j++)
                            set2.add((String) ref.get(j));
                        toBeDeletedRefs.retainAll(set2); // retain only elements in toBeDeletedRefs that are also in set2 (intersection)
                    }
                    //serialize_index(t.getIndices().get(i),tree);
                }
            }

            for (String ref : toBeDeletedRefs) {
                Vector<Vector<Object>> page = deserialize_page(t, t.getPages().indexOf(ref));
                int j;
                for (j = 0; j < page.size(); j++) {
                    boolean toBeDeleted = true;
                    for (int l = 0; l < index.size(); l++) {
                        if (!page.get(j).get(index.get(l)).equals(values.get(l)))
                            toBeDeleted = false;
                    }
                    if (toBeDeleted) {
                        Vector<Object> row = page.get(j);
                        page.remove(j);
                        for (int q = 0; q < t.getIndices().size(); q++) {
                            String[] cols = t.getIndices().get(q).split("_");//get the index columns
                            Object x = row.get(t.getColumns().indexOf(cols[1]));//get the xyz values from the row according to the index
                            Object y = row.get(t.getColumns().indexOf(cols[2]));
                            Object z = row.get(t.getColumns().indexOf(cols[3]));
                            octree tree = deserialize_index(t.getIndices().get(q));
                            tree.deleteOnce(x, y, z, ref);//delete the row from the tree
                            serialize_index(t.getIndices().get(q), tree);
                        }
                        if (page.size() == 0) {//if the page is empty delete it from table attributes and serializable file and from trees
                            File myObj = new File(ref); //t.getPages().get(t.getPages().indexOf(ref))
                            myObj.delete();

                            for (int k = 0; k < t.getIndices().size(); k++) { //delete the referemce from the indices (exiplaned in line 669)
                                octree tree = deserialize_index(t.getIndices().get(k));
                                tree.deleteRefFromHtbl(ref);
                                serialize_index(t.getIndices().get(k), tree); //
                            }
                            t.getPages().remove(ref);
                        } else {
                            serialize_page(strTableName, page, t.getPages().indexOf(ref));
                        }
                        j--;
                    }
                }
            }
//            for(int i = 0; i < t.getIndices().size();i++){
//                String[] xyz = new String[3];
//                String[] cols = t.getIndices().get(i).split("_");
//                octree x = deserialize_index(t.getIndices().get(i));
//                if(keys.contains(cols[1]))
//                    xyz[0] = cols[1];
//                if(keys.contains(cols[2]))
//                    xyz[1] = cols[2];
//                if(keys.contains(cols[3]))
//                    xyz[2]= cols[3];
//                boolean partial=false;
//                if(xyz[0]!=null || xyz[1]!=null || xyz[2]!=null){
//                    Object[] xyzVal = new Object[3];
//                    for(int j = 0; j<xyz.length;j++){
//                        if(xyz[j]==null){
//                            xyzVal[j] = new DBAppNull();
//                            partial=true;
//                        }else{
//                            xyzVal[j] = htblColNameValue.get(xyz[j]);
//                        }
//                    }
//                    for(String ref : toBeDeletedRefs){
//                        Vector<Vector> page = deserialize_page(t, t.getPages().indexOf(ref));
//                        for(int j=0;j<page.size();j++){
//                            if(((partial&&xyz[0]==null)||page.get(j).get(t.getColumns().indexOf(xyz[0])).equals(xyzVal[0]))&&((partial&&xyz[1]==null)||page.get(j).get(t.getColumns().indexOf(xyz[1])).equals(xyzVal[1]))&&((partial&&xyz[2]==null)||page.get(j).get(t.getColumns().indexOf(xyz[2])).equals(xyzVal[2]))){
//                                x.insert(page.get(j).get(t.getColumns().indexOf(cols[1])),page.get(j).get(t.getColumns().indexOf(cols[2])),page.get(j).get(t.getColumns().indexOf(cols[3])),ref);
//                            }
//                        }
//                    }
//
//                }
//                serialize_index(t.getIndices().get(i),x);
//            }

        }
        pageFixing(t); //fix the pages references in the table
        for (int k = 0; k < t.getIndices().size();  k++) { //fix the page referneces in the indices
            octree tree = deserialize_index(t.getIndices().get(k));
            tree.updateHtbl(t.getPages());
            serialize_index(t.getIndices().get(k), tree);
        }
        serialize_table(t);
    }

    public void updateTable(String strTableName,
                            String strClusteringKeyValue,
                            Hashtable<String, Object> htblColNameValue)
            throws DBAppException {
        Table t = deserialize_table(strTableName);
        Set<String> keys = htblColNameValue.keySet();
        for(String key: keys){
            if(htblColNameValue.get(key) instanceof String){
                htblColNameValue.put(key,((String) htblColNameValue.get(key)).toLowerCase());
            }
        }
        Vector<Integer> index = new Vector<>();
        Vector<Object> values = new Vector<>();
        Vector<String> types = new Vector<>();
        boolean useIndex = false;
        for (String key : keys) {
            index.add(t.getColumns().indexOf(key));
            values.add(htblColNameValue.get(key));
        }
        for (String key : keys) {
            if (!t.getColumns().contains(key)) {
                throw new DBAppException("Column doesn't exist");
            }
        }
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("metadata.csv"));
            String line1 = null;
            reader1.readLine();
            String clusteringName = null;
            String clusteringType = null;
            Object clusterval = null;
            while ((line1 = reader1.readLine()) != null) {
                String[] data = line1.split(", ");
                for (String key : keys) {
                    if (data[0].equals(strTableName) && data[1].equals(key)) {
                        types.add(data[2]);
                    }
                    if (data[0].equals(strTableName) && data[3].equals("true")) {
                        clusteringName = data[1];
                        clusteringType = data[2];
                        Pattern doublePattern = Pattern.compile("^\\d+(\\.\\d+)?$");
                        Pattern datePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
                        Pattern intPattern = Pattern.compile("^\\d+$");
                        Matcher doubleMatcher = doublePattern.matcher(strClusteringKeyValue);
                        Matcher dateMatcher = datePattern.matcher(strClusteringKeyValue);
                        Matcher intMatcher = intPattern.matcher(strClusteringKeyValue);
                        if (data[2].equals("java.lang.Double")) {
                            if (!doubleMatcher.find()) {
                                throw new DBAppException("Data type not compatible");
                            } else {
                                clusterval = Double.parseDouble(strClusteringKeyValue);
                            }
                        } else if (data[2].equals("java.lang.Integer")) {
                            if (!intMatcher.find()) {
                                throw new DBAppException("Data type not compatible");
                            } else {
                                clusterval = Integer.parseInt(strClusteringKeyValue);
                            }
                        } else {
                            if (!dateMatcher.find()) {
                                throw new DBAppException("Data type not compatible");
                            } else {
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                clusterval = formatter.parse(strClusteringKeyValue);
                            }
                        }
                    }
                }
            }
            reader1.close();

            int clusindex = t.getColumns().indexOf(clusteringName);
            for (int i = 0; i < values.size(); i++) {
                if (!(types.get(i).equalsIgnoreCase((values.get(i).getClass() + "").split(" ")[1]))) {
                    if (values.get(i) instanceof DBAppNull)
                        continue;
                    throw new DBAppException("DataType incompatible");
                }
            }
            maxMinCheck(htblColNameValue, strTableName);
            int i;
            String[] cols = null;
            for (i = 0; i < t.getIndices().size(); i++) {
                cols = t.getIndices().get(i).split("_");
                if (cols[1].equals(clusteringName) || cols[2].equals(clusteringName) || cols[3].equals(clusteringName)) {
                    useIndex = true;
                    break;
                }
            }

            if (!useIndex) {
                for (i = 0; i < t.getPages().size(); i++) {
                    Vector<Vector<Object>> p = deserialize_page(t, i);
                    int indexx = binarySearch(p, clusterval, clusindex);
                    if (indexx != -1) {
                        Vector<Object> row=new Vector<>();
                        for(int j=0;j<p.get(indexx).size();j++){
                            row.add(p.get(indexx).get(j));
                        }
                        for (int j = 0; j < values.size(); j++) {
                            p.get(indexx).set(index.get(j), values.get(j));
                        }
                        for (int k = 0; k < t.getIndices().size(); k++) {
                            octree tree = deserialize_index(t.getIndices().get(k));
                            cols = t.getIndices().get(k).split("_");
                            tree.update(row.get(t.getColumns().indexOf(cols[1])), row.get(t.getColumns().indexOf(cols[2])), row.get(t.getColumns().indexOf(cols[3])), (String) t.getPages().get(i), p.get(indexx).get(t.getColumns().indexOf(cols[1])), p.get(indexx).get(t.getColumns().indexOf(cols[2])), p.get(indexx).get(t.getColumns().indexOf(cols[3])), (String) t.getPages().get(i));
                            serialize_index(t.getIndices().get(k), tree);
                        }
                        serialize_page(strTableName, p, i);
                        break;
                    }
                }
            } else {
                octree tree = deserialize_index(t.getIndices().get(i));
                Vector<Object> ref = null;
                if (cols[1].equals(clusteringName)) {
                    ref = tree.search(clusterval, null, null, true);
                } else if (cols[2].equals(clusteringName)) {
                    ref = tree.search(null, clusterval, null, true);
                } else {
                    ref = tree.search(null, null, clusterval, true);
                }

                for (i = 0; i < ref.size(); i++) {
                    Vector<Vector<Object>> page = deserialize_page(t, t.getPages().indexOf(ref.get(i)));
                    int j = binarySearch(page, clusterval, clusindex);
                    if (j != -1) {
                        Vector row = page.get(j);
                        for (int k = 0; k < values.size(); k++) {
                            page.get(j).set(index.get(k), values.get(k));
                        }
                        for (int k = 0; k < t.getIndices().size(); k++) {
                            tree = deserialize_index(t.getIndices().get(k));
                            cols = t.getIndices().get(k).split("_");
                            tree.update(row.get(t.getColumns().indexOf(cols[1])), row.get(t.getColumns().indexOf(cols[2])), row.get(t.getColumns().indexOf(cols[3])), (String) ref.get(i), page.get(j).get(t.getColumns().indexOf(cols[1])), page.get(j).get(t.getColumns().indexOf(cols[2])), page.get(j).get(t.getColumns().indexOf(cols[3])), (String) ref.get(i));
                            serialize_index(t.getIndices().get(k), tree);
                        }
                        serialize_page(strTableName, page, i);
                        break;
                    }
                }
            }
            serialize_table(t);
        } catch (IOException | ParseException e) {
            throw new DBAppException(e.getMessage());
        }
    }

    public Iterator selectFromTable(SQLTerm[] arrSQLTerms,
                                    String[] strarrOperators)
            throws DBAppException{
    ArrayList<Vector> res= new ArrayList<>();
        String tablename= arrSQLTerms[0]._strTableName;
    for(int i=0;i<arrSQLTerms.length;i++){
        if(!arrSQLTerms[i]._strTableName.equals(tablename)){
            throw new DBAppException("there are two or more tables with different names");
        }
        if(!(arrSQLTerms[i]._strOperator.equals("=")||arrSQLTerms[i]._strOperator.equals("!=")||arrSQLTerms[i]._strOperator.equals(">")||arrSQLTerms[i]._strOperator.equals(">=")||arrSQLTerms[i]._strOperator.equals("<")||arrSQLTerms[i]._strOperator.equals("<="))){
            throw new DBAppException("one of the sql operators are not valid");
        }
    }
    if(arrSQLTerms.length!=strarrOperators.length+1){
        throw new DBAppException("not enough operators");
    }
    File x= new File(arrSQLTerms[0]._strTableName+".ser");
    if(!x.exists()){
        throw new DBAppException("table does not exist");
    }
    for(int i=0;i<strarrOperators.length;i++){
        if(!(strarrOperators[i].equals("OR")||strarrOperators[i].equals("AND")||strarrOperators[i].equals("XOR"))){
            throw new DBAppException("one or more of the operators are not valid");
        }
    }

    try{
        Vector<String> types = new Vector<>();
        for(SQLTerm sql: arrSQLTerms){
            types.add((sql._objValue.getClass() + "").split(" ")[1]);
        }
        BufferedReader reader1 = new BufferedReader(new FileReader("metadata.csv"));
            for(int i = 0; i<arrSQLTerms.length;i++) {
                reader1 = new BufferedReader(new FileReader("metadata.csv"));
                String line1 = null;
                reader1.readLine();
                boolean columnFound = false;
                if(arrSQLTerms[i]._strOperator.equals("=") && arrSQLTerms[i]._objValue instanceof DBAppNull)
                    continue;
                if (arrSQLTerms[i]._objValue instanceof String)
                    arrSQLTerms[i]._objValue = ((String) arrSQLTerms[i]._objValue).toLowerCase();

                while ((line1 = reader1.readLine()) != null) {
                    String[] data = line1.split(", ");
                    if (data[0].equals(arrSQLTerms[i]._strTableName) && data[1].equals(arrSQLTerms[i]._strColumnName)) {
                        columnFound = true;
                        if (!data[2].equalsIgnoreCase(types.get(i))) {
                            throw new DBAppException("Data type incompatible");
                        }
                    }
                }
                if(!columnFound){
                    throw new DBAppException("One or more of the column names in SQLTerms is wrong");
                }
            }


        reader1.close();
        boolean useindex =false;
        Table t=deserialize_table(arrSQLTerms[0]._strTableName);
        if(arrSQLTerms.length == 3){

              for(int j=0;j<t.getIndices().size();j++){
                  String[] col= t.getIndices().get(j).split("_");
                  Vector<String> cols= new Vector<>();
                  Collections.addAll(cols, col);
                  if(cols.contains(arrSQLTerms[0]._strColumnName)&&cols.contains(arrSQLTerms[1]._strColumnName)&&cols.contains(arrSQLTerms[2]._strColumnName)&&strarrOperators[0].equals("AND")&&strarrOperators[1].equals("AND")){
                      useindex=true;
                      break;
                  }
              }

        }

        if(!useindex){
            for(int i=0;i<t.getPages().size();i++){
                Vector<Vector<Object>> page= deserialize_page(t,i);
                for(int j=0;j<page.size();j++){
                    boolean alltrue=true;
                    switch(arrSQLTerms[0]._strOperator){
                        case("="): if(!(page.get(j).get(t.getColumns().indexOf(arrSQLTerms[0]._strColumnName)).equals(arrSQLTerms[0]._objValue))){alltrue=false;}break;
                        case("!="): if((page.get(j).get(t.getColumns().indexOf(arrSQLTerms[0]._strColumnName)).equals(arrSQLTerms[0]._objValue))){alltrue=false;}break;
                        case(">"): if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[0]._strColumnName))).compareTo(arrSQLTerms[0]._objValue)>0)){alltrue=false;}break;
                        case(">="):if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[0]._strColumnName))).compareTo(arrSQLTerms[0]._objValue)>0||(page.get(j).get(t.getColumns().indexOf(arrSQLTerms[0]._strColumnName))).equals(arrSQLTerms[0]._objValue))){alltrue=false;}break;
                        case("<"):if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[0]._strColumnName))).compareTo(arrSQLTerms[0]._objValue)<0)){alltrue=false;}break;
                        case("<="):if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[0]._strColumnName))).compareTo(arrSQLTerms[0]._objValue)<0||(page.get(j).get(t.getColumns().indexOf(arrSQLTerms[0]._strColumnName))).equals(arrSQLTerms[0]._objValue))){alltrue=false;}break;
                    }
                    for(int k=1;k<arrSQLTerms.length;k++){
                        if(strarrOperators[k-1].equals("AND")){
                            switch(arrSQLTerms[k]._strOperator){
                                case("="): if(!(page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName)).equals(arrSQLTerms[k]._objValue))){alltrue=alltrue&&false;}else{alltrue=alltrue&&true;}break;
                                case("!="): if((page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName)).equals(arrSQLTerms[k]._objValue))){alltrue=alltrue&&false;}else{alltrue=alltrue&&true;}break;
                                case(">"): if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).compareTo(arrSQLTerms[k]._objValue)>0)){alltrue=alltrue&&false;}else{alltrue=alltrue&&true;}break;
                                case(">="):if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).compareTo(arrSQLTerms[k]._objValue)>0||(page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).equals(arrSQLTerms[k]._objValue))){alltrue=alltrue&&false;}else{alltrue=alltrue&&true;}break;
                                case("<"):if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).compareTo(arrSQLTerms[k]._objValue)<0)){alltrue=alltrue&&false;}else{alltrue=alltrue&&true;}break;
                                case("<="):if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).compareTo(arrSQLTerms[k]._objValue)<0||(page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).equals(arrSQLTerms[k]._objValue))){alltrue=alltrue&&false;}else{alltrue=alltrue&&true;}break;
                            }
                        }
                        else if (strarrOperators[k-1].equals("OR")){
                            switch(arrSQLTerms[k]._strOperator){
                                case("="): if(!(page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName)).equals(arrSQLTerms[k]._objValue))){alltrue=alltrue||false;}else{alltrue=alltrue||true;}break;
                                case("!="): if((page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName)).equals(arrSQLTerms[k]._objValue))){alltrue=alltrue||false;}else{alltrue=alltrue||true;}break;
                                case(">"): if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).compareTo(arrSQLTerms[k]._objValue)>0)){alltrue=alltrue||false;}else{alltrue=alltrue||true;}break;
                                case(">="):if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).compareTo(arrSQLTerms[k]._objValue)>0||(page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).equals(arrSQLTerms[k]._objValue))){alltrue=alltrue||false;}else{alltrue=alltrue||true;}break;
                                case("<"):if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).compareTo(arrSQLTerms[k]._objValue)<0)){alltrue=alltrue||false;}else{alltrue=alltrue||true;}break;
                                case("<="):if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).compareTo(arrSQLTerms[k]._objValue)<0||(page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).equals(arrSQLTerms[k]._objValue))){alltrue=alltrue||false;}else{alltrue=alltrue||true;}break;
                            }
                        }
                        else{
                            switch(arrSQLTerms[k]._strOperator){
                                case("="): if(!(page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName)).equals(arrSQLTerms[k]._objValue))){alltrue=alltrue^false;} else{alltrue=alltrue^true;}break;
                                case("!="): if((page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName)).equals(arrSQLTerms[k]._objValue))){alltrue=alltrue^false;}else{alltrue=alltrue^true;}break;
                                case(">"): if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).compareTo(arrSQLTerms[k]._objValue)>0)){alltrue=alltrue^false;}else{alltrue=alltrue^true;}break;
                                case(">="):if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).compareTo(arrSQLTerms[k]._objValue)>0||(page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).equals(arrSQLTerms[k]._objValue))){alltrue=alltrue^false;}else{alltrue=alltrue^true;}break;
                                case("<"):if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).compareTo(arrSQLTerms[k]._objValue)<0)){alltrue=alltrue^false;}else{alltrue=alltrue^true;}break;
                                case("<="):if(!(((Comparable)page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).compareTo(arrSQLTerms[k]._objValue)<0||(page.get(j).get(t.getColumns().indexOf(arrSQLTerms[k]._strColumnName))).equals(arrSQLTerms[k]._objValue))){alltrue=alltrue^false;}else{alltrue=alltrue^true;}break;
                            }
                        }


                    }
                    if(alltrue){
                        res.add(page.get(j));
                    }
                }
            }
        }else{
            Set<String> toBeSelected = new HashSet<>();
            int index = 0;
            for(int j=0;j<t.getIndices().size();j++){
                String[] col= t.getIndices().get(j).split("_");
                Vector<String> cols= new Vector<>();
                Collections.addAll(cols, col);
                if(cols.contains(arrSQLTerms[0]._strColumnName)&&cols.contains(arrSQLTerms[1]._strColumnName)&&cols.contains(arrSQLTerms[2]._strColumnName)&&strarrOperators[0].equals("AND")&&strarrOperators[1].equals("AND")){
                    index = j;
                    break;
                }
            }
            octree tree = deserialize_index(t.getIndices().get(index));
            Object xObj = null;String xOp = "";
            Object yObj = null;String yOp = "";
            Object zObj = null;String zOp = "";
            int[] colIndices = new int[3];
            for(int i = 0; i<arrSQLTerms.length;i++){
               String[] cols = t.getIndices().get(index).split("_");
               if(cols[1].equals(arrSQLTerms[i]._strColumnName)) {
                   xObj = arrSQLTerms[i]._objValue;
                   xOp = arrSQLTerms[i]._strOperator;
                   colIndices[0] = t.getColumns().indexOf(arrSQLTerms[i]._strColumnName);
               }
               if(cols[2].equals(arrSQLTerms[i]._strColumnName)) {
                   yObj = arrSQLTerms[i]._objValue;
                   yOp = arrSQLTerms[i]._strOperator;
                   colIndices[1] = t.getColumns().indexOf(arrSQLTerms[i]._strColumnName);
               }
               if(cols[3].equals(arrSQLTerms[i]._strColumnName)) {
                   zObj = arrSQLTerms[i]._objValue;
                   zOp = arrSQLTerms[i]._strOperator;
                   colIndices[2] = t.getColumns().indexOf(arrSQLTerms[i]._strColumnName);
               }
            }
            Vector<String> references = tree.searchSelect(xObj,yObj,zObj,xOp,yOp,zOp);
            for(String ref: references){
                toBeSelected.add(ref);
            }
            Map<String, Predicate<Object>> conditions = parseConditions(new String[]{xOp,yOp,zOp},new Object[]{xObj,yObj,zObj},new String[]{"x","y","z"});
            Predicate<Object> conditionX = conditions.get("x");
            Predicate<Object> conditionY = conditions.get("y");
            Predicate<Object> conditionZ = conditions.get("z");
            for(String ref: toBeSelected){
                Vector<Vector<Object>> page = deserialize_page(t,t.getPages().indexOf(ref));
                for(int i = 0; i<page.size();i++){
                    Object x_obj = page.get(i).get(colIndices[0]);
                    Object y_obj = page.get(i).get(colIndices[1]);
                    Object z_obj = page.get(i).get(colIndices[2]);
                    if(x_obj instanceof String)
                        x_obj = ((String)x_obj).toLowerCase();
                    if(y_obj instanceof String)
                        y_obj = ((String)y_obj).toLowerCase();
                    if(z_obj instanceof String)
                        z_obj = ((String)z_obj).toLowerCase();
                    if(conditionX.test(x_obj)&&conditionY.test(y_obj)&&conditionZ.test(z_obj))
                        res.add(page.get(i));
                }
            }
        }
        Iterator<Vector> result=res.iterator();
        return result;
    }catch(IOException e){
        throw new DBAppException(e.getMessage());
    }
    }

//    public static void main(String[] args) throws DBAppException, IOException, ParseException {
//        String strTableName = "Student";
//        DBApp dbApp = new DBApp();
////        dbApp.init();
//        Hashtable htblColNameType = new Hashtable();
//        htblColNameType.put("id", "java.lang.Integer");
//        htblColNameType.put("name", "java.lang.String");
//        htblColNameType.put("gpa", "java.lang.Double");
//        htblColNameType.put("Bday", "java.util.Date");
//        Hashtable htblColNameMin = new Hashtable();
//        htblColNameMin.put("id", "1");
//        htblColNameMin.put("name", "AAAA");
//        htblColNameMin.put("gpa", "0.7");
//        htblColNameMin.put("Bday", "1200-06-22");
//        Hashtable htblColNameMax = new Hashtable();
//        htblColNameMax.put("id", "1000");
//        htblColNameMax.put("name", "ZZZZZZZZZZZZZZ");
//        htblColNameMax.put("gpa", "4.0");
//        htblColNameMax.put("Bday", "3000-06-22");
//
//
// //       dbApp.createTable(strTableName, "id", htblColNameType, htblColNameMin, htblColNameMax);
//        Hashtable htblColNameValue = new Hashtable();
//        htblColNameValue.put("id", 5);
//        htblColNameValue.put("name", "Ahmed Noor");
//        htblColNameValue.put("gpa", 0.7);
//        htblColNameValue.put("Bday", new Date(102, 8, 2));
//        Hashtable htblColNameValue1 = new Hashtable();
//        htblColNameValue1.put("id", Integer.valueOf(4));
//        htblColNameValue1.put("name", "Kika Mima");
//        htblColNameValue1.put("gpa", Double.valueOf(0.8));
//        htblColNameValue1.put("Bday", new Date(0, 9, 1));
//        Hashtable htblColNameValue2 = new Hashtable();
//        htblColNameValue2.put("id", 3);
//        htblColNameValue2.put("name", "Safwat noob");
//        htblColNameValue2.put("gpa", Double.valueOf(3.9));
//        htblColNameValue2.put("Bday", new Date(100, 5, 30));
//        Hashtable htblColNameValue3 = new Hashtable();
//        htblColNameValue3.put("id", Integer.valueOf(2));
//        htblColNameValue3.put("name", new DBAppNull());
//        htblColNameValue3.put("gpa", new DBAppNull());
//        htblColNameValue3.put("Bday", new Date(103, 5, 30));
//        Hashtable htblColNameValue4 = new Hashtable();
//        htblColNameValue4.put("gpa", Double.valueOf(0.98));
//        htblColNameValue4.put("id", Integer.valueOf(1));
//        htblColNameValue4.put("Bday", new Date(19, 5, 30));
//        htblColNameValue4.put("name", "Layla");
//        String[] cols = new String[3];
//        cols[0] = "name";
//        cols[1] = "gpa";
//        cols[2] = "Bday";
////        dbApp.createIndex(strTableName, cols);
////        dbApp.insertIntoTable(strTableName, htblColNameValue);
////        dbApp.insertIntoTable(strTableName, htblColNameValue1);
////        dbApp.insertIntoTable(strTableName, htblColNameValue2);
////        dbApp.insertIntoTable(strTableName, htblColNameValue3);
////        dbApp.insertIntoTable(strTableName, htblColNameValue4);
//
////        Hashtable tobedeleted= new Hashtable<>();
////        tobedeleted.put("name",new DBAppNull());
////        dbApp.deleteFromTable("Student",tobedeleted);
//        dbApp.insertIntoTable(strTableName, htblColNameValue3);
////        tobedeleted= new Hashtable<>();
////        tobedeleted.put("gpa",Double.valueOf(0.94));
////        dbApp.deleteFromTable("Student",tobedeleted);
//        Hashtable<String,Object> tobeUpdated = new Hashtable<>();
//        tobeUpdated.put("name","KIKAMIMA");
//        tobeUpdated.put("Bday",new Date(100,5,30));
//        dbApp.updateTable(strTableName,"2",tobeUpdated);
//
//        SQLTerm[] arrSQLTerms;
//        arrSQLTerms = new SQLTerm[1];
//        for(int i = 0; i<arrSQLTerms.length;i++){
//            arrSQLTerms[i] = new SQLTerm();
//        }
////        arrSQLTerms[0]._strTableName = "Student";
////        arrSQLTerms[0]._strColumnName= "name";
////        arrSQLTerms[0]._strOperator = "=";
////        arrSQLTerms[0]._objValue = "Maged";
////        arrSQLTerms[1]._strTableName = "Student";
////        arrSQLTerms[1]._strColumnName= "gpa";
////        arrSQLTerms[1]._strOperator = "<";
////        arrSQLTerms[1]._objValue = new Double( 1.5 );
//        arrSQLTerms[0]._strTableName = "Student";
//        arrSQLTerms[0]._strColumnName= "name";
//        arrSQLTerms[0]._strOperator = "=";
//        arrSQLTerms[0]._objValue = new DBAppNull();
//        String[]strarrOperators = new String[0];
////        strarrOperators[0] = "OR";
////        strarrOperators[1] = "OR";
//        Iterator itr = dbApp.selectFromTable(arrSQLTerms,strarrOperators);
//        for (Iterator it = itr; it.hasNext(); ) {
//            Object v = it.next();
//            System.out.println(v);
//        }
//        Table t = deserialize_table("Student");
//        Vector page = deserialize_page(t, 0);
//        Vector page1 = deserialize_page(t, 1);
//        Vector page2 = deserialize_page(t, 2);
//        System.out.println(page);
//        System.out.println(page1);
//        System.out.println(page2);
//        octree tree = deserialize_index(t.getIndices().get(0));
////        System.out.println(page);
////        System.out.println(page1);
////        System.out.println(page2);
//        System.out.println(tree.htblRefernces);
//        System.out.println(tree);
//
////
////
////        System.out.println(t.getPages());
//    }
    public static void main(String[] args) throws DBAppException {
        String strTableName = "Student";
        DBApp dbApp = new DBApp();
        dbApp.init();
        Hashtable htblColNameType = new Hashtable();
        htblColNameType.put("id", "java.lang.Integer");
        htblColNameType.put("name", "java.lang.String");
        htblColNameType.put("gpa", "java.lang.Double");
        htblColNameType.put("Birthday", "java.util.Date");
        htblColNameType.put("collageName","java.lang.String");
        htblColNameType.put("takesMath","java.lang.String");
        htblColNameType.put("mathGrade","java.lang.Integer");
        Hashtable htblColNameMin = new Hashtable();
        htblColNameMin.put("id", "1");
        htblColNameMin.put("name", "AAAA");
        htblColNameMin.put("gpa", "0.7");
        htblColNameMin.put("Birthday", "1200-06-22");
        htblColNameMin.put("collageName", "a");
        htblColNameMin.put("takesMath", "aaa");
        htblColNameMin.put("mathGrade","0");
        Hashtable htblColNameMax = new Hashtable();
        htblColNameMax.put("id", "1000");
        htblColNameMax.put("name", "ZZZZZZZZZZZZZZ");
        htblColNameMax.put("gpa", "4.0");
        htblColNameMax.put("Birthday", "3000-06-22");
        htblColNameMax.put("collageName", "zzzzzzzzzz");
        htblColNameMax.put("takesMath", "zzz");
        htblColNameMax.put("mathGrade","100");
        dbApp.createTable(strTableName, "id", htblColNameType, htblColNameMin, htblColNameMax);


        Hashtable htblColNameValue = new Hashtable();
        htblColNameValue.put("id", 5);
        htblColNameValue.put("name", "Ahmed Noor");
        htblColNameValue.put("gpa", 0.7);
        htblColNameValue.put("Birthday", new Date(102, 8, 2));
        htblColNameValue.put("collageName","GUC");
        htblColNameValue.put("takesMath","yes");
        htblColNameValue.put("mathGrade",44);
        Hashtable htblColNameValue1 = new Hashtable();
        htblColNameValue1.put("id", Integer.valueOf(4));
        htblColNameValue1.put("name", "Kika Mima");
        htblColNameValue1.put("gpa", Double.valueOf(0.8));
        htblColNameValue1.put("Birthday", new Date(0, 9, 1));
        htblColNameValue1.put("collageName","AUC");
        htblColNameValue1.put("takesMath","yes");
        htblColNameValue1.put("mathGrade",90);
        Hashtable htblColNameValue2 = new Hashtable();
        htblColNameValue2.put("id", 3);
        htblColNameValue2.put("name", "Safwat noob");
        htblColNameValue2.put("gpa", Double.valueOf(3.9));
        htblColNameValue2.put("Birthday", new Date(100, 5, 30));
        htblColNameValue2.put("collageName","GUC");
        htblColNameValue2.put("takesMath","yes");
        htblColNameValue2.put("mathGrade",44);
        Hashtable htblColNameValue3 = new Hashtable();
        htblColNameValue3.put("id", Integer.valueOf(2));
        htblColNameValue3.put("name", new DBAppNull());
        htblColNameValue3.put("gpa", 1.5);
        htblColNameValue3.put("Birthday", new Date(103, 5, 30));
        htblColNameValue3.put("collageName","GUC");
        htblColNameValue3.put("takesMath","no");
//        htblColNameValue3.put("mathGrade",44);
        Hashtable htblColNameValue4 = new Hashtable();
        htblColNameValue4.put("gpa", Double.valueOf(0.98));
        htblColNameValue4.put("id", Integer.valueOf(1));
        htblColNameValue4.put("Birthday", new Date(19, 5, 30));
        htblColNameValue4.put("name", "Layla");
        htblColNameValue4.put("collageName","GUC");
        htblColNameValue4.put("takesMath","yes");
        htblColNameValue4.put("mathGrade",44);
        Hashtable htblColNameValue5 = new Hashtable();
        htblColNameValue5.put("gpa", Double.valueOf(1.5));
        htblColNameValue5.put("id", Integer.valueOf(6));
        htblColNameValue5.put("Birthday", new Date(40, 3, 10));
        htblColNameValue5.put("name", "Amir");
        htblColNameValue5.put("collageName","BUE");
        htblColNameValue5.put("takesMath","no");
//        htblColNameValue5.put("mathGrade",44);
        Hashtable htblColNameValue6 = new Hashtable();
        htblColNameValue6.put("gpa", Double.valueOf(0.99));
        htblColNameValue6.put("id", Integer.valueOf(8));
        htblColNameValue6.put("Birthday", new Date(33, 2, 20));
        htblColNameValue6.put("name", "Hazem");
        htblColNameValue6.put("collageName","BUE");
        htblColNameValue6.put("takesMath","yes");
        htblColNameValue6.put("mathGrade",89);

        String[] cols = new String[3];
        cols[0] = "name";
        cols[1] = "gpa";
        cols[2] = "Birthday";
        dbApp.createIndex(strTableName, cols);
        cols[0] = "collageName";
        cols[1] = "takesMath";
        cols[2] = "mathGrade";
        dbApp.createIndex(strTableName, cols);
        dbApp.insertIntoTable(strTableName, htblColNameValue);
        dbApp.insertIntoTable(strTableName, htblColNameValue1);
        dbApp.insertIntoTable(strTableName, htblColNameValue2);
        dbApp.insertIntoTable(strTableName, htblColNameValue3);
        dbApp.insertIntoTable(strTableName, htblColNameValue4);
        dbApp.insertIntoTable(strTableName, htblColNameValue5);
        dbApp.insertIntoTable(strTableName, htblColNameValue6);
        System.out.println("Insert test: -----------------------------------------");
        Table t = deserialize_table(strTableName);
        for(String ref : t.getPages())
            System.out.println(deserialize_page(t,t.getPages().indexOf(ref)));
        for(String ref : t.getIndices())
            System.out.println(deserialize_index(ref));
        System.out.println("-----------------------------------------------------------------");


        System.out.println("selecting duplicate without index test: --------------------");
        System.out.println("select where collageName = GUC");
        SQLTerm[] arrSQLTerms = new SQLTerm[1];
        for(int i = 0; i<arrSQLTerms.length;i++){
            arrSQLTerms[i] = new SQLTerm();
        }
        arrSQLTerms[0]._strTableName = "Student";
        arrSQLTerms[0]._strColumnName= "collageName";
        arrSQLTerms[0]._strOperator = "=";
        arrSQLTerms[0]._objValue = "GUC";
        String[]strarrOperators = new String[0];
        Iterator itr = dbApp.selectFromTable(arrSQLTerms,strarrOperators);
        for (Iterator it = itr; it.hasNext(); ) {
            Object v = it.next();
            System.out.println(v);
        }
        System.out.println("----------------------------------------");


        System.out.println("select duplicate null using index test:---------------------");
        System.out.println("select where collageName != AUC AND takesMath != yes AND mathGrade = null");
        arrSQLTerms = new SQLTerm[3];
        for(int i = 0; i<arrSQLTerms.length;i++){
            arrSQLTerms[i] = new SQLTerm();
        }
        arrSQLTerms[0]._strTableName = "Student";
        arrSQLTerms[0]._strColumnName= "collageName";
        arrSQLTerms[0]._strOperator = "!=";
        arrSQLTerms[0]._objValue = "AUC";
        arrSQLTerms[1]._strTableName = "Student";
        arrSQLTerms[1]._strColumnName= "takesMath";
        arrSQLTerms[1]._strOperator = "!=";
        arrSQLTerms[1]._objValue = "yes";
        arrSQLTerms[2]._strTableName = "Student";
        arrSQLTerms[2]._strColumnName= "mathGrade";
        arrSQLTerms[2]._strOperator = "=";
        arrSQLTerms[2]._objValue = new DBAppNull();
        strarrOperators = new String[2];
        strarrOperators[0] = "AND";
        strarrOperators[1] = "AND";
        itr = dbApp.selectFromTable(arrSQLTerms,strarrOperators);
        for (Iterator it = itr; it.hasNext(); ) {
            Object v = it.next();
            System.out.println(v);
        }
        System.out.println("-----------------------------------");


        System.out.println("select duplicate and null without using index test:---------------------");
        System.out.println("select where collageName != AUC AND takesMath = yes OR mathGrade = null");
        arrSQLTerms = new SQLTerm[3];
        for(int i = 0; i<arrSQLTerms.length;i++){
            arrSQLTerms[i] = new SQLTerm();
        }
        arrSQLTerms[0]._strTableName = "Student";
        arrSQLTerms[0]._strColumnName= "collageName";
        arrSQLTerms[0]._strOperator = "!=";
        arrSQLTerms[0]._objValue = "AUC";
        arrSQLTerms[1]._strTableName = "Student";
        arrSQLTerms[1]._strColumnName= "takesMath";
        arrSQLTerms[1]._strOperator = "=";
        arrSQLTerms[1]._objValue = "yes";
        arrSQLTerms[2]._strTableName = "Student";
        arrSQLTerms[2]._strColumnName= "mathGrade";
        arrSQLTerms[2]._strOperator = "=";
        arrSQLTerms[2]._objValue = new DBAppNull();
        strarrOperators = new String[2];
        strarrOperators[0] = "AND";
        strarrOperators[1] = "OR";
        itr = dbApp.selectFromTable(arrSQLTerms,strarrOperators);
        for (Iterator it = itr; it.hasNext(); ) {
            Object v = it.next();
            System.out.println(v);
        }
        System.out.println("-----------------------------------");


        Hashtable tobedeleted= new Hashtable<>();
        tobedeleted.put("collageName","AUC");
        dbApp.deleteFromTable("Student",tobedeleted);
        System.out.println("normal deleting test:--------------------------");
        System.out.println("delete where collageName = AUC");
        t = deserialize_table(strTableName);
        for(String ref : t.getPages())
            System.out.println(deserialize_page(t,t.getPages().indexOf(ref)));
        for(String ref : t.getIndices()) {
            System.out.println(deserialize_index(ref).htblRefernces);
            System.out.println(deserialize_index(ref));
        }
        System.out.println("-----------------------------------------------------------------");


        System.out.println("delete multi rows at once test:----------------");
        System.out.println("select where collageName = BUE");
        tobedeleted= new Hashtable<>();
        tobedeleted.put("collageName","BUE");
        dbApp.deleteFromTable("Student",tobedeleted);
        t = deserialize_table(strTableName);
        for(String ref : t.getPages())
            System.out.println(deserialize_page(t,t.getPages().indexOf(ref)));
        for(String ref : t.getIndices()) {
            System.out.println(deserialize_index(ref).htblRefernces);
            System.out.println(deserialize_index(ref));
        }
        System.out.println("-----------------------------------------------------------------");


        System.out.println("deleting a whole page test:--------------------------");
        System.out.println("delete where name = safwat noob");
        tobedeleted= new Hashtable<>();
        tobedeleted.put("name","safwat noob");
        dbApp.deleteFromTable("Student",tobedeleted);
        t = deserialize_table(strTableName);
        for(String ref : t.getPages())
            System.out.println(deserialize_page(t,t.getPages().indexOf(ref)));
        for(String ref : t.getIndices()) {
            System.out.println(deserialize_index(ref).htblRefernces);
            System.out.println(deserialize_index(ref));
        }
        System.out.println("-----------------------------------------------------------------");


        System.out.println("deleting duplicate using index with complete query test:--------------------------");
        System.out.println("delete where collageName = GUC && takesMath = yes && grade = 44");
        tobedeleted= new Hashtable<>();
        tobedeleted.put("collageName","guc");
        tobedeleted.put("takesMath","yes");
        tobedeleted.put("mathGrade",44);
        dbApp.deleteFromTable("Student",tobedeleted);
        t = deserialize_table(strTableName);
        for(String ref : t.getPages())
            System.out.println(deserialize_page(t,t.getPages().indexOf(ref)));
        for(String ref : t.getIndices()) {
            System.out.println(deserialize_index(ref).htblRefernces);
            System.out.println(deserialize_index(ref));
        }
        System.out.println("-----------------------------------------------------------------");

        System.out.println("deleting duplicate using index with complete query test:--------------------------");
        System.out.println("delete where name = null");
        tobedeleted= new Hashtable<>();
        tobedeleted.put("name",new DBAppNull());
        dbApp.deleteFromTable("Student",tobedeleted);
        t = deserialize_table(strTableName);
        for(String ref : t.getPages())
            System.out.println(deserialize_page(t,t.getPages().indexOf(ref)));
        for(String ref : t.getIndices()) {
            System.out.println(deserialize_index(ref).htblRefernces);
            System.out.println(deserialize_index(ref));
        }
        System.out.println("-----------------------------------------------------------------");


        System.out.println("Inserting records back:-------------------");
        dbApp.insertIntoTable(strTableName, htblColNameValue1);
        dbApp.insertIntoTable(strTableName, htblColNameValue5);
        dbApp.insertIntoTable(strTableName, htblColNameValue3);
        dbApp.insertIntoTable(strTableName, htblColNameValue6);
        dbApp.insertIntoTable(strTableName, htblColNameValue4);
        dbApp.insertIntoTable(strTableName, htblColNameValue2);
        dbApp.insertIntoTable(strTableName, htblColNameValue);
        t = deserialize_table(strTableName);
        for(String ref : t.getPages())
            System.out.println(deserialize_page(t,t.getPages().indexOf(ref)));
        for(String ref : t.getIndices()) {
            System.out.println(deserialize_index(ref).htblRefernces);
            System.out.println(deserialize_index(ref));
        }
        System.out.println("---------------------------------------------");

        System.out.println("deleting duplicates using index with complete query test:--------------------------");
        System.out.println("delete where id = 3  ----- inserted him again after the test");
        tobedeleted= new Hashtable<>();
        tobedeleted.put("id",3);
        dbApp.deleteFromTable("Student",tobedeleted);
        t = deserialize_table(strTableName);
        for(String ref : t.getPages())
            System.out.println(deserialize_page(t,t.getPages().indexOf(ref)));
        for(String ref : t.getIndices()) {
            System.out.println(deserialize_index(ref).htblRefernces);
            System.out.println(deserialize_index(ref));
        }
        dbApp.insertIntoTable(strTableName, htblColNameValue2);
        System.out.println("-----------------------------------------------------------------");

        System.out.println("update null test:------------------------------------------");
        System.out.println("update id = 2 to name = maged, takesMath = yes, mathGrade = 60");
        Hashtable<String,Object> tobeUpdated = new Hashtable<>();
        tobeUpdated.put("name","Maged");
        tobeUpdated.put("Birthday",new Date(99,1,2));
        tobeUpdated.put("takesMath","yes");
        tobeUpdated.put("mathGrade",60);
        dbApp.updateTable(strTableName,"2",tobeUpdated);
        t = deserialize_table(strTableName);
        for(String ref : t.getPages())
            System.out.println(deserialize_page(t,t.getPages().indexOf(ref)));
        for(String ref : t.getIndices()) {
            System.out.println(deserialize_index(ref).htblRefernces);
            System.out.println(deserialize_index(ref));
        }
        System.out.println("-----------------------------------------------------------------");

        System.out.println("update duplicate and update to null test:------------------------");
        System.out.println("update id = 3 to name = soubra, takesMath = no, mathGrade = null , AUC");
        tobeUpdated.put("name","soubra");
        tobeUpdated.put("takesMath","no");
        tobeUpdated.put("mathGrade",new DBAppNull());
        tobeUpdated.put("collageName","AUC");
        dbApp.updateTable(strTableName,"3",tobeUpdated);
        t = deserialize_table(strTableName);
        for(String ref : t.getPages())
            System.out.println(deserialize_page(t,t.getPages().indexOf(ref)));
        for(String ref : t.getIndices()) {
            System.out.println(deserialize_index(ref).htblRefernces);
            System.out.println(deserialize_index(ref));
        }
        System.out.println("-----------------------------------------------------------------");
    }
}
