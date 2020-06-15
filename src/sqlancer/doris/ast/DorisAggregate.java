package sqlancer.doris.ast;

import java.util.List;

import sqlancer.Randomly;
import sqlancer.ast.FunctionNode;
import sqlancer.doris.ast.DorisAggregate.DorisAggregateFunction;

public class DorisAggregate extends FunctionNode<DorisAggregateFunction, DorisExpression> implements DorisExpression {

    public enum DorisAggregateFunction {
        COUNT(1), //
        SUM(1), //
        AVG(1), //
        MIN(1), //
        MAX(1);

        private int nrArgs;

        DorisAggregateFunction(int nrArgs) {
            this.nrArgs = nrArgs;
        }

        public static DorisAggregateFunction getRandom() {
            return Randomly.fromOptions(values());
        }

        public int getNrArgs() {
            return nrArgs;
        }

    }

    public DorisAggregate(List<DorisExpression> args, DorisAggregateFunction func) {
        super(func, args);
    }

}
