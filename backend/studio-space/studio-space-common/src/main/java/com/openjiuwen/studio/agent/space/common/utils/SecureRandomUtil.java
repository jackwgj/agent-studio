package com.openjiuwen.studio.agent.space.common.utils;

import lombok.extern.slf4j.Slf4j;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.AESLightEngine;
import org.bouncycastle.crypto.prng.SP800SecureRandomBuilder;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Slf4j
public class SecureRandomUtil {

    private SecureRandomUtil() {
    }

    /**
     * drbg安全随机数
     *
     * @return SecureRandom drbg安全随机数
     */
    public static SecureRandom getInstance() {
        return DrbgHolder.DRBG;
    }

    private static class DrbgHolder {
        private static final SecureRandom DRBG;

        private static final int CIPHER_LEN = 256;

        // 规范要求种子至少为384bit
        private static final int ENTROPY_BITS_REQUIRED = 384;

        static {
            SecureRandom secureRandom = null;
            try {
                secureRandom = drbg();
            } catch (NoSuchAlgorithmException e) {
                log.error("drbg init error", e);
            }
            DRBG = secureRandom;
        }

        /**
         * 获取SecureRandom
         *
         * @return SecureRandom 返回SecureRandom
         * @throws NoSuchAlgorithmException 抛出NoSuchAlgorithmException
         */
        private static SecureRandom drbg() throws NoSuchAlgorithmException {
            SecureRandom blockingRandom = SecureRandom.getInstanceStrong();
            boolean isPredictionResistant = true; // 是否是真熵源
            // NID_aes_256_ctr
            BlockCipher cipher = new AESLightEngine();
            int cipherLen = CIPHER_LEN;
            byte[] nonce = new byte[cipherLen / Byte.SIZE];
            blockingRandom.nextBytes(nonce);
            boolean isForceReseed = false; // 是否每次取完随机数都重新刷新熵源，否则根据算法不同定期刷新
            SecureRandom fastRandom = new SP800SecureRandomBuilder(blockingRandom, isPredictionResistant)
                    .setEntropyBitsRequired(ENTROPY_BITS_REQUIRED)
                    .buildCTR(cipher, cipherLen, nonce, isForceReseed);
            fastRandom.nextInt(); // seed初始化，避免阻塞
            return fastRandom;
        }
    }
}
