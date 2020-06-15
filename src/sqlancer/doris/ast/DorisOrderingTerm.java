package sqlancer.doris.ast;

import sqlancer.visitor.UnaryOperation;

public class DorisOrderingTerm implements UnaryOperation<DorisExpression>, DorisExpression {

    private final DorisExpression expr;
    private final boolean asc;

    public DorisOrderingTerm(DorisExpression expr, boolean asc) {
        this.expr = expr;
        this.asc = asc;
    }

    @Override
    public DorisExpression getExpression() {
        return expr;
    }

    @Override
    public String getOperatorRepresentation() {
        return asc ? "ASC" : "DESC";
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.POSTFIX;
    }

    @Override
    public boolean omitBracketsWhenPrinting() {
        return true;
    }

}
