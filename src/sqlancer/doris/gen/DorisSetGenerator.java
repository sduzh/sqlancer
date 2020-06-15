package sqlancer.doris.gen;

import java.sql.SQLException;
import java.util.function.Function;

import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.doris.DorisProvider.DorisGlobalState;

public final class DorisSetGenerator {

    private DorisSetGenerator() {
    }

    private enum Action {

        // SQL_MODE("sql_mode", (r) -> Randomly.fromOptions("TRADITIONAL", "ANSI", "POSTGRESQL", "ORACLE")),
        TIDB_OPT_AGG_PUSH_DOWN("doris_opt_agg_push_down",
                (r) -> Randomly.fromOptions(0, 1)), TIDB_BUILD_STATS_CONCURRENCY("doris_build_stats_concurrency",
                        (r) -> Randomly.getNotCachedInteger(0, 500)), TIDB_CHECKSUM_TABLE_CONCURRENCY(
                                "doris_checksum_table_concurrency",
                                (r) -> Randomly.getNotCachedInteger(0, 500)), TIDB_DISTSQL_SCAN_CONCURRENCY(
                                        "doris_distsql_scan_concurrency",
                                        (r) -> Randomly.getNotCachedInteger(1, 500)), TIDB_INDEX_LOOKUP_SIZE(
                                                "doris_index_lookup_size",
                                                (r) -> Randomly.getNotCachedInteger(1,
                                                        100000)), TIDB_INDEX_LOOKUP_CONCURRENCY(
                                                                "doris_index_lookup_concurrency",
                                                                (r) -> Randomly.getNotCachedInteger(1,
                                                                        100)), TIDB_INDEX_LOOKUP_JOIN_CONCURRENCY(
                                                                                "doris_index_lookup_join_concurrency",
                                                                                (r) -> Randomly.getNotCachedInteger(1,
                                                                                        100)), TIDB_HASH_JOIN_CONCURRENCY(
                                                                                                "doris_hash_join_concurrency",
                                                                                                (r) -> Randomly
                                                                                                        .getNotCachedInteger(
                                                                                                                1,
                                                                                                                100)), TIDB_INDEX_SERIAL_SCAN_CONCURRENCY(
                                                                                                                        "doris_index_serial_scan_concurrency",
                                                                                                                        (r) -> Randomly
                                                                                                                                .getNotCachedInteger(
                                                                                                                                        1,
                                                                                                                                        100)), TIDB_PROJECTION_CONCURRENCY(
                                                                                                                                                "doris_projection_concurrency",
                                                                                                                                                (r) -> Randomly
                                                                                                                                                        .getNotCachedInteger(
                                                                                                                                                                1,
                                                                                                                                                                100)), TIDB_HASHAGG_PARTIAL_CONCURRENCY(
                                                                                                                                                                        "doris_hashagg_partial_concurrency",
                                                                                                                                                                        (r) -> Randomly
                                                                                                                                                                                .getNotCachedInteger(
                                                                                                                                                                                        1,
                                                                                                                                                                                        100)), TIDB_HASHAGG_FINAL_CONCURRENCY(
                                                                                                                                                                                                "doris_hashagg_final_concurrency",
                                                                                                                                                                                                (r) -> Randomly
                                                                                                                                                                                                        .getNotCachedInteger(
                                                                                                                                                                                                                1,
                                                                                                                                                                                                                100)), TIDB_INDEX_JOIN_BATCH_SIZE(
                                                                                                                                                                                                                        "doris_index_join_batch_size",
                                                                                                                                                                                                                        (r) -> Randomly
                                                                                                                                                                                                                                .getNotCachedInteger(
                                                                                                                                                                                                                                        1,
                                                                                                                                                                                                                                        5000)), //
        TIDB_INDEX_SKIP_UTF8_CHECK("doris_skip_utf8_check", (r) -> Randomly.fromOptions(0, 1)), TIDB_INIT_CHUNK_SIZE(
                "doris_init_chunk_size",
                (r) -> Randomly.getNotCachedInteger(1, 32)), TIDB_MAX_CHUNK_SIZE("doris_max_chunk_size",
                        (r) -> Randomly.getNotCachedInteger(32, 50000)), TIDB_CONSTRAINT_CHECK_IN_PLACE(
                                "doris_constraint_check_in_place",
                                (r) -> Randomly.fromOptions(0, 1)), TIDB_OPT_INSUBQ_TO_JOIN_AND_AGG(
                                        "doris_opt_insubq_to_join_and_agg",
                                        (r) -> Randomly.fromOptions(0, 1)), TIDB_OPT_CORRELATION_THRESHOLD(
                                                "doris_opt_correlation_threshold",
                                                (r) -> Randomly.fromOptions(0, 0.0001, 0.1, 0.25, 0.50, 0.75, 0.9,
                                                        0.9999999, 1)), TIDB_OPT_CORRELATION_EXP_FACTOR(
                                                                "doris_opt_correlation_exp_factor",
                                                                (r) -> Randomly.getNotCachedInteger(0, 10000)),

        TIDB_ENABLE_WINDOW_FUNCTION("doris_enable_window_function", (r) -> Randomly.fromOptions(0, 1)),

        TIDB_ENABLE_FAST_ANALYZE("doris_enable_fast_analyze",
                (r) -> Randomly.fromOptions(0, 1)), TIDB_WAIT_SPLIT_REGION_FINISH("doris_wait_split_region_finish",
                        (r) -> Randomly.fromOptions(0, 1)),
        // TODO: global
        // TIDB_SCATTER_REGION("doris_scatter_region", (r) -> Randomly.fromOptions(0, 1));
        TIDB_ENABLE_STMT_SUMMARY("doris_enable_stmt_summary", (r) -> Randomly.fromOptions(0, 1)), TIDB_ENABLE_CHUNK_RPC(
                "doris_enable_chunk_rpc", (r) -> Randomly.fromOptions(0, 1));

        private String name;
        private Function<Randomly, Object> prod;

        Action(String name, Function<Randomly, Object> prod) {
            this.name = name;
            this.prod = prod;
        }

    }

    public static Query getQuery(DorisGlobalState globalState) throws SQLException {
        StringBuilder sb = new StringBuilder();
        Action option = Randomly.fromOptions(Action.values());
        sb.append("set @@");
        sb.append(option.name);
        sb.append("=");
        sb.append(option.prod.apply(globalState.getRandomly()));
        return new QueryAdapter(sb.toString());
    }

}
