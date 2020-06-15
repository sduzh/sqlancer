package sqlancer.doris.oracle;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import sqlancer.Randomly;
import sqlancer.TestOracle;
import sqlancer.doris.DorisErrors;
import sqlancer.doris.DorisExpressionGenerator;
import sqlancer.doris.DorisProvider.DorisGlobalState;
import sqlancer.doris.DorisSchema;
import sqlancer.doris.DorisSchema.DorisTable;
import sqlancer.doris.DorisSchema.DorisTables;
import sqlancer.doris.ast.DorisColumnReference;
import sqlancer.doris.ast.DorisExpression;
import sqlancer.doris.ast.DorisJoin;
import sqlancer.doris.ast.DorisSelect;
import sqlancer.doris.ast.DorisTableReference;
import sqlancer.doris.ast.DorisUnaryPostfixOperation;
import sqlancer.doris.ast.DorisUnaryPostfixOperation.DorisUnaryPostfixOperator;
import sqlancer.doris.ast.DorisUnaryPrefixOperation;
import sqlancer.doris.ast.DorisUnaryPrefixOperation.DorisUnaryPrefixOperator;

public abstract class DorisTLPBase implements TestOracle {

    final DorisGlobalState state;
    final Set<String> errors = new HashSet<>();

    DorisSchema s;
    DorisTables targetTables;
    DorisExpressionGenerator gen;
    DorisSelect select;
    DorisExpression predicate;
    DorisExpression negatedPredicate;
    DorisExpression isNullPredicate;

    public DorisTLPBase(DorisGlobalState state) {
        this.state = state;
        DorisErrors.addExpressionErrors(errors);
    }

    @Override
    public void check() throws SQLException {
        s = state.getSchema();
        targetTables = s.getRandomTableNonEmptyTables();
        gen = new DorisExpressionGenerator(state).setColumns(targetTables.getColumns());
        select = new DorisSelect();
        select.setFetchColumns(generateFetchColumns());
        List<DorisTable> tables = targetTables.getTables();

        List<DorisExpression> tableList = tables.stream().map(t -> new DorisTableReference(t))
                .collect(Collectors.toList());
        List<DorisExpression> joins = DorisJoin.getJoins(tableList, state);
        select.setJoinList(joins);
        select.setFromList(tableList);
        select.setWhereClause(null);
        predicate = generatePredicate();
        negatedPredicate = new DorisUnaryPrefixOperation(predicate, DorisUnaryPrefixOperator.NOT);
        isNullPredicate = new DorisUnaryPostfixOperation(predicate, DorisUnaryPostfixOperator.IS_NULL);
    }

    List<DorisExpression> generateFetchColumns() {
        return Arrays.asList(new DorisColumnReference(targetTables.getColumns().get(0)));
    }

    DorisExpression generatePredicate() {
        return gen.generateExpression();
    }

}
