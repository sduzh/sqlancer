package sqlancer.doris.ast;

import sqlancer.Randomly;
import sqlancer.ast.BinaryOperatorNode;
import sqlancer.ast.BinaryOperatorNode.Operator;
import sqlancer.doris.ast.DorisBinaryBitOperation.DorisBinaryBitOperator;

public class DorisBinaryBitOperation extends BinaryOperatorNode<DorisExpression, DorisBinaryBitOperator>
        implements DorisExpression {

    public enum DorisBinaryBitOperator implements Operator {
        AND("&"), //
        OR("|"), //
        XOR("^"), //
        LEFT_SHIFT("<<"), //
        RIGHT_SHIFT(">>");

        String textRepresentation;

        DorisBinaryBitOperator(String textRepresentation) {
            this.textRepresentation = textRepresentation;
        }

        public static DorisBinaryBitOperator getRandom() {
            return Randomly.fromOptions(values());
        }

        @Override
        public String getTextRepresentation() {
            return textRepresentation;
        }
    }

    public DorisBinaryBitOperation(DorisExpression left, DorisExpression right, DorisBinaryBitOperator op) {
        super(left, right, op);
    }

}
