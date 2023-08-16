package com.phiau.platform.sharding;

import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

/**
 * @author phiau
 * @date 2023.08.16 09:40
 */
public class StringUtil {

    /**
     * 格式化
     * @param collection
     * @param delimiter
     * @return
     */
    public static String format(Collection collection, String delimiter) {
        if (!CollectionUtils.isEmpty(collection)) {
            StringBuffer sb = new StringBuffer();
            for (Object o : collection) {
                sb.append(o).append(delimiter);
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        return "";
    }

    public static void main(String[] args) {
        List<Integer> list = ShardingDatabaseAndTableConfiguration.genOrderTableSuffix();
        System.out.println(list.size());
        String s = format(list, ",");
        System.out.println(s);
    }
}
