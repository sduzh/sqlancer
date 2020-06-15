package sqlancer.doris.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.doris.DorisExpressionGenerator;
import sqlancer.doris.DorisProvider.DorisGlobalState;
import sqlancer.doris.DorisSchema.DorisTables;
import sqlancer.doris.ast.DorisExpression;
import sqlancer.doris.ast.DorisSelect;
import sqlancer.doris.ast.DorisTableReference;
import sqlancer.doris.visitor.DorisVisitor;

public final class DorisRandomQuerySynthesizer {

    private DorisRandomQuerySynthesizer() {
    }

    public static Query generate(DorisGlobalState globalState, int nrColumns) {
        DorisSelect select = generateSelect(globalState, nrColumns);
        return new QueryAdapter(DorisVisitor.asString(select));
    }

    public static DorisSelect generateSelect(DorisGlobalState globalState, int nrColumns) {
        DorisTables tables = globalState.getSchema().getRandomTableNonEmptyTables();
        DorisExpressionGenerator gen = new DorisExpressionGenerator(globalState).setColumns(tables.getColumns());
        DorisSelect select = new DorisSelect();
        // select.setDistinct(Randomly.getBoolean());
        List<DorisExpression> columns = new ArrayList<>();
        // TODO: also generate aggregates
        columns.addAll(gen.generateExpressions(nrColumns));
        select.setFetchColumns(columns);
        List<DorisExpression> tableList = tables.getTables().stream().map(t -> new DorisTableReference(t))
                .collect(Collectors.toList());
        // TODO: generate joins
        select.setFromList(tableList);
        if (Randomly.getBoolean()) {
            select.setWhereClause(gen.generateExpression());
        }
        if (Randomly.getBooleanWithRatherLowProbability()) {
            select.setOrderByExpressions(gen.generateOrderBys());
        }
        if (Randomly.getBoolean()) {
            select.setGroupByExpressions(gen.generateExpressions(Randomly.smallNumber() + 1));
            if (Randomly.getBoolean()) {
                select.setHavingClause(gen.generateHavingClause());
            }
        }
        if (Randomly.getBoolean()) {
            select.setLimitClause(gen.generateExpression());
        }
        if (Randomly.getBoolean()) {
            select.setOffsetClause(gen.generateExpression());
        }
        return select;
    }

}
