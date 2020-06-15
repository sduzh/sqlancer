package sqlancer.doris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sqlancer.IgnoreMeException;
import sqlancer.Randomly;
import sqlancer.gen.UntypedExpressionGenerator;
import sqlancer.doris.DorisProvider.DorisGlobalState;
import sqlancer.doris.DorisSchema.DorisColumn;
import sqlancer.doris.DorisSchema.DorisDataType;
import sqlancer.doris.ast.DorisAggregate;
import sqlancer.doris.ast.DorisAggregate.DorisAggregateFunction;
import sqlancer.doris.ast.DorisBinaryArithmeticOperation;
import sqlancer.doris.ast.DorisBinaryArithmeticOperation.DorisBinaryArithmeticOperator;
import sqlancer.doris.ast.DorisBinaryBitOperation;
import sqlancer.doris.ast.DorisBinaryBitOperation.DorisBinaryBitOperator;
import sqlancer.doris.ast.DorisBinaryComparisonOperation;
import sqlancer.doris.ast.DorisBinaryComparisonOperation.DorisComparisonOperator;
import sqlancer.doris.ast.DorisBinaryLogicalOperation;
import sqlancer.doris.ast.DorisBinaryLogicalOperation.DorisBinaryLogicalOperator;
import sqlancer.doris.ast.DorisCase;
import sqlancer.doris.ast.DorisCastOperation;
import sqlancer.doris.ast.DorisColumnReference;
import sqlancer.doris.ast.DorisConstant;
import sqlancer.doris.ast.DorisExpression;
import sqlancer.doris.ast.DorisFunctionCall;
import sqlancer.doris.ast.DorisFunctionCall.DorisFunction;
import sqlancer.doris.ast.DorisOrderingTerm;
import sqlancer.doris.ast.DorisRegexOperation;
import sqlancer.doris.ast.DorisRegexOperation.DorisRegexOperator;
import sqlancer.doris.ast.DorisUnaryPostfixOperation;
import sqlancer.doris.ast.DorisUnaryPostfixOperation.DorisUnaryPostfixOperator;
import sqlancer.doris.ast.DorisUnaryPrefixOperation;
import sqlancer.doris.ast.DorisUnaryPrefixOperation.DorisUnaryPrefixOperator;

public class DorisExpressionGenerator extends UntypedExpressionGenerator<DorisExpression, DorisColumn> {

    private final DorisGlobalState globalState;

    public DorisExpressionGenerator(DorisGlobalState globalState) {
        this.globalState = globalState;
    }

    private enum Gen {
        UNARY_PREFIX, //
        UNARY_POSTFIX, //
        CONSTANT, //
        COLUMN, //
        COMPARISON,
        REGEX,
        FUNCTION,
        BINARY_LOGICAL,
        CAST,
        // CASE // https://github.com/apache/incubator-doris/issues/3874
        BINARY_ARITHMETIC
    }

    @Override
    protected DorisExpression generateExpression(int depth) {
        if (depth >= globalState.getOptions().getMaxExpressionDepth() || Randomly.getBoolean()) {
            return generateLeafNode();
        }
        if (allowAggregates && Randomly.getBoolean()) {
            allowAggregates = false;
            DorisAggregateFunction func = DorisAggregateFunction.getRandom();
            List<DorisExpression> args = generateExpressions(func.getNrArgs());
            return new DorisAggregate(args, func);
        }
        switch (Randomly.fromOptions(Gen.values())) {
        case UNARY_POSTFIX:
            return new DorisUnaryPostfixOperation(generateExpression(depth + 1), DorisUnaryPostfixOperator.getRandom());
        case UNARY_PREFIX:
            DorisUnaryPrefixOperator rand = DorisUnaryPrefixOperator.getRandom();
            return new DorisUnaryPrefixOperation(generateExpression(depth + 1), rand);
        case COLUMN:
            return generateColumn();
        case CONSTANT:
            return generateConstant();
        case COMPARISON:
            return new DorisBinaryComparisonOperation(generateExpression(depth + 1), generateExpression(depth + 1),
                    DorisComparisonOperator.getRandom());
        case REGEX:
            return new DorisRegexOperation(generateExpression(depth + 1), generateExpression(depth + 1),
                    DorisRegexOperator.getRandom());
        case FUNCTION:
            DorisFunction func = DorisFunction.getRandom();
            return new DorisFunctionCall(func, generateExpressions(depth, func.getNrArgs()));
        case BINARY_LOGICAL:
            return new DorisBinaryLogicalOperation(generateExpression(depth + 1), generateExpression(depth + 1),
                    DorisBinaryLogicalOperator.getRandom());
        case BINARY_ARITHMETIC:
            return new DorisBinaryArithmeticOperation(generateExpression(depth + 1), generateExpression(depth + 1),
                    DorisBinaryArithmeticOperator.getRandom());
        case CAST:
            return new DorisCastOperation(generateExpression(depth + 1), Randomly.fromOptions(
                        "TINYINT",
                        "SMALLINT",
                        "INT",
                        "BIGINT",
                        "LARGEINT",
                        "CHAR",
                        "DATE",
                        "DATETIME",
                        "DECIMAL"));
        // case CASE:
        //     int nr = Randomly.fromOptions(1, 2);
        //     return new DorisCase(generateExpression(depth + 1), generateExpressions(depth + 1, nr),
        //             generateExpressions(depth + 1, nr), generateExpression(depth + 1));
        default:
            throw new AssertionError();
        }
    }

    @Override
    protected DorisExpression generateColumn() {
        DorisColumn column = Randomly.fromList(columns);
        return new DorisColumnReference(column);
    }

    @Override
    public DorisExpression generateConstant() {
        DorisDataType type = DorisDataType.getRandom();
        if (Randomly.getBooleanWithRatherLowProbability()) {
            return DorisConstant.createNullConstant();
        }
        switch (type) {
        case INT:
            return DorisConstant.createIntConstant(globalState.getRandomly().getInteger());
        case FLOATING:
            return DorisConstant.createFloatConstant(globalState.getRandomly().getDouble());
        case CHAR:
            return DorisConstant.createStringConstant(globalState.getRandomly().getChar());
        case DECIMAL:
            return DorisConstant.createIntConstant(globalState.getRandomly().getInteger());
        default:
            throw new AssertionError();
        }
    }

    @Override
    public List<DorisExpression> generateOrderBys() {
        List<DorisExpression> expressions = super.generateOrderBys();
        List<DorisExpression> newExpressions = new ArrayList<>();
        for (DorisExpression expr : expressions) {
            DorisExpression newExpr = expr;
            if (Randomly.getBoolean()) {
                newExpr = new DorisOrderingTerm(expr, Randomly.getBoolean());
            }
            newExpressions.add(newExpr);
        }
        return newExpressions;
    }

}
