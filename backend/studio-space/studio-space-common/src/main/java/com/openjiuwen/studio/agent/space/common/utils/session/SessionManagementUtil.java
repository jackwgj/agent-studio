package com.openjiuwen.studio.agent.space.common.utils.session;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public class SessionManagementUtil {
    private static final int SESSION_MIN_BYTE_SIZE = 24;
    private static final int TOKEN_MIN_BYTE_SIZE = 24;
    private static final int DEFAULT_BYTE_SIZE = 24;

    public SessionManagementUtil() {
    }

    public static byte[] generateSessionId() throws GeneralSecurityException {
        return generateSecureRandomBytes(24);
    }

    public static String genSessionIdToHex() throws GeneralSecurityException {
        return toHex(generateSessionId());
    }

    /** @deprecated */
    @Deprecated
    public static byte[] generateSecureRandomBytes(int byteSize) throws GeneralSecurityException {
        SecureRandom random = genSecRandom();
        byte[] bytes = new byte[byteSize];
        random.nextBytes(bytes);
        return bytes;
    }

    public static byte[] genSRBytesAndCheckSize(int byteSize) throws GeneralSecurityException {
        if (byteSize >= 0 && byteSize <= 32768) {
            SecureRandom random = genSecRandom();
            byte[] bytes = new byte[byteSize];
            random.nextBytes(bytes);
            return bytes;
        } else {
            throw new GeneralSecurityException("byteSize must be more than 0 and less than 1048576");
        }
    }

    public static SecureRandom genSecRandom() throws GeneralSecurityException {
        return ThreadLocalSecureRandom.getInstance();
    }

    public static String genSecureRandomBytesToHex(int byteArraylenth) throws GeneralSecurityException {
        return toHex(generateSecureRandomBytes(byteArraylenth));
    }

    public static byte[] generateToken() throws GeneralSecurityException {
        return generateSecureRandomBytes(24);
    }

    public static String genTokenToHex() throws GeneralSecurityException {
        return toHex(generateToken());
    }

    public static String toHex(byte[] byteArr) {
        StringBuilder builder = new StringBuilder();
        if (byteArr == null) {
            return null;
        } else {
            for(int i = 0; i < byteArr.length; ++i) {
                String hv = Integer.toHexString(byteArr[i] & 255);
                if (hv.length() < 2) {
                    builder.append(0);
                }

                builder.append(hv);
            }

            return builder.toString();
        }
    }
}
