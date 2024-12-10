package com.shop.util;

import java.security.SecureRandom;

public class CouponUtils {


    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";


    private static final int CODE_LENGTH = 16;


    public static String generateRandomCouponCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder couponCode = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            couponCode.append(CHARACTERS.charAt(index));
        }

        return couponCode.toString();
    }
}