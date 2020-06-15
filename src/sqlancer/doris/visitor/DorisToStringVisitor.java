package sqlancer.doris.visitor;

import sqlancer.IgnoreMeException;
import sqlancer.Randomly;
import sqlancer.doris.ast.DorisAggregate;
import sqlancer.doris.ast.DorisCase;
import sqlancer.doris.ast.DorisCastOperation;
import sqlancer.doris.ast.DorisColumnReference;
import sqlancer.doris.ast.DorisConstant;
import sqlancer.doris.ast.DorisExpression;
import sqlancer.doris.ast.DorisFunctionCall;
import sqlancer.doris.ast.DorisJoin;
import sqlancer.doris.ast.DorisJoin.JoinType;
import sqlancer.doris.ast.DorisSelect;
import sqlancer.doris.ast.DorisTableReference;
import sqlancer.doris.ast.DorisText;
import sqlancer.visitor.ToStringVisitor;

public class DorisToStringVisitor extends ToStringVisitor<DorisExpression> implements DorisVisitor {

    @Override
    public void visitSpecific(DorisExpression expr) {
        DorisVisitor.super.visit(expr);
    }

    @Override
    public void visit(DorisConstant c) {
        sb.append(c.toString());
    }

    public String getString() {
        return sb.toString();
    }

    @Override
    public void visit(DorisColumnReference c) {
        if (c.getColumn().getTable() == null) {
            sb.append(c.getColumn().getName());
        } else {
            sb.append(c.getColumn().getFullQualifiedName());
        }
    }

    @Override
    public void visit(DorisTableReference expr) {
        sb.append(expr.getTable().getName());
    }

    @Override
    public void visit(DorisSelect select) {
        sb.append("SELECT ");
        visit(select.getFetchColumns());
        sb.append(" FROM ");
        visit(select.getFromList());
        if (!select.getFromList().isEmpty() && !select.getJoinList().isEmpty()) {
            sb.append(", ");
        }
        if (!select.getJoinList().isEmpty()) {
            visit(select.getJoinList());
        }
        if (select.getWhereClause() != null) {
            sb.append(" WHERE ");
            visit(select.getWhereClause());
        }
        if (!select.getGroupByExpressions().isEmpty()) {
            sb.append(" GROUP BY ");
            visit(select.getGroupByExpressions());
        }
        if (select.getHavingClause() != null) {
            sb.append(" HAVING ");
            visit(select.getHavingClause());
        }
        if (!select.getOrderByExpressions().isEmpty()) {
            sb.append(" ORDER BY ");
            visit(select.getOrderByExpressions());
        }
    }

    @Override
    public void visit(DorisFunctionCall call) {
        sb.append(call.getFunction());
        sb.append("(");
        visit(call.getArgs());
        sb.append(")");
    }

    @Override
    public void visit(DorisJoin join) {
        sb.append(" ");
        visit(join.getLeftTable());
        sb.append(" ");
        switch (join.getJoinType()) {
        case INNER:
            if (Randomly.getBoolean()) {
                sb.append("INNER ");
            } else {
                sb.append("CROSS ");
            }
            sb.append("JOIN ");
            break;
        case LEFT:
            sb.append("LEFT ");
            if (Randomly.getBoolean()) {
                sb.append(" OUTER ");
            }
            sb.append("JOIN ");
            break;
        case RIGHT:
            sb.append("RIGHT ");
            if (Randomly.getBoolean()) {
                sb.append(" OUTER ");
            }
            sb.append("JOIN ");
            break;
        default:
            throw new AssertionError();
        }
        visit(join.getRightTable());
        sb.append(" ");
        sb.append("ON ");
        visit(join.getOnCondition());
    }

    @Override
    public void visit(DorisText text) {
        sb.append(text.getText());
    }

    @Override
    public void visit(DorisAggregate aggr) {
        sb.append(aggr.getFunction());
        sb.append("(");
        visit(aggr.getArgs());
        sb.append(")");
    }

    @Override
    public void visit(DorisCastOperation cast) {
        sb.append("CAST(");
        visit(cast.getExpr());
        sb.append(" AS ");
        sb.append(cast.getType());
        sb.append(")");
    }

    @Override
    public void visit(DorisCase op) {
        sb.append("(CASE ");
        visit(op.getSwitchCondition());
        for (int i = 0; i < op.getConditions().size(); i++) {
            sb.append(" WHEN ");
            visit(op.getConditions().get(i));
            sb.append(" THEN ");
            visit(op.getExpressions().get(i));
        }
        if (op.getElseExpr() != null) {
            sb.append(" ELSE ");
            visit(op.getElseExpr());
        }
        sb.append(" END )");
    }
}
