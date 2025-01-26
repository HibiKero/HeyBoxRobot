package com.heychat.model;

import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDate;

public class SignInRecord {
    private Long id;
    private String userId;
    private String nickname;
    private Timestamp signInTime;
    private int continuousDays;
    private int totalDays;
    private Date lastSignInDate;
    private boolean alreadySignedToday;

    // 构造函数
    public SignInRecord() {
        this.alreadySignedToday = false;
    }

    public SignInRecord(String userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
        this.signInTime = new Timestamp(System.currentTimeMillis());
        this.continuousDays = 1;
        this.totalDays = 1;
        this.lastSignInDate = Date.valueOf(LocalDate.now());
        this.alreadySignedToday = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Timestamp getSignInTime() {
        return signInTime;
    }

    public void setSignInTime(Timestamp signInTime) {
        this.signInTime = signInTime;
    }

    public int getContinuousDays() {
        return continuousDays;
    }

    public void setContinuousDays(int continuousDays) {
        this.continuousDays = continuousDays;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }

    public Date getLastSignInDate() {
        return lastSignInDate;
    }

    public void setLastSignInDate(Date lastSignInDate) {
        this.lastSignInDate = lastSignInDate;
    }

    public boolean isAlreadySignedToday() {
        return alreadySignedToday;
    }

    public void setAlreadySignedToday(boolean alreadySignedToday) {
        this.alreadySignedToday = alreadySignedToday;
    }

    @Override
    public String toString() {
        return "SignInRecord{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", signInTime=" + signInTime +
                ", continuousDays=" + continuousDays +
                ", totalDays=" + totalDays +
                ", lastSignInDate=" + lastSignInDate +
                ", alreadySignedToday=" + alreadySignedToday +
                '}';
    }
} 