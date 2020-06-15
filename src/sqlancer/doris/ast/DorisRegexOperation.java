package sqlancer.doris.ast;

import sqlancer.Randomly;
import sqlancer.ast.BinaryOperatorNode;
import sqlancer.ast.BinaryOperatorNode.Operator;
import sqlancer.doris.ast.DorisRegexOperation.DorisRegexOperator;

public class DorisRegexOperation extends BinaryOperatorNode<DorisExpression, DorisRegexOperator>
        implements DorisExpression {

    public enum DorisRegexOperator implements Operator {
        LIKE("LIKE"), //
        NOT_LIKE("NOT LIKE"), //
        ILIKE("REGEXP"), //
        NOT_REGEXP("NOT REGEXP");

        private String textRepr;

        DorisRegexOperator(String textRepr) {
            this.textRepr = textRepr;
        }

        public static DorisRegexOperator getRandom() {
            return Randomly.fromOptions(values());
        }

        @Override
        public String getTextRepresentation() {
            return textRepr;
        }

    }

    public DorisRegexOperation(DorisExpression left, DorisExpression right, DorisRegexOperator op) {
        super(left, right, op);
    }

}
