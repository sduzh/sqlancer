package sqlancer.doris.gen;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.doris.DorisErrors;
import sqlancer.doris.DorisExpressionGenerator;
import sqlancer.doris.DorisProvider.DorisGlobalState;
import sqlancer.doris.DorisSchema.DorisTable;
import sqlancer.doris.visitor.DorisVisitor;

public final class DorisDeleteGenerator {

    private DorisDeleteGenerator() {
    }

    public static Query getQuery(DorisGlobalState globalState) throws SQLException {
        Set<String> errors = new HashSet<>();
        DorisTable table = globalState.getSchema().getRandomTable(t -> !t.isView());
        DorisExpressionGenerator gen = new DorisExpressionGenerator(globalState).setColumns(table.getColumns());
        StringBuilder sb = new StringBuilder("DELETE ");
        if (Randomly.getBooleanWithSmallProbability()) {
            sb.append("LOW_PRIORITY ");
        }
        if (Randomly.getBooleanWithSmallProbability()) {
            sb.append("QUICK ");
        }
        if (Randomly.getBooleanWithSmallProbability()) {
            sb.append("IGNORE ");
        }
        sb.append("FROM ");
        sb.append(table.getName());
        if (Randomly.getBoolean()) {
            sb.append(" WHERE ");
            sb.append(DorisVisitor.asString(gen.generateExpression()));
            errors.add("Truncated incorrect");
            errors.add("Data truncation");
            errors.add("Truncated incorrect FLOAT value");
        }
        if (Randomly.getBoolean()) {
            sb.append(" ORDER BY ");
            DorisErrors.addExpressionErrors(errors);
            sb.append(gen.generateOrderBys().stream().map(o -> DorisVisitor.asString(o))
                    .collect(Collectors.joining(", ")));
        }
        if (Randomly.getBoolean()) {
            sb.append(" LIMIT ");
            sb.append(Randomly.getNotCachedInteger(0, Integer.MAX_VALUE));
        }
        errors.add("Bad Number");
        errors.add("Division by 0");
        errors.add("error parsing regexp");
        return new QueryAdapter(sb.toString(), errors);

    }

}
