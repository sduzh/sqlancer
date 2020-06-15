package sqlancer.doris.gen;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import sqlancer.IgnoreMeException;
import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.doris.DorisExpressionGenerator;
import sqlancer.doris.DorisProvider.DorisGlobalState;
import sqlancer.doris.DorisSchema.DorisColumn;
import sqlancer.doris.DorisSchema.DorisCompositeDataType;
import sqlancer.doris.DorisSchema.DorisDataType;
import sqlancer.doris.DorisSchema.DorisTable;
import sqlancer.doris.visitor.DorisVisitor;

public class DorisTableGenerator {

    private final List<DorisColumn> columns = new ArrayList<>();
    private final Set<String> errors = new HashSet<>();

    public Query getQuery(DorisGlobalState globalState) throws SQLException {
        String tableName = globalState.getSchema().getFreeTableName();
        int nrColumns = Randomly.smallNumber() + 1;
        for (int i = 0; i < nrColumns; i++) {
            DorisColumn fakeColumn = new DorisColumn("c" + i, null, false, false);
            columns.add(fakeColumn);
        }
        DorisExpressionGenerator gen = new DorisExpressionGenerator(globalState).setColumns(columns);

        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(tableName);
        createNewDuplicateTable(gen, sb);
        return new QueryAdapter(sb.toString(), errors, true);
    }

    private void createNewDuplicateTable(DorisExpressionGenerator gen, StringBuilder sb) {
        sb.append("(");
        for (int i = 0; i < columns.size(); i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(columns.get(i).getName());
            sb.append(" ");
            DorisCompositeDataType type;
            do {
                type = DorisCompositeDataType.getRandom();
            } while (i == 0 && type.getPrimitiveDataType() == DorisDataType.FLOATING);
            appendType(sb, type);
            sb.append(" ");
            if (Randomly.getBooleanWithRatherLowProbability()) {
                sb.append("NOT NULL ");
            }
            // if (Randomly.getBoolean() && type.getPrimitiveDataType() != DorisDataType.CHAR) {
            //     sb.append("DEFAULT \"");
            //     sb.append(DorisVisitor.asString(gen.generateConstant()));
            //     sb.append("\" ");
            //     errors.add("Invalid number format");
            // }
        }
        sb.append(")");
        // TODO: random keys and buckets
        sb.append("DUPLICATE KEY(").append(columns.get(0).getName()).append(") ");
        sb.append("DISTRIBUTED BY HASH(").append(columns.get(0).getName()).append(") BUCKETS 1 ");
        sb.append("PROPERTIES(\"replication_num\"=\"1\")");
        errors.add("Float or double can not used as a key, use decimal instead");
    }

    private void appendType(StringBuilder sb, DorisCompositeDataType type) {
        if (type.getPrimitiveDataType() == DorisDataType.CHAR) {
            throw new IgnoreMeException();
        }
        // sb.append(type.toString());
        if (type.getPrimitiveDataType() == DorisDataType.INT) {
            sb.append(Randomly.fromOptions("TINYINT", "SMALLINT", "INT", "BIGINT", "LARGEINT"));
            return;
        }
        sb.append(type);
        if (type.getPrimitiveDataType() == DorisDataType.DECIMAL) {
            appendPrecisionAndScale(sb);
        }
        // appendSpecifiers(sb, type.getPrimitiveDataType());
        // appendSizeSpecifiers(sb, type.getPrimitiveDataType());
    }

    private enum Action {
        AUTO_INCREMENT, PRE_SPLIT_REGIONS, SHARD_ROW_ID_BITS
    }

    private void appendSizeSpecifiers(StringBuilder sb, DorisDataType type) {
        // if (type.isNumeric() && Randomly.getBoolean()) {
        //     sb.append(" UNSIGNED");
        // }
    }

    static void appendPrecisionAndScale(StringBuilder sb) {
        long m = Randomly.getNotCachedInteger(2, 9);
        long n = Randomly.getNotCachedInteger(0, (int)m-1);
        sb.append("(");
        sb.append(m);
        sb.append(",");
        sb.append(n);
        sb.append(")");
    }
}
