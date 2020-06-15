package sqlancer.doris.visitor;

import sqlancer.doris.ast.DorisAggregate;
import sqlancer.doris.ast.DorisCase;
import sqlancer.doris.ast.DorisCastOperation;
import sqlancer.doris.ast.DorisColumnReference;
import sqlancer.doris.ast.DorisConstant;
import sqlancer.doris.ast.DorisExpression;
import sqlancer.doris.ast.DorisFunctionCall;
import sqlancer.doris.ast.DorisJoin;
import sqlancer.doris.ast.DorisSelect;
import sqlancer.doris.ast.DorisTableReference;
import sqlancer.doris.ast.DorisText;

public interface DorisVisitor {

    default void visit(DorisExpression expr) {
        if (expr instanceof DorisConstant) {
            visit((DorisConstant) expr);
        } else if (expr instanceof DorisColumnReference) {
            visit((DorisColumnReference) expr);
        } else if (expr instanceof DorisSelect) {
            visit((DorisSelect) expr);
        } else if (expr instanceof DorisTableReference) {
            visit((DorisTableReference) expr);
        } else if (expr instanceof DorisFunctionCall) {
            visit((DorisFunctionCall) expr);
        } else if (expr instanceof DorisJoin) {
            visit((DorisJoin) expr);
        } else if (expr instanceof DorisText) {
            visit((DorisText) expr);
        } else if (expr instanceof DorisAggregate) {
            visit((DorisAggregate) expr);
        } else if (expr instanceof DorisCastOperation) {
            visit((DorisCastOperation) expr);
        } else if (expr instanceof DorisCase) {
            visit((DorisCase) expr);
        } else {
            throw new AssertionError(expr.getClass());
        }
    }

    void visit(DorisCase caseExpr);

    void visit(DorisCastOperation cast);

    void visit(DorisAggregate aggr);

    void visit(DorisFunctionCall call);

    void visit(DorisConstant expr);

    void visit(DorisColumnReference expr);

    void visit(DorisTableReference expr);

    void visit(DorisSelect select);

    void visit(DorisJoin join);

    void visit(DorisText text);

    static String asString(DorisExpression expr) {
        DorisToStringVisitor v = new DorisToStringVisitor();
        v.visit(expr);
        return v.getString();
    }

}
