package com.phiau.platform.sharding;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

/**
 * 订单数据表分片算法
 *
 * @author phiau
 * @date 2023.08.15 16:08
 */
public class OrderTableShardingAlgorithm implements StandardShardingAlgorithm<String> {

    private Properties props = new Properties();

    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<String> preciseShardingValue) {
        if (!collection.isEmpty()) {
            String value = preciseShardingValue.getValue().substring(0, 6);  // 订单的值
            Iterator<String> it = collection.iterator();
            while (it.hasNext()) {
                String tableName = it.next();
                if (tableName.endsWith(value)) {
                    return tableName;
                }
            }
        }
        throw new NullPointerException();
    }

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<String> rangeShardingValue) {
        return collection;
    }

    @Override
    public void init() {
    }

    @Override
    public String getType() {
        return "PHIAU";
    }

    @Override
    public Properties getProps() {
        return this.props;
    }

    @Override
    public void setProps(Properties properties) {
        this.props = properties;
    }
}
