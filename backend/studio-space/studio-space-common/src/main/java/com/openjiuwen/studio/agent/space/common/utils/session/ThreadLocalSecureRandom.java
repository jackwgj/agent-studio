package com.openjiuwen.studio.agent.space.common.utils.session;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.prng.SP800SecureRandomBuilder;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class ThreadLocalSecureRandom {
    private static final ThreadLocal<SecureRandom> secureRandoms = ThreadLocal.withInitial(ThreadLocalSecureRandom::create);

    private ThreadLocalSecureRandom() {
    }

    public static SecureRandom getInstance() {
        return secureRandoms.get();
    }

    private static SecureRandom create() {
        try {
            SecureRandom source = SecureRandom.getInstanceStrong();
            boolean predictionResistant = true;
            BlockCipher cipher = new AESEngine();
            int cipherLen = 256;
            int entropyBitesRequired = 384;
            byte[] nonce = new byte[cipherLen / 8];
            source.nextBytes(nonce);
            boolean reSeed = false;
            SecureRandom random = (new SP800SecureRandomBuilder(source, predictionResistant)).setEntropyBitsRequired(entropyBitesRequired).buildCTR(cipher, cipherLen, nonce, reSeed);
            random.nextInt();
            return random;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("cannot get strong random instance", e);
        }
    }
}
