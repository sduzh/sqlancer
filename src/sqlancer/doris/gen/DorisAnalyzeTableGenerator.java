package sqlancer.doris.gen;

import java.sql.SQLException;
import java.util.Arrays;

import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.doris.DorisProvider.DorisGlobalState;
import sqlancer.doris.DorisSchema.DorisTable;

public final class DorisAnalyzeTableGenerator {

    private DorisAnalyzeTableGenerator() {
    }

    public static Query getQuery(DorisGlobalState globalState) throws SQLException {
        DorisTable table = globalState.getSchema().getRandomTable(t -> !t.isView());
        boolean analyzeIndex = !table.getIndexes().isEmpty() && Randomly.getBoolean();
        StringBuilder sb = new StringBuilder("ANALYZE ");
        if (analyzeIndex && Randomly.getBoolean()) {
            sb.append("INCREMENTAL ");
        }
        sb.append("TABLE ");
        sb.append(table.getName());
        if (analyzeIndex) {
            sb.append(" INDEX ");
            sb.append(table.getRandomIndex().getIndexName());
        }
        if (Randomly.getBoolean()) {
            sb.append(" WITH ");
            sb.append(Randomly.getNotCachedInteger(1, 1024));
            sb.append(" BUCKETS");
        }
        return new QueryAdapter(sb.toString(), Arrays.asList());
    }

}
