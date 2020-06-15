package sqlancer.doris.ast;

import sqlancer.doris.DorisSchema.DorisColumn;

public class DorisColumnReference implements DorisExpression {

    private final DorisColumn c;

    public DorisColumnReference(DorisColumn c) {
        this.c = c;
    }

    public DorisColumn getColumn() {
        return c;
    }

}
