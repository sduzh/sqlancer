package sqlancer.doris;

import java.util.Set;

public final class DorisErrors {

    private DorisErrors() {
    }

    public static void addExpressionErrors(Set<String> errors) {
        errors.add("select list expression not produced by aggregation output");
        errors.add("Invalid type cast of");
        errors.add("type not match, originType=");
        errors.add("Unexpected exception: tupleisNull fn == NULL");
        errors.add("Cross join can't be used with ON clause");
        errors.add("avg requires a numeric parameter:");
        errors.add("sum requires a numeric parameter");
        errors.add("Invalid number format:");
        errors.add("is not a number");
        errors.add("Invalid floating-point literal");
        errors.add("No matching function with signature");
        errors.add("left operand of LIKE must be of type STRING");
        errors.add("right operand of LIKE must be of type STRING");
        errors.add("left operand of REGEXP must be of type STRING");
        errors.add("right operand of REGEXP must be of type STRING");
        errors.add("requires return type 'BOOLEAN'");
        errors.add("should return type 'BOOLEAN'");
        errors.add("GROUP BY: ordinal exceeds number of items in select list");
        errors.add("GROUP BY: ordinal must be");
        errors.add("ORDER BY expression not produced by aggregation output");
        errors.add("ORDER BY: ordinal exceeds number of items in select list");
        errors.add("ORDER BY: ordinal must be >= 1");
        errors.add("HAVING clause not produced by aggregation output");
        errors.add("Syntax error in line ");
        errors.add("LEFT OUTER JOIN requires an ON or USING clause");
        errors.add("RIGHT OUTER JOIN requires an ON or USING clause");
        errors.add("Invalid regular expression in");
        errors.add("Failed analysis after expr substitution"); // https://github.com/DorisDB/DorisDB/issues/310
        errors.add("non-equal LEFT OUTER JOIN is not supported");
        errors.add("Unexpected exception: null");
        errors.add("Incompatible return types");
        errors.add("non-equal RIGHT OUTER JOIN is not supported");
        // https://github.com/apache/incubator-doris/issues/3862
        errors.add("Unable to find _ZN5doris18DecimalV2Operators16cast_to_date_valEPN9doris_udf15FunctionContextERKNS1_12DecimalV2ValE");
        // https://github.com/apache/incubator-doris/issues/3864
        errors.add("Unable to find _ZN5doris13CastFunctions16cast_to_date_valEPN9doris_udf15FunctionContextERKNS1_11LargeIntValE");
        errors.add("Unable to find _ZN5doris13CastFunctions20cast_to_datetime_valEPN9doris_udf15FunctionContextERKNS1_11LargeIntValE");
        errors.add("org.apache.doris.analysis.DecimalLiteral cannot be cast to org.apache.doris.analysis.StringLiteral");
        errors.add("can't be assigned to some PlanNode.");
        errors.add("GROUP BY expression must not contain aggregate functions");
        errors.add("cannot be cast to org.apache.doris.analysis.StringLiteral");
        errors.add("Unexpected exception: No match for");
    }

    public static void addExpressionHavingErrors(Set<String> errors) {
    }

    public static void addInsertErrors(Set<String> errors) {
        errors.add("Invalid number format:");
        errors.add("is not a number");
        errors.add("Invalid floating-point literal");
        errors.add("Invalid integer literal");
        errors.add("No matching function with signature");
        errors.add("left operand of LIKE must be of type STRING");
        errors.add("all partitions have no load data");
        errors.add("must be explicitly mentioned in column permutation");
        errors.add("Number out of range");
        errors.add("Invalid type cast of");
        errors.add("Unexpected exception: null"); // https://github.com/DorisDB/DorisDB/issues/310
    }

}
