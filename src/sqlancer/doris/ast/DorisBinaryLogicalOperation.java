package sqlancer.doris.ast;

import sqlancer.Randomly;
import sqlancer.ast.BinaryOperatorNode;
import sqlancer.ast.BinaryOperatorNode.Operator;
import sqlancer.doris.ast.DorisBinaryLogicalOperation.DorisBinaryLogicalOperator;

public class DorisBinaryLogicalOperation extends BinaryOperatorNode<DorisExpression, DorisBinaryLogicalOperator>
        implements DorisExpression {

    public enum DorisBinaryLogicalOperator implements Operator {
        AND("AND"), //
        OR("OR"); //

        String textRepresentation;

        DorisBinaryLogicalOperator(String textRepresentation) {
            this.textRepresentation = textRepresentation;
        }

        public static DorisBinaryLogicalOperator getRandom() {
            return Randomly.fromOptions(values());
        }

        @Override
        public String getTextRepresentation() {
            return textRepresentation;
        }
    }

    public DorisBinaryLogicalOperation(DorisExpression left, DorisExpression right, DorisBinaryLogicalOperator op) {
        super(left, right, op);
    }

}
