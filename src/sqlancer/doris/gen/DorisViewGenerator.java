package sqlancer.doris.gen;

import java.util.HashSet;
import java.util.Set;

import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.doris.DorisErrors;
import sqlancer.doris.DorisProvider.DorisGlobalState;

public final class DorisViewGenerator {

    private DorisViewGenerator() {
    }

    public static Query getQuery(DorisGlobalState globalState) {
        int nrColumns = Randomly.smallNumber() + 1;
        StringBuilder sb = new StringBuilder("CREATE ");
        if (Randomly.getBoolean()) {
            sb.append("OR REPLACE ");
        }
        if (Randomly.getBoolean()) {
            sb.append("ALGORITHM=");
            sb.append(Randomly.fromOptions("UNDEFINED", "MERGE", "TEMPTABLE"));
            sb.append(" ");
        }
        sb.append("VIEW ");
        sb.append(globalState.getSchema().getFreeViewName());
        sb.append("(");
        for (int i = 0; i < nrColumns; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append("c" + i);
        }
        sb.append(") AS ");
        sb.append(DorisRandomQuerySynthesizer.generate(globalState, nrColumns).getQueryString());
        Set<String> errors = new HashSet<>();
        DorisErrors.addExpressionErrors(errors);
        errors.add(
                "references invalid table(s) or column(s) or function(s) or definer/invoker of view lack rights to use them");
        errors.add("Unknown column ");
        if (Randomly.getBoolean()) {
            sb.append(" WITH ");
            sb.append(Randomly.fromOptions("CASCADED", "LOCAL"));
            sb.append(" ");
            sb.append(" CHECK OPTION");
        }
        return new QueryAdapter(sb.toString(), errors, true);
    }

}
