package sqlancer.doris.ast;

public class DorisCastOperation implements DorisExpression {

    private final DorisExpression expr;
    private final String type;

    public DorisCastOperation(DorisExpression expr, String type) {
        this.expr = expr;
        this.type = type;
    }

    public DorisExpression getExpr() {
        return expr;
    }

    public String getType() {
        return type;
    }

}
