package com.phiau.platform.sharding;

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * 分库数据库和数据表配置
 *
 * @author phiau
 * @date 2023.08.15 17:07
 */
public class ShardingDatabaseAndTableConfiguration {

    private final static String SHARDING_DATABASE_ALIAS_PREFIX = "ds";

    private static int FIRST_YEAR = 2023;    // 数据表开始的年份
    private static int FIRST_MONTH = 8;      // 数据表开始的月份

    /**
     * 生成订单表后缀
     * @return
     */
    public static List<Integer> genOrderTableSuffix() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 12);
        int nowYear = calendar.get(Calendar.YEAR);
        int nowMonth = calendar.get(Calendar.MONTH);

        List<Integer> list = new ArrayList<>();
        int year = FIRST_YEAR;
        int month = FIRST_MONTH;
        while (true) {
            list.add(year * 100 + month);
            month++;
            if (month > 12) {
                year += 1;
                month = 1;
            }
            if (year >= nowYear && month > nowMonth) {
                break;
            }
        }
        return list;
    }


    /**
     * 数据库 url 列表
     *
     * @return
     */
    private static List<DataSourceConfig> dataSourceConfigs() {
        List<DataSourceConfig> list = new ArrayList<>();

        DataSourceConfig config1 = new DataSourceConfig();
        config1.setName("shard01");
        config1.setHost("192.168.56.10");
        config1.setPort(3306);
        config1.setUserName("root");
        config1.setPassword("123456");

        list.add(config1);

        return list;
    }

    /**
     * 创建数据源
     *
     * @return
     */
    private static Map<String, DataSource> createDataSourceMap() {
        List<DataSourceConfig> list = dataSourceConfigs();
        if (!CollectionUtils.isEmpty(list)) {
            Map<String, DataSource> map = new HashMap<>();
            int n = 0;
            for (DataSourceConfig config : list) {
                map.put(SHARDING_DATABASE_ALIAS_PREFIX + n, DataSourceUtil.createDataSource(config));
            }
            return map;
        }
        return null;
    }

    private static ShardingRuleConfiguration createShardingRuleConfiguration(int firstDataBase, int lastDataBase) {
        ShardingRuleConfiguration configuration = new ShardingRuleConfiguration();

        configuration.getTables().add(getOrderTableRuleConfiguration(firstDataBase, lastDataBase));

        // 定义具体的分片规则算法，用于提供分库分表的算法规则
        Properties props = new Properties();
        props.setProperty("algorithm-expression", SHARDING_DATABASE_ALIAS_PREFIX + "${user_id%2}"); // 表示根据user_id取模得到目标表
        configuration.getShardingAlgorithms().put("inline", new ShardingSphereAlgorithmConfiguration("INLINE", props));

//        这是另一种规则
//        Properties properties = new Properties();
//        properties.setProperty("algorithm-expression", "t_u_order_${order_id%2}");
//        configuration.getShardingAlgorithms().put("order_inline", new ShardingSphereAlgorithmConfiguration("INLINE", properties));

        // 这个是自定义的分片规则算法
        Properties properties = new Properties();
        properties.setProperty("strategy", "standard");
        properties.setProperty("algorithmClassName", OrderTableShardingAlgorithm.class.getName());
        configuration.getShardingAlgorithms().put("phiau", new ShardingSphereAlgorithmConfiguration("PHIAU", properties));

        /** 设置数据库的分片规则 */
//        如果没有设置多个数据库，但是这里设置了数据库路由，那么会报错，说路由不到 “no database route info”
//        StandardShardingStrategyConfiguration databaseStrategyConfiguration = new StandardShardingStrategyConfiguration("user_id", "inline");
//        configuration.setDefaultDatabaseShardingStrategy(databaseStrategyConfiguration);

        /** 设置数据表的分片规则 */
        StandardShardingStrategyConfiguration tableStrategyConfiguration = new StandardShardingStrategyConfiguration("order_id", "phiau");
        configuration.setDefaultTableShardingStrategy(tableStrategyConfiguration);

        configuration.getKeyGenerators().put("snowflake",new ShardingSphereAlgorithmConfiguration("SNOWFLAKE",getProperties()));

        return configuration;
    }

    private static Properties getProperties(){

        Properties properties=new Properties();

        properties.setProperty("worker-id","123");

        return properties;

    }

    /**
     * 创建订单表的分片规则
     *
     * @return
     */
    private static ShardingTableRuleConfiguration getOrderTableRuleConfiguration(int firstDatabaseAlias, int lastDatabaseAlias) {
        String actualDataNodes;
        // 表后缀列表
        List<Integer> suffix = genOrderTableSuffix();
        String suffixStr = StringUtil.format(suffix, ",");
        if (firstDatabaseAlias < lastDatabaseAlias) {
            actualDataNodes = String.format("%s${%d..%d}.t_u_order_$->{[%s]}", SHARDING_DATABASE_ALIAS_PREFIX, firstDatabaseAlias, lastDatabaseAlias, suffixStr);
        } else {
            actualDataNodes = String.format("%s${%d}.t_u_order_$->{[%s]}", SHARDING_DATABASE_ALIAS_PREFIX, firstDatabaseAlias, suffixStr);
        }
        ShardingTableRuleConfiguration tableRule = new ShardingTableRuleConfiguration("t_u_order", actualDataNodes);
        tableRule.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
        return tableRule;
    }

    public static DataSource getDataSource() throws SQLException {
        Map<String, DataSource> map = createDataSourceMap();
        ShardingRuleConfiguration shardingRuleConfiguration = createShardingRuleConfiguration(0, 0);
        DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(map, Collections.singleton(shardingRuleConfiguration), new Properties());
        return dataSource;
    }
}
