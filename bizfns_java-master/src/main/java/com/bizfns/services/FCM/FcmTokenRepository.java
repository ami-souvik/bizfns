package com.bizfns.services.FCM;

public interface FcmTokenRepository {
    void saveFcmToken(FcmToken fcmToken);
    void deleteByUserId(int userId);
    FcmToken findByUserId(int userId);
}
