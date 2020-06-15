package sqlancer.doris.ast;

import sqlancer.Randomly;
import sqlancer.ast.BinaryOperatorNode.Operator;
import sqlancer.ast.UnaryOperatorNode;
import sqlancer.doris.ast.DorisUnaryPrefixOperation.DorisUnaryPrefixOperator;

public class DorisUnaryPrefixOperation extends UnaryOperatorNode<DorisExpression, DorisUnaryPrefixOperator>
        implements DorisExpression {

    public enum DorisUnaryPrefixOperator implements Operator {
        NOT("NOT"), //
        INVERSION("~"), //
        PLUS("+"), //
        MINUS("-"), //
        BINARY("BINARY"); //

        private String s;

        DorisUnaryPrefixOperator(String s) {
            this.s = s;
        }

        public static DorisUnaryPrefixOperator getRandom() {
            return Randomly.fromOptions(values());
        }

        @Override
        public String getTextRepresentation() {
            return s;
        }
    }

    public DorisUnaryPrefixOperation(DorisExpression expr, DorisUnaryPrefixOperator op) {
        super(expr, op);
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.PREFIX;
    }

}
