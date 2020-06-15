package sqlancer.doris.ast;

import sqlancer.Randomly;
import sqlancer.ast.BinaryOperatorNode;
import sqlancer.ast.BinaryOperatorNode.Operator;
import sqlancer.doris.ast.DorisBinaryArithmeticOperation.DorisBinaryArithmeticOperator;

public class DorisBinaryArithmeticOperation extends BinaryOperatorNode<DorisExpression, DorisBinaryArithmeticOperator>
        implements DorisExpression {

    public enum DorisBinaryArithmeticOperator implements Operator {
        ADD("+"), //
        MINUS("-"), //
        MULT("*"), //
        DIV("/"), //
        INTEGER_DIV("DIV"), //
        MOD("%"); //

        String textRepresentation;

        DorisBinaryArithmeticOperator(String textRepresentation) {
            this.textRepresentation = textRepresentation;
        }

        public static DorisBinaryArithmeticOperator getRandom() {
            return Randomly.fromOptions(values());
        }

        @Override
        public String getTextRepresentation() {
            return textRepresentation;
        }
    }

    public DorisBinaryArithmeticOperation(DorisExpression left, DorisExpression right, DorisBinaryArithmeticOperator op) {
        super(left, right, op);
    }

}
