package sqlancer.doris.ast;

import sqlancer.Randomly;
import sqlancer.ast.BinaryOperatorNode.Operator;
import sqlancer.ast.UnaryOperatorNode;
import sqlancer.doris.ast.DorisUnaryPostfixOperation.DorisUnaryPostfixOperator;

public class DorisUnaryPostfixOperation extends UnaryOperatorNode<DorisExpression, DorisUnaryPostfixOperator>
        implements DorisExpression {

    public enum DorisUnaryPostfixOperator implements Operator {
        IS_NULL("IS NULL"), //
        IS_NOT_NULL("IS NOT NULL"); //

        private String s;

        DorisUnaryPostfixOperator(String s) {
            this.s = s;
        }

        public static DorisUnaryPostfixOperator getRandom() {
            return Randomly.fromOptions(values());
        }

        @Override
        public String getTextRepresentation() {
            return s;
        }
    }

    public DorisUnaryPostfixOperation(DorisExpression expr, DorisUnaryPostfixOperator op) {
        super(expr, op);
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.POSTFIX;
    }

}
