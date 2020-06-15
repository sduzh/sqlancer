package sqlancer.doris.ast;

import sqlancer.doris.DorisSchema.DorisTable;

public class DorisTableReference implements DorisExpression {

    private DorisTable table;

    public DorisTableReference(DorisTable table) {
        this.table = table;
    }

    public DorisTable getTable() {
        return table;
    }

}
