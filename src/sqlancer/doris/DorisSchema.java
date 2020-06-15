package sqlancer.doris;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import sqlancer.Randomly;
import sqlancer.schema.AbstractSchema;
import sqlancer.schema.AbstractTable;
import sqlancer.schema.AbstractTableColumn;
import sqlancer.schema.AbstractTables;
import sqlancer.schema.TableIndex;
import sqlancer.doris.DorisSchema.DorisTable;

public class DorisSchema extends AbstractSchema<DorisTable> {

    public enum DorisDataType {

        INT, FLOATING, CHAR, DECIMAL;

        private final boolean isPrimitive;

        DorisDataType() {
            isPrimitive = true;
        }

        DorisDataType(boolean isPrimitive) {
            this.isPrimitive = isPrimitive;
        }

        public static DorisDataType getRandom() {
            return Randomly.fromOptions(values());
        }

        public boolean isPrimitive() {
            return isPrimitive;
        }

        public boolean isNumeric() {
            switch (this) {
            case INT:
            case DECIMAL:
            case FLOATING:
                return true;
            case CHAR:
                return false;
            default:
                throw new AssertionError(this);
            }
        }
    }

    public static class DorisCompositeDataType {

        private final DorisDataType dataType;

        private final int size;

        public DorisCompositeDataType(DorisDataType dataType) {
            this.dataType = dataType;
            this.size = -1;
        }

        public DorisCompositeDataType(DorisDataType dataType, int size) {
            this.dataType = dataType;
            this.size = size;
        }

        public DorisDataType getPrimitiveDataType() {
            return dataType;
        }

        public int getSize() {
            if (size == -1) {
                throw new AssertionError(this);
            }
            return size;
        }

        public static DorisCompositeDataType getInt(int size) {
            return new DorisCompositeDataType(DorisDataType.INT, size);
        }

        public static DorisCompositeDataType getRandom() {
            DorisDataType primitiveType = DorisDataType.getRandom();
            int size = -1;
            switch (primitiveType) {
            case INT:
                size = Randomly.fromOptions(1, 2, 4, 8, 16);
                break;
            case FLOATING:
                size = Randomly.fromOptions(4, 8);
                break;
            default:
                break;
            }
            return new DorisCompositeDataType(primitiveType, size);
        }

        @Override
        public String toString() {
            switch (getPrimitiveDataType()) {
            case INT:
                switch (size) {
                case 1:
                    return "TINYINT";
                case 2:
                    return "SMALLINT";
                case 4:
                    return "INT";
                case 8:
                    return "BIGINT";
                case 16:
                    return "LARGEINT";
                default:
                    throw new AssertionError(size);
                }
            case FLOATING:
                switch (size) {
                case 4:
                    return "FLOAT";
                case 8:
                    return "DOUBLE";
                default:
                    throw new AssertionError(size);
                }
            default:
                return getPrimitiveDataType().toString();
            }
        }

    }

    public static class DorisColumn extends AbstractTableColumn<DorisTable, DorisCompositeDataType> {

        private final boolean isPrimaryKey;
        private boolean isNullable;

        public DorisColumn(String name, DorisCompositeDataType columnType, boolean isPrimaryKey, boolean isNullable) {
            super(name, null, columnType);
            this.isPrimaryKey = isPrimaryKey;
            this.isNullable = isNullable;
        }

        public boolean isPrimaryKey() {
            return isPrimaryKey;
        }

        public boolean isNullable() {
            return isNullable;
        }

    }

    public static class DorisTables extends AbstractTables<DorisTable, DorisColumn> {

        public DorisTables(List<DorisTable> tables) {
            super(tables);
        }

    }

    public DorisSchema(List<DorisTable> databaseTables) {
        super(databaseTables);
    }

    public DorisTables getRandomTableNonEmptyTables() {
        return new DorisTables(Randomly.nonEmptySubset(getDatabaseTables()));
    }

    private static DorisCompositeDataType getColumnType(String typeString) {
        typeString = typeString.replace(" zerofill", "").replace(" unsigned", "");
        if (typeString.contains("DECIMAL")) {
            return new DorisCompositeDataType(DorisDataType.DECIMAL);
        }
        if (typeString.startsWith("CHAR") || typeString.startsWith("VARCHAR")) {
            return new DorisCompositeDataType(DorisDataType.CHAR);
        }
        DorisDataType primitiveType;
        int size = -1;
        switch (typeString) {
        case "FLOAT":
        case "DOUBLE":
            primitiveType = DorisDataType.FLOATING;
            break;
        case "TINYINT":
            primitiveType = DorisDataType.INT;
            size = 1;
            break;
        case "SMALLINT":
            primitiveType = DorisDataType.INT;
            size = 2;
            break;
        case "INT":
            primitiveType = DorisDataType.INT;
            size = 4;
            break;
        case "BIGINT":
            primitiveType = DorisDataType.INT;
            size = 8;
            break;
        case "LARGEINT":
            primitiveType = DorisDataType.INT;
            size = 16;
            break;
        default:
            throw new AssertionError(typeString);
        }
        return new DorisCompositeDataType(primitiveType, size);
    }

    public static class DorisTable extends AbstractTable<DorisColumn, TableIndex> {

        public DorisTable(String tableName, List<DorisColumn> columns, List<TableIndex> indexes, boolean isView) {
            super(tableName, columns, indexes, isView);
        }

        public boolean hasPrimaryKey() {
            return getColumns().stream().anyMatch(c -> c.isPrimaryKey());
        }

    }

    public static DorisSchema fromConnection(Connection con, String databaseName) throws SQLException {
        List<DorisTable> databaseTables = new ArrayList<>();
        List<String> tableNames = getTableNames(con);
        for (String tableName : tableNames) {
            List<DorisColumn> databaseColumns = getTableColumns(con, tableName);
            List<TableIndex> indexes = getIndexes(con, tableName);
            boolean isView = tableName.startsWith("v");
            DorisTable t = new DorisTable(tableName, databaseColumns, indexes, isView);
            for (DorisColumn c : databaseColumns) {
                c.setTable(t);
            }
            databaseTables.add(t);

        }
        return new DorisSchema(databaseTables);
    }

    private static List<String> getTableNames(Connection con) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        try (Statement s = con.createStatement()) {
            ResultSet tableRs = s.executeQuery("SHOW TABLES");
            while (tableRs.next()) {
                String tableName = tableRs.getString(1);
                tableNames.add(tableName);
            }
        }
        return tableNames;
    }

    private static List<TableIndex> getIndexes(Connection con, String tableName) throws SQLException {
        List<TableIndex> indexes = new ArrayList<>();
        try (Statement s = con.createStatement()) {
            try (ResultSet rs = s.executeQuery(String.format("SHOW INDEX FROM %s", tableName))) {
                while (rs.next()) {
                    String indexName = rs.getString("Key_name");
                    indexes.add(TableIndex.create(indexName));
                }
            }
        }
        return indexes;
    }

    private static List<DorisColumn> getTableColumns(Connection con, String tableName) throws SQLException {
        List<DorisColumn> columns = new ArrayList<>();
        try (Statement s = con.createStatement()) {
            try (ResultSet rs = s.executeQuery("SHOW COLUMNS FROM " + tableName)) {
                while (rs.next()) {
                    String columnName = rs.getString("Field");
                    String dataType = rs.getString("Type");
                    boolean isNullable = rs.getString("Null").contentEquals("YES");
                    boolean isPrimaryKey = rs.getString("Key").contains("YES");
                    DorisColumn c = new DorisColumn(columnName, getColumnType(dataType), isPrimaryKey, isNullable);
                    columns.add(c);
                }
            }
        }
        return columns;
    }

}
