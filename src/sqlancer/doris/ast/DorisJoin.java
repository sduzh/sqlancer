package sqlancer.doris.ast;

import java.util.ArrayList;
import java.util.List;

import sqlancer.Randomly;
import sqlancer.doris.DorisExpressionGenerator;
import sqlancer.doris.DorisProvider.DorisGlobalState;
import sqlancer.doris.DorisSchema.DorisColumn;

public class DorisJoin implements DorisExpression {

    private final DorisExpression leftTable;
    private final DorisExpression rightTable;
    private final JoinType joinType;
    private final DorisExpression onCondition;
    private NaturalJoinType outerType;

    public enum JoinType {
        INNER, LEFT, RIGHT;

        public static JoinType getRandom() {
            return Randomly.fromOptions(values());
        }
    }

    public enum NaturalJoinType {
        INNER, LEFT, RIGHT;

        public static NaturalJoinType getRandom() {
            return Randomly.fromOptions(values());
        }
    }

    public DorisJoin(DorisExpression leftTable, DorisExpression rightTable, JoinType joinType,
            DorisExpression whereCondition) {
        this.leftTable = leftTable;
        this.rightTable = rightTable;
        this.joinType = joinType;
        this.onCondition = whereCondition;
    }

    public DorisExpression getLeftTable() {
        return leftTable;
    }

    public DorisExpression getRightTable() {
        return rightTable;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public DorisExpression getOnCondition() {
        return onCondition;
    }

    public static DorisJoin createInnerJoin(DorisExpression left, DorisExpression right, DorisExpression onClause) {
        return new DorisJoin(left, right, JoinType.INNER, onClause);
    }

    public static DorisJoin createLeftOuterJoin(DorisExpression left, DorisExpression right, DorisExpression onClause) {
        return new DorisJoin(left, right, JoinType.LEFT, onClause);
    }

    public static DorisJoin createRightOuterJoin(DorisExpression left, DorisExpression right, DorisExpression onClause) {
        return new DorisJoin(left, right, JoinType.RIGHT, onClause);
    }

    private void setNaturalJoinType(NaturalJoinType outerType) {
        this.outerType = outerType;
    }

    public NaturalJoinType getNaturalJoinType() {
        return outerType;
    }

    public static List<DorisExpression> getJoins(List<DorisExpression> tableList, DorisGlobalState globalState) {
        List<DorisExpression> joinExpressions = new ArrayList<>();
        while (tableList.size() >= 2 && Randomly.getBoolean()) {
            DorisTableReference leftTable = (DorisTableReference) tableList.remove(0);
            DorisTableReference rightTable = (DorisTableReference) tableList.remove(0);
            List<DorisColumn> columns = new ArrayList<>(leftTable.getTable().getColumns());
            columns.addAll(rightTable.getTable().getColumns());
            DorisExpressionGenerator joinGen = new DorisExpressionGenerator(globalState).setColumns(columns);
            switch (DorisJoin.JoinType.getRandom()) {
            case INNER:
                joinExpressions.add(DorisJoin.createInnerJoin(leftTable, rightTable, joinGen.generateExpression()));
                break;
            case LEFT:
                joinExpressions.add(DorisJoin.createLeftOuterJoin(leftTable, rightTable, joinGen.generateExpression()));
                break;
            case RIGHT:
                joinExpressions.add(DorisJoin.createRightOuterJoin(leftTable, rightTable, joinGen.generateExpression()));
                break;
            default:
                throw new AssertionError();
            }
        }
        return joinExpressions;
    }

}
