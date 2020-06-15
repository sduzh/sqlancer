package sqlancer.doris.oracle;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sqlancer.DatabaseProvider;
import sqlancer.Randomly;
import sqlancer.TestOracle;
import sqlancer.doris.DorisErrors;
import sqlancer.doris.DorisProvider.DorisGlobalState;
import sqlancer.doris.ast.DorisExpression;
import sqlancer.doris.visitor.DorisVisitor;

public class DorisTLPHavingOracle extends DorisTLPBase implements TestOracle {

    public DorisTLPHavingOracle(DorisGlobalState state) {
        super(state);
        DorisErrors.addExpressionHavingErrors(errors);
    }

    @Override
    public void check() throws SQLException {
        super.check();
        if (Randomly.getBoolean()) {
            select.setWhereClause(gen.generateExpression());
        }
        boolean orderBy = Randomly.getBoolean();
        if (orderBy) {
            select.setOrderByExpressions(gen.generateOrderBys());
        }
        select.setGroupByExpressions(gen.generateExpressions(Randomly.smallNumber() + 1));
        select.setHavingClause(null);
        String originalQueryString = DorisVisitor.asString(select);
        List<String> resultSet = DatabaseProvider.getResultSetFirstColumnAsString(originalQueryString, errors,
                state.getConnection(), state);

        select.setHavingClause(predicate);
        String firstQueryString = DorisVisitor.asString(select);
        select.setHavingClause(negatedPredicate);
        String secondQueryString = DorisVisitor.asString(select);
        select.setHavingClause(isNullPredicate);
        String thirdQueryString = DorisVisitor.asString(select);
        List<String> combinedString = new ArrayList<>();
        List<String> secondResultSet = TestOracle.getCombinedResultSet(firstQueryString, secondQueryString,
                thirdQueryString, combinedString, !orderBy, state, errors);
        TestOracle.assumeResultSetsAreEqual(resultSet, secondResultSet, originalQueryString, combinedString, state);
    }

    @Override
    DorisExpression generatePredicate() {
        return gen.generateHavingClause();
    }
}
