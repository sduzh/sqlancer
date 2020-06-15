package sqlancer.doris;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import sqlancer.CompositeTestOracle;
import sqlancer.MainOptions.DBMSConverter;
import sqlancer.TestOracle;
import sqlancer.doris.DorisProvider.DorisGlobalState;
import sqlancer.doris.oracle.DorisTLPHavingOracle;
import sqlancer.doris.oracle.DorisTLPWhereOracle;

@Parameters
public class DorisOptions {

    @Parameter(names = "--oracle", converter = DBMSConverter.class)
    public List<DorisOracle> oracle = Arrays.asList(DorisOracle.QUERY_PARTITIONING);

    public enum DorisOracle {
        HAVING {
            @Override
            public TestOracle create(DorisGlobalState globalState) throws SQLException {
                return new DorisTLPHavingOracle(globalState);
            }
        },
        WHERE {
            @Override
            public TestOracle create(DorisGlobalState globalState) throws SQLException {
                return new DorisTLPWhereOracle(globalState);
            }
        },
        QUERY_PARTITIONING {
            @Override
            public TestOracle create(DorisGlobalState globalState) throws SQLException {
                List<TestOracle> oracles = new ArrayList<>();
                oracles.add(new DorisTLPWhereOracle(globalState));
                oracles.add(new DorisTLPHavingOracle(globalState));
                return new CompositeTestOracle(oracles);
            }
        };

        public abstract TestOracle create(DorisGlobalState globalState) throws SQLException;

    }

}
