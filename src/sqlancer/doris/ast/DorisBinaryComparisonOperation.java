package sqlancer.doris.ast;

import sqlancer.Randomly;
import sqlancer.ast.BinaryOperatorNode;
import sqlancer.ast.BinaryOperatorNode.Operator;
import sqlancer.doris.ast.DorisBinaryComparisonOperation.DorisComparisonOperator;

public class DorisBinaryComparisonOperation extends BinaryOperatorNode<DorisExpression, DorisComparisonOperator>
        implements DorisExpression {

    public enum DorisComparisonOperator implements Operator {
        EQUALS("="), //
        GREATER(">"), //
        GREATER_EQUALS(">="), //
        SMALLER("<"), //
        SMALLER_EQUALS("<="), //
        NOT_EQUALS("!="); //

        private String textRepr;

        DorisComparisonOperator(String textRepr) {
            this.textRepr = textRepr;
        }

        public static DorisComparisonOperator getRandom() {
            return Randomly.fromOptions(DorisComparisonOperator.values());
        }

        @Override
        public String getTextRepresentation() {
            return textRepr;
        }

    }

    public DorisBinaryComparisonOperation(DorisExpression left, DorisExpression right, DorisComparisonOperator op) {
        super(left, right, op);
    }

}
