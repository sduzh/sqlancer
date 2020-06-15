package sqlancer.doris.ast;

import java.util.List;

public class DorisCase implements DorisExpression {

    private List<DorisExpression> conditions;
    private List<DorisExpression> expressions;
    private DorisExpression elseExpr;
    private DorisExpression switchCondition;

    public DorisCase(DorisExpression switchCondition, List<DorisExpression> conditions, List<DorisExpression> expressions,
            DorisExpression elseExpr) {
        this.switchCondition = switchCondition;
        this.conditions = conditions;
        this.expressions = expressions;
        this.elseExpr = elseExpr;
        if (conditions.size() != expressions.size()) {
            throw new IllegalArgumentException();
        }
    }

    public DorisExpression getSwitchCondition() {
        return switchCondition;
    }

    public List<DorisExpression> getConditions() {
        return conditions;
    }

    public List<DorisExpression> getExpressions() {
        return expressions;
    }

    public DorisExpression getElseExpr() {
        return elseExpr;
    }

}
