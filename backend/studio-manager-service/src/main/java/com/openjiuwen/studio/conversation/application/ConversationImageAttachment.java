/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.conversation.application;

import com.openjiuwen.studio.conversation.application.dto.ConversationImageCandidate;

import java.util.ArrayList;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.conversation.domain.model.ConversationMessage;
import com.openjiuwen.studio.conversation.domain.model.valueobject.FileRef;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

/** Validate image attachments independently of browser metadata. */
final class ConversationImageAttachment {
    static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final int MAX_CURRENT_IMAGES = 10; // Match the workbench attachment picker.
    private static final int MAX_HISTORY_IMAGES = 8;
    private static final int MAX_IMAGE_SIDE = 8192;
    private static final long MAX_IMAGE_PIXELS = 16_000_000L;

    private ConversationImageAttachment() {
    }

    static String mediaTypeForName(String fileName) {
        String name = fileName.toLowerCase(Locale.ROOT);
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return null;
    }

    static void checkByteLimit(long size) {
        if (size > MAX_IMAGE_BYTES) {
            throw new AgentStudioException(StudioError.PICTURE_FILE_SIZE_EXCEED_LIMIT,
                "图片文件不能超过 10 MiB");
        }
    }

    static void validate(byte[] bytes, String mediaType) {
        checkByteLimit(bytes.length);
        try (MemoryCacheImageInputStream input = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw invalidImage();
            }
            ImageReader reader = readers.next();
            try {
                if (!("image/" + reader.getFormatName().toLowerCase(Locale.ROOT)).equals(mediaType)) {
                    throw invalidImage();
                }
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0) {
                    throw invalidImage();
                }
                if (width > MAX_IMAGE_SIDE || height > MAX_IMAGE_SIDE || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new AgentStudioException(StudioError.PICTURE_FILE_SIZE_EXCEED_LIMIT,
                        "图片单边最大 8192 像素，且总像素不能超过 1600 万");
                }
                reader.addIIOReadWarningListener((ignored, warning) -> { throw invalidImage(); });
                if (reader.read(0) == null) {
                    throw invalidImage();
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException | IllegalArgumentException exception) {
            throw invalidImage();
        }
    }

    record Selection(List<ConversationImageCandidate> images, int omittedCurrent) {}

    static Selection pick(List<FileRef> current, List<ConversationMessage> history) {
        var prior = new LinkedHashMap<String, ConversationImageCandidate>();
        for (ConversationMessage message : history == null ? List.<ConversationMessage>of() : history) {
            if ("user".equals(message.getRole()) && message.getFileRefs() != null) {
                for (FileRef ref : message.getFileRefs()) {
                    if (isVerified(ref)) {
                        prior.putIfAbsent(ref.getObjectKey(), new ConversationImageCandidate(ref, false, prior.size() + 1));
                    }
                }
            }
        }
        var recent = new LinkedHashMap<String, ConversationImageCandidate>();
        int ordinal = prior.size();
        for (FileRef ref : current == null ? List.<FileRef>of() : current) {
            if (!isVerified(ref) || recent.containsKey(ref.getObjectKey())) {
                continue;
            }
            var earlier = prior.remove(ref.getObjectKey());
            recent.put(ref.getObjectKey(), new ConversationImageCandidate(ref, true,
                earlier == null ? ++ordinal : earlier.ordinal()));
        }
        var selected = new ArrayList<>(recent.values().stream().limit(MAX_CURRENT_IMAGES).toList());
        var earlier = new ArrayList<>(prior.values());
        if (earlier.size() > MAX_HISTORY_IMAGES) {
            selected.add(earlier.get(0));
            selected.addAll(earlier.subList(earlier.size() - (MAX_HISTORY_IMAGES - 1), earlier.size()));
        } else {
            selected.addAll(earlier);
        }
        return new Selection(List.copyOf(selected), Math.max(0, recent.size() - MAX_CURRENT_IMAGES));
    }

    private static boolean isVerified(FileRef ref) {
        return ref != null && ref.getFileName() != null && ref.getMediaType() != null
            && ref.getMediaType().equals(mediaTypeForName(ref.getFileName())) && ref.getSize() != null
            && ref.getSize() > 0 && ref.getSize() <= MAX_IMAGE_BYTES && ref.getChecksum() != null
            && ref.getChecksum().matches("[a-f0-9]{64}") && ref.getObjectKey() != null
            && ref.getObjectKey().startsWith("conversation-inputs/");
    }

    private static AgentStudioException invalidImage() {
        return new AgentStudioException(StudioError.ILLEGAL_FILE_TYPE, "图片内容无效或与文件后缀不符");
    }
}
