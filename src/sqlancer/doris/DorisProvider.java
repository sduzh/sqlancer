package sqlancer.doris;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import sqlancer.AbstractAction;
import sqlancer.CompositeTestOracle;
import sqlancer.DatabaseProvider;
import sqlancer.GlobalState;
import sqlancer.IgnoreMeException;
import sqlancer.Main.QueryManager;
import sqlancer.Main.StateLogger;
import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.QueryProvider;
import sqlancer.Randomly;
import sqlancer.StateToReproduce;
import sqlancer.StateToReproduce.MySQLStateToReproduce;
import sqlancer.StatementExecutor;
import sqlancer.TestOracle;
import sqlancer.doris.DorisProvider.DorisGlobalState;
import sqlancer.doris.gen.DorisInsertGenerator;
import sqlancer.doris.gen.DorisRandomQuerySynthesizer;
import sqlancer.doris.gen.DorisTableGenerator;

public class DorisProvider implements DatabaseProvider<DorisGlobalState, DorisOptions> {

    public enum Action implements AbstractAction<DorisGlobalState> {
        INSERT(DorisInsertGenerator::getQuery), //
        TRUNCATE((g) -> new QueryAdapter("TRUNCATE TABLE " + g.getSchema().getRandomTable(t -> !t.isView()).getName())),
        EXPLAIN((g) -> {
                    Set<String> errors = new HashSet<>();
                    DorisErrors.addExpressionErrors(errors);
                    DorisErrors.addExpressionHavingErrors(errors);
                    return new QueryAdapter("EXPLAIN "
                            + DorisRandomQuerySynthesizer.generate(g, Randomly.smallNumber() + 1).getQueryString(), errors);
                });

        private final QueryProvider<DorisGlobalState> queryProvider;

        Action(QueryProvider<DorisGlobalState> queryProvider) {
            this.queryProvider = queryProvider;
        }

        @Override
        public Query getQuery(DorisGlobalState state) throws SQLException {
            return queryProvider.getQuery(state);
        }
    }

    public static class DorisGlobalState extends GlobalState<DorisOptions> {

        private DorisSchema schema;

        public void setSchema(DorisSchema schema) {
            this.schema = schema;
        }

        public DorisSchema getSchema() {
            return schema;
        }

    }

    private static int mapActions(DorisGlobalState globalState, Action a) {
        Randomly r = globalState.getRandomly();
        switch (a) {
            case INSERT:
            case EXPLAIN:
                return r.getInteger(0, globalState.getOptions().getMaxNumberInserts());
            case TRUNCATE:
                return r.getInteger(0, 2);
            default:
                throw new AssertionError(a);
        }

    }

    @Override
    public DorisGlobalState generateGlobalState() {
        return new DorisGlobalState();
    }

    @Override
    public void generateAndTestDatabase(DorisGlobalState globalState) throws SQLException {
        QueryManager manager = globalState.getManager();
        Connection con = globalState.getConnection();
        String databaseName = globalState.getDatabaseName();
        globalState.setSchema(DorisSchema.fromConnection(con, databaseName));
        StateLogger logger = globalState.getLogger();
        StateToReproduce state = globalState.getState();
        for (int i = 0; i < Randomly.fromOptions(1, 2); i++) {
            boolean success = false;
            do {
                Query qt = new DorisTableGenerator().getQuery(globalState);
                success = manager.execute(qt);
                logger.writeCurrent(state);
                globalState.setSchema(DorisSchema.fromConnection(con, databaseName));
                try {
                    logger.getCurrentFileWriter().close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                logger.currentFileWriter = null;
            } while (!success);
        }
        globalState.setSchema(DorisSchema.fromConnection(con, databaseName));

        StatementExecutor<DorisGlobalState, Action> se = new StatementExecutor<>(globalState, Action.values(),
                DorisProvider::mapActions, (q) -> {
                    if (q.couldAffectSchema()) {
                        try {
                            globalState.setSchema(DorisSchema.fromConnection(con, databaseName));
                        } catch (SQLException e) {
                            throw new AssertionError(e);
                        }
                    }
                    if (globalState.getSchema().getDatabaseTables().isEmpty()) {
                        throw new IgnoreMeException();
                    }
                });
        se.executeStatements();
        manager.incrementCreateDatabase();
        List<TestOracle> oracles = globalState.getDmbsSpecificOptions().oracle.stream().map(o -> {
            try {
                return o.create(globalState);
            } catch (SQLException e1) {
                throw new AssertionError(e1);
            }
        }).collect(Collectors.toList());
        CompositeTestOracle oracle = new CompositeTestOracle(oracles);

        for (int i = 0; i < globalState.getOptions().getNrQueries(); i++) {
            try {
                oracle.check();
                manager.incrementSelectQueryCount();
            } catch (IgnoreMeException e) {

            }
        }
        try {
            if (globalState.getOptions().logEachSelect()) {
                logger.getCurrentFileWriter().close();
                logger.currentFileWriter = null;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public Connection createDatabase(GlobalState<?> globalState) throws SQLException {
        String databaseName = globalState.getDatabaseName();
        String url = "jdbc:mysql://127.0.0.1:9033/";
        Connection con = DriverManager.getConnection(url, globalState.getOptions().getUserName(),
                globalState.getOptions().getPassword());
        globalState.getState().statements.add(new QueryAdapter("USE test"));
        globalState.getState().statements.add(new QueryAdapter("DROP DATABASE IF EXISTS " + databaseName));
        String createDatabaseCommand = "CREATE DATABASE " + databaseName;
        globalState.getState().statements.add(new QueryAdapter(createDatabaseCommand));
        globalState.getState().statements.add(new QueryAdapter("USE " + databaseName));
        try (Statement s = con.createStatement()) {
            s.execute("DROP DATABASE IF EXISTS " + databaseName);
        }
        try (Statement s = con.createStatement()) {
            s.execute(createDatabaseCommand);
        }
        con.close();
        con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:9033/" + databaseName,
                globalState.getOptions().getUserName(), globalState.getOptions().getPassword());
        return con;
    }

    @Override
    public String getDBMSName() {
        return "doris";
    }

    @Override
    public StateToReproduce getStateToReproduce(String databaseName) {
        return new MySQLStateToReproduce(databaseName);
    }

    @Override
    public DorisOptions getCommand() {
        return new DorisOptions();
    }

}
