package com.example.demo.common;

import java.time.ZoneId;

public class Constant {

    public enum SocialLoginType {
        GOOGLE,
        KAKAO,
        NAVER
    }

    public static final long WITHDRAWAL_GRACE_PERIOD_DAYS = 7;
    public static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
}

