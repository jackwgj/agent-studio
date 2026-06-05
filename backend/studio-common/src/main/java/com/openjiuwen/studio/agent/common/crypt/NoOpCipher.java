/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.crypt;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class NoOpCipher implements Cipher {
    @Override
    public byte[] encrypt(String plainText, byte[] initVector) throws AgentStudioException {
        return encrypt(plainText);
    }

    @Override
    public String decrypt(byte[] cipherText, byte[] initVector) throws AgentStudioException {
        if (cipherText == null) {
            return null;
        }

        return decrypt(cipherText);
    }

    @Override
    public byte[] genIV() throws AgentStudioException {
        return new byte[0];
    }

    @Override
    public byte[] encrypt(String plainText) throws AgentStudioException {
        if (StringUtils.isBlank(plainText)) {
            return null;
        }

        byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);

        byte[] message = new byte[plainText.getBytes(StandardCharsets.UTF_8).length + 1];
        byte[] index = ByteBuffer.allocate(1).put(index()).array();
        System.arraycopy(index, 0, message, 0, 1);
        System.arraycopy(plainTextBytes, 0, message, 1, plainTextBytes.length);

        return message;
    }

    @Override
    public String decrypt(byte[] cipherText) throws AgentStudioException {
        if (cipherText == null) {
            return null;
        }

        if (cipherText.length <= 1) {
            throw new AgentStudioException("Invalid cipher text");
        }

        final byte[] decryptData = new byte[cipherText.length - 1];
        System.arraycopy(cipherText, 1, decryptData, 0, cipherText.length - 1);

        return new String(decryptData, StandardCharsets.UTF_8);
    }

    @Override
    public byte index() {
        return (byte) 0x00;
    }

    @Override
    public String name() {
        return "NO_OP_CIPHER";
    }
}