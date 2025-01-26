package com.heychat.service;

import com.heychat.config.DatabaseConfig;
import com.heychat.model.SignInRecord;
import com.heychat.model.LevelSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class SignInService {
    private static final Logger logger = LoggerFactory.getLogger(SignInService.class);

    public SignInRecord signIn(String userId, String nickname) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // 检查用户是否已经有记录
            SignInRecord record = getSignInRecord(conn, userId);
            
            if (record == null) {
                // 新用户首次签到
                record = new SignInRecord(userId, nickname);
                insertSignInRecord(conn, record);
                logger.info("用户 {} 首次签到", nickname);
                return record;
            }
            
            // 检查是否是今天重复签到
            if (record.getLastSignInDate() != null && 
                record.getLastSignInDate().toLocalDate().equals(LocalDate.now())) {
                logger.info("用户 {} 今天已经签到过了", nickname);
                record.setAlreadySignedToday(true);  // 添加标记
                return record;
            }
            
            // 更新现有用户的签到记录
            updateSignInRecord(conn, record);
            logger.info("用户 {} 第 {} 次签到", nickname, record.getTotalDays() + 1);
            return record;
            
        } catch (SQLException e) {
            logger.error("签到过程中发生错误", e);
            throw new RuntimeException("签到失败", e);
        }
    }

    private SignInRecord getSignInRecord(Connection conn, String userId) throws SQLException {
        String sql = "SELECT * FROM sign_in_records WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                SignInRecord record = new SignInRecord();
                record.setId(rs.getLong("id"));
                record.setUserId(rs.getString("user_id"));
                record.setNickname(rs.getString("nickname"));
                record.setSignInTime(rs.getTimestamp("sign_in_time"));
                record.setContinuousDays(rs.getInt("continuous_days"));
                record.setTotalDays(rs.getInt("total_days"));
                record.setLastSignInDate(rs.getDate("last_sign_in_date"));
                return record;
            }
        }
        return null;
    }

    private void insertSignInRecord(Connection conn, SignInRecord record) throws SQLException {
        String sql = "INSERT INTO sign_in_records (user_id, nickname, sign_in_time, continuous_days, total_days, last_sign_in_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, record.getUserId());
            stmt.setString(2, record.getNickname());
            stmt.setTimestamp(3, record.getSignInTime());
            stmt.setInt(4, record.getContinuousDays());
            stmt.setInt(5, record.getTotalDays());
            stmt.setDate(6, record.getLastSignInDate());
            stmt.executeUpdate();
        }
    }

    private void updateSignInRecord(Connection conn, SignInRecord record) throws SQLException {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        LocalDate lastSignInDate = record.getLastSignInDate().toLocalDate();
        
        // 计算日期差
        long daysBetween = ChronoUnit.DAYS.between(lastSignInDate, currentDate);
        
        // 更新连续签到天数
        if (daysBetween == 1) {
            // 连续签到
            record.setContinuousDays(record.getContinuousDays() + 1);
        } else if (daysBetween > 1) {
            // 中断签到，重置连续天数
            record.setContinuousDays(1);
        }
        
        // 如果不是同一天才更新
        if (daysBetween != 0) {
            // 更新总签到天数
            record.setTotalDays(record.getTotalDays() + 1);
            
            // 计算获得的经验值
            int exp = calculateExp(record.getContinuousDays());
            int oldExp = record.getExperience();
            int newExp = oldExp + exp;
            record.setExperience(newExp);
            
            // 计算新等级
            int newLevel = LevelSystem.calculateLevel(newExp);
            if (newLevel > record.getLevel()) {
                record.setLevel(newLevel);
                logger.info("用户 {} 升级到 {} 级", record.getNickname(), newLevel);
            }
            
            // 更新签到时间和日期
            record.setSignInTime(new Timestamp(System.currentTimeMillis()));
            record.setLastSignInDate(Date.valueOf(currentDate));
            
            // 更新数据库
            String sql = "UPDATE sign_in_records SET sign_in_time = ?, continuous_days = ?, " +
                        "total_days = ?, last_sign_in_date = ?, experience = ?, level = ? " +
                        "WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setTimestamp(1, record.getSignInTime());
                stmt.setInt(2, record.getContinuousDays());
                stmt.setInt(3, record.getTotalDays());
                stmt.setDate(4, record.getLastSignInDate());
                stmt.setInt(5, record.getExperience());
                stmt.setInt(6, record.getLevel());
                stmt.setString(7, record.getUserId());
                stmt.executeUpdate();
            }
        }
    }

    public String generateSignInMessage(SignInRecord record) {
        // 如果是标记为今天已经签到
        if (record.isAlreadySignedToday()) {
            return String.format("%s 你今天已经签到过了", record.getNickname());
        }

        // 计算经验值
        int exp = calculateExp(record.getContinuousDays());
        
        // 生成运势
        double energy = generateEnergy();
        
        StringBuilder message = new StringBuilder();
        message.append(String.format("签到成功！%s\n", record.getNickname()));
        message.append(String.format("本次经验值 +%d\n", exp));
        
        // 添加等级信息
        int currentLevel = record.getLevel();
        int totalExp = record.getExperience();
        int nextLevelExp = LevelSystem.getNextLevelExp(currentLevel);
        
        if (nextLevelExp > 0) {
            message.append(String.format("当前等级: %d级 (%.1f%%)\n", 
                currentLevel,
                ((totalExp - LevelSystem.getCurrentLevelExp(currentLevel)) * 100.0) / 
                (nextLevelExp - LevelSystem.getCurrentLevelExp(currentLevel))
            ));
        } else {
            message.append(String.format("当前等级: %d级 (满级)\n", currentLevel));
        }
        
        message.append("今日运势:\n");
        message.append(String.format("元气: %.1f", energy));
        
        return message.toString();
    }

    // 计算经验值的方法
    private int calculateExp(int continuousDays) {
        LocalDate today = LocalDate.now();
        int baseExp = 0;
        
        // 每月1号固定+7
        if (today.getDayOfMonth() == 1) {
            baseExp += 7;
        }
        
        // 根据星期几计算基础经验值
        int dayOfWeek = today.getDayOfWeek().getValue(); // 1-7, 周一到周日
        if (dayOfWeek >= 6) { // 周六(6)或周日(7)
            // 2-5随机整数
            baseExp += 2 + (int)(Math.random() * 4);
        } else { // 周一到周五
            // 1-3随机整数
            baseExp += 1 + (int)(Math.random() * 3);
        }
        
        // 连续签到加成
        if (continuousDays > 1) {
            // 基础连续签到加成，上限4点
            int bonus = Math.min(continuousDays - 1, 4);
            baseExp += bonus;
            
            // 特殊天数额外奖励
            if (continuousDays == 7 || continuousDays == 30) {
                baseExp += 5;
                logger.info("达到{}天连续签到，额外奖励5点经验值", continuousDays);
            }
        }
        
        return baseExp;
    }

    // 生成运势值（0-5，0.5倍数）
    private double generateEnergy() {
        // 生成0到10的随机数，然后除以2得到0-5的0.5倍数
        return Math.round(Math.random() * 10) / 2.0;
    }
} 