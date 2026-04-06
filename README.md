# Mini Database Engine

A lightweight relational database engine implemented from scratch in Java. Supports table creation, CRUD operations, SQL-like querying, and 3D **Octree indexing** for accelerated multi-column lookups. Data is persisted to disk via Java serialization.

---

## Features

- **Table management** — create tables with typed columns, clustering keys, and min/max constraints
- **Full CRUD** — insert, update, delete, and select rows
- **Page-based storage** — rows are stored in fixed-size serialized pages on disk
- **Binary search** on clustered (primary key) column for fast lookup and insert positioning
- **Octree indexing** — create a 3D spatial index over any 3 columns for accelerated range and equality queries
- **SQL-like selection** — query with `SQLTerm` conditions combined using `AND`, `OR`, `XOR`
- **Null support** — nullable columns using the custom `DBAppNull` type
- **Metadata tracking** — all table/column definitions, types, min/max values, and index info stored in `metadata.csv`
- **Auto index utilization** — queries over 3 AND-connected indexed columns automatically use the Octree

---

## Architecture

```
DBApp.java          ← Main engine: all public API methods
├── Table.java      ← Serializable table descriptor (name, pages list, column names, index refs)
├── octree.java     ← 3D Octree index (insert, delete, search, range search)
├── node.java       ← Internal Octree node with region subdivision logic
├── SQLTerm.java    ← Query term: (tableName, columnName, operator, value)
├── DBAppException.java  ← Custom checked exception
└── DBAppNull.java  ← Sentinel object for nullable column values

Disk layout:
├── metadata.csv              ← Schema registry for all tables and columns
├── <TableName>.ser           ← Serialized Table object
├── <TableName>Page0.ser      ← Serialized page (Vector<Vector<Object>>)
├── <TableName>Page1.ser
└── <TableName>_col1_col2_col3_Index.ser  ← Serialized Octree index
```

---

## Core API

### Initialize the Engine
```java
DBApp dbApp = new DBApp();
dbApp.init(); // Creates metadata.csv
```

### Create a Table
```java
Hashtable<String, String> colTypes = new Hashtable<>();
colTypes.put("id",   "java.lang.Integer");
colTypes.put("name", "java.lang.String");
colTypes.put("gpa",  "java.lang.Double");
colTypes.put("dob",  "java.util.Date");

Hashtable<String, String> colMin = new Hashtable<>();
colMin.put("id", "1");  colMin.put("name", "A");
colMin.put("gpa", "0.0");  colMin.put("dob", "1900-01-01");

Hashtable<String, String> colMax = new Hashtable<>();
colMax.put("id", "9999");  colMax.put("name", "ZZZZ");
colMax.put("gpa", "4.0");  colMax.put("dob", "2100-01-01");

dbApp.createTable("Student", "id", colTypes, colMin, colMax);
```

- `"id"` is the **clustering key** — rows are kept sorted by this column across pages
- Supported types: `java.lang.Integer`, `java.lang.Double`, `java.lang.String`, `java.util.Date`

### Insert a Row
```java
Hashtable<String, Object> row = new Hashtable<>();
row.put("id", 1);
row.put("name", "Alice");
row.put("gpa", 3.8);
row.put("dob", new Date(2000, 5, 15));

dbApp.insertIntoTable("Student", row);
```
- Rows are inserted in sorted order by the clustering key using **binary search** within each page
- When a page exceeds `MaximumRowsCountinTablePage` (configured in `DBApp.config`), it is split
- All existing Octree indices are updated automatically on insert

### Update Rows
```java
// strKey = clustering key value (as String)
Hashtable<String, Object> updates = new Hashtable<>();
updates.put("gpa", 3.9);

dbApp.updateTable("Student", "1", updates);
```
- Locates the row by clustering key, applies the given field updates
- Octree indices are updated if indexed columns are modified

### Delete Rows
```java
Hashtable<String, Object> filter = new Hashtable<>();
filter.put("gpa", 3.8);

dbApp.deleteFromTable("Student", filter);
```
- Supports partial match deletion (any subset of columns)
- Automatically removes deleted entries from all relevant Octree indices

### Create an Octree Index
```java
String[] indexCols = {"name", "gpa", "dob"};
dbApp.createIndex("Student", indexCols);
```
- Always indexes **exactly 3 columns**
- Retroactively indexes all existing rows in the table
- Index is persisted as `Student_name_gpa_dob_Index.ser`
- Metadata is updated to reflect the index on these columns

### Query / Select
```java
SQLTerm[] terms = new SQLTerm[2];
terms[0] = new SQLTerm();
terms[0]._strTableName  = "Student";
terms[0]._strColumnName = "gpa";
terms[0]._strOperator   = ">=";
terms[0]._objValue      = 3.5;

terms[1] = new SQLTerm();
terms[1]._strTableName  = "Student";
terms[1]._strColumnName = "name";
terms[1]._strOperator   = "=";
terms[1]._objValue      = "Alice";

String[] operators = {"AND"};

Iterator result = dbApp.selectFromTable(terms, operators);
while (result.hasNext()) {
    System.out.println(result.next());
}
```

**Supported column operators:** `=`, `!=`, `>`, `>=`, `<`, `<=`

**Supported logical operators:** `AND`, `OR`, `XOR`

**Index acceleration:** If 3 terms are provided, all joined with `AND`, and an Octree index exists on exactly those 3 columns, the query will use the index instead of a full table scan.

---

## ⚙️ Configuration

Edit `src/resources/DBApp.config` to tune engine behaviour:

```properties
MaximumRowsCountinTablePage = 200
MaximumEntriesinOctreeNode  = 16
```

| Property | Description |
|---|---|
| `MaximumRowsCountinTablePage` | Max rows stored per serialized page file before a page split occurs |
| `MaximumEntriesinOctreeNode` | Max entries per Octree leaf node before the node is subdivided into 8 children |

---

## Data Model & Storage

| Concept | Implementation |
|---|---|
| Table schema | `metadata.csv` — one row per column with type, min, max, index info |
| Table object | `<Name>.ser` — serialized `Table` (page file list, column names, index refs) |
| Page | `<Name>PageN.ser` — serialized `Vector<Vector<Object>>` (list of rows) |
| Index | `<Name>_c1_c2_c3_Index.ser` — serialized `octree` object |
| Null values | Stored as `DBAppNull` instances; compared and printed as `"null"` |
| Strings | Stored and compared case-insensitively (lowercased internally) |

Rows within each page are kept **sorted by the clustering key**. Inserts use binary search to find the correct position, and pages are renumbered (via `pageFixing`) after deletions to keep filenames contiguous.

---

## Octree Index

The Octree is a 3D spatial index that recursively subdivides a 3D bounding box into 8 equal octants. Each leaf node holds up to `MaximumEntriesinOctreeNode` entries before splitting.

Key capabilities:
- **Exact 3-column match** — point query returns matching page references directly
- **Range queries** — `searchSelect` supports per-axis operators (`=`, `<`, `>`, `<=`, `>=`, `!=`)
- **Partial queries** — `null` on any axis is treated as a wildcard
- **Page reference mapping** — the octree stores short integer keys internally; a `Hashtable` maps them back to actual `.ser` page filenames
- **Auto-update** — insert, delete, and update operations in `DBApp` keep all indices consistent

---

## Project Structure

```
Mini-Database-Engine-main/
├── src/
│   ├── DBApp.java          # Engine core — all public API + serialization utilities
│   ├── Table.java          # Table metadata model
│   ├── octree.java         # 3D Octree index
│   ├── node.java           # Octree node (recursive subdivision)
│   ├── SQLTerm.java        # Query term struct
│   ├── DBAppException.java # Custom exception
│   ├── DBAppNull.java      # Null value sentinel
│   └── resources/
│       └── DBApp.config    # Engine configuration
├── bin/                    # Compiled .class files
└── DBProj.iml              # IntelliJ IDEA module file
```

---

## Getting Started

### Compile

```bash
cd Mini-Database-Engine-main/src
javac -d ../bin DBApp.java Table.java octree.java node.java SQLTerm.java DBAppException.java DBAppNull.java
```

### Run the Demo

```bash
cd ../bin
java DBApp
```

The `main` method in `DBApp.java` demonstrates the full workflow: creating a `Student` table, building two Octree indices, inserting 7 rows, and running several select queries with and without index utilization.

---

## Contributors

- Youssef Maged
- Kirollos Magdy
- Youssef Amir
- Layla Hossam
