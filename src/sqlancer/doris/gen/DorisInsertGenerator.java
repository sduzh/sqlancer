package sqlancer.doris.gen;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.doris.DorisErrors;
import sqlancer.doris.DorisExpressionGenerator;
import sqlancer.doris.DorisProvider.DorisGlobalState;
import sqlancer.doris.DorisSchema.DorisColumn;
import sqlancer.doris.DorisSchema.DorisTable;
import sqlancer.doris.visitor.DorisVisitor;

public class DorisInsertGenerator {

    private final DorisGlobalState globalState;
    private final Set<String> errors = new HashSet<>();
    private DorisExpressionGenerator gen;

    public DorisInsertGenerator(DorisGlobalState globalState) {
        this.globalState = globalState;
        DorisErrors.addInsertErrors(errors);
    }

    public static Query getQuery(DorisGlobalState globalState) throws SQLException {
        return new DorisInsertGenerator(globalState).get();
    }

    private Query get() {
        DorisTable table = globalState.getSchema().getRandomTable(t -> !t.isView());
        gen = new DorisExpressionGenerator(globalState).setColumns(table.getColumns());
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT");
        sb.append(" INTO ");
        sb.append(table.getName());
        if (Randomly.getBoolean()) {
            sb.append(" VALUES ");
            List<DorisColumn> columns = table.getColumns();
            insertColumns(sb, columns);
        } else {
            List<DorisColumn> columnSubset = table.getRandomNonEmptyColumnSubset();
            sb.append("(");
            sb.append(columnSubset.stream().map(c -> c.getName()).collect(Collectors.joining(", ")));
            sb.append(") VALUES ");
            insertColumns(sb, columnSubset);
        }
        return new QueryAdapter(sb.toString(), errors);
    }

    private void insertColumns(StringBuilder sb, List<DorisColumn> columns) {
        for (int nrRows = 0; nrRows < Randomly.smallNumber() + 1; nrRows++) {
            if (nrRows != 0) {
                sb.append(", ");
            }
            sb.append("(");
            for (int nrColumn = 0; nrColumn < columns.size(); nrColumn++) {
                if (nrColumn != 0) {
                    sb.append(", ");
                }
                insertValue(sb);
            }
            sb.append(")");
        }
    }

    private void insertValue(StringBuilder sb) {
        sb.append(gen.generateConstant()); // TODO: try to insert valid data
    }

}
