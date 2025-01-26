package com.heychat.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final String DB_FILE = "heybox_robot.db";
    private static HikariDataSource dataSource;

    public static void initialize() {
        try {
            // 检查数据库文件是否存在
            boolean isFirstRun = !new File(DB_FILE).exists();
            
            // 配置连接池
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + DB_FILE);
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            
            dataSource = new HikariDataSource(config);
            
            if (isFirstRun) {
                logger.info("首次运行，创建数据库表");
                createTables();
            }
        } catch (Exception e) {
            logger.error("初始化数据库失败", e);
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    private static void createTables() {
        String createSignInTable = 
            "CREATE TABLE IF NOT EXISTS sign_in_records (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    user_id TEXT NOT NULL," +
            "    nickname TEXT NOT NULL," +
            "    sign_in_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    continuous_days INTEGER DEFAULT 1," +
            "    total_days INTEGER DEFAULT 1," +
            "    last_sign_in_date DATE," +
            "    experience INTEGER DEFAULT 0," +
            "    level INTEGER DEFAULT 1," +
            "    UNIQUE(user_id)" +
            ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createSignInTable);
            logger.info("数据库表创建成功");
        } catch (SQLException e) {
            logger.error("创建数据库表失败", e);
            throw new RuntimeException("创建数据库表失败", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("数据源未初始化");
        }
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
} 
