package com.phiau.platform.sharding;

import lombok.Data;

/**
 * @author phiau
 * @date 2023.08.15 17:29
 */
@Data
public class DataSourceConfig {

    private String name;

    private String host;

    private int port;

    private String userName;

    private String password;
}
