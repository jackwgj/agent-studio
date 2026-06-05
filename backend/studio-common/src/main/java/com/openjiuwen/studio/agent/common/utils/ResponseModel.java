/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * response class
 */
@Slf4j
public class ResponseModel {
    private ResponseModel() {
    }

    /**
     * custom response
     *
     * @param code response code
     * @param msg response msg
     * @param data response data
     * @param <T> data type
     * @return responseMode
     */
    public static <T> ResponseEntity<T> custom(String code, String msg, T data) {
        return new ResponseEntity<>(data, num2HttpStatus(code));
    }

    /**
     * success response
     *
     * @param data response data
     * @param <T> data type
     * @return response model
     */
    public static <T> ResponseEntity<T> success(T data) {
        HttpStatus httpStatus = HttpStatus.OK;
        if (ResponseUtils.getResponseCode() != null) {
            httpStatus = ResponseUtils.getResponseCode();
            ResponseUtils.clear();
        }
        return new ResponseEntity<>(data, httpStatus);
    }

    /**
     * failed response
     *
     * @param <T> data type
     * @return response model
     */
    public static <T> ResponseEntity<T> failed() {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * failed code
     *
     * @param code response code
     * @param msg response msg
     * @return response model
     */
    public static ResponseEntity<Map<String, String>> failed(String code, String msg) {
        Map<String, String> failedMsg = new HashMap<>();
        failedMsg.put("status", code);
        failedMsg.put("message", msg);
        return new ResponseEntity<>(failedMsg, num2HttpStatus(code));
    }

    /**
     * failed msg
     *
     * @param msg response msg
     * @param <T> data type
     * @return response model
     */
    public static <T> ResponseEntity<T> failMsg(String msg) {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * successDownload
     *
     * @param file File resource
     * @return ResponseEntity
     */
    public static ResponseEntity<Resource> successDownload(Resource file) {
        if (file instanceof TransferResource) {
            return transferRemoteResource((TransferResource) file);
        } else {
            return downloadLocalResource(file);
        }
    }

    /**
     * transfer remote file
     *
     * @param resource remote file
     * @return resource
     */
    private static ResponseEntity<Resource> transferRemoteResource(TransferResource resource) {
        HttpHeaders headers = new HttpHeaders();
        if (Objects.isNull(resource)) {
            return ResponseEntity.ok().headers(headers).body(null);
        }
        resource.getMode().ifPresent(mode -> 
            headers.add("Content-Disposition",
                String.format(Locale.ROOT, "%s; filename=\"%s\"", mode, resource.getFilename())));
        if (!resource.getMode().isPresent()) {
            headers.add("Content-Disposition", String.format(Locale.ROOT, "attachment; filename=\"%s\"",
                null != resource.getFilename() ? resource.getFilename() : ""));
        }

        resource.getLength().ifPresent(length -> headers.add("Content-Length", length.toString()));
        resource.getContentType().ifPresentOrElse(
            ct -> headers.add("Content-Type", ct),
            () -> headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE));
        resource.getHeaderAttrMap().ifPresent(headerAttr -> {
            headerAttr.forEach((key, value) -> headers.add(key, value));
        });

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    /**
     * download local file
     *
     * @param resource file
     * @return resource
     */
    private static ResponseEntity<Resource> downloadLocalResource(Resource resource) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition",
            String.format(Locale.ROOT, "attachment; filename=\"%s\"", null != resource ? resource.getFilename() : ""));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    /**
     * Convert status code to status information
     *
     * @param code httpStatus code
     * @return HttpStatus message
     */
    public static HttpStatus num2HttpStatus(String code) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        for (HttpStatus httpStatus : HttpStatus.values()) {
            boolean bool = Integer.parseInt(code) == httpStatus.value();
            if (bool) {
                return httpStatus;
            }
        }
        return status;
    }

    /**
     * transfer resource
     */
    public static class TransferResource extends InputStreamResource {
        /**
         * file name
         */
        private String filename;

        /**
         * content length
         */
        private Long length;

        /**
         * content type
         */
        private String contentType;

        /**
         * download mode:attachment, inline
         */
        private String mode;

        /**
         * header parameters
         */
        private Map<String, String> headerAttrMap;

        public TransferResource(InputStream inputStream) {
            super(inputStream);
        }

        public TransferResource(InputStream inputStream, String description) {
            super(inputStream, description);
        }

        @Override
        public String getFilename() {
            return this.filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public Optional<Long> getLength() {
            return Optional.ofNullable(length);
        }

        public void setLength(Long length) {
            this.length = length;
        }

        @Override
        public long contentLength() throws IOException {
            return length == null ? 0 : length.longValue();
        }

        public Optional<String> getContentType() {
            return Optional.ofNullable(contentType);
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public Optional<String> getMode() {
            return Optional.ofNullable(mode);
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public Optional<Map<String, String>> getHeaderAttrMap() {
            return Optional.ofNullable(headerAttrMap);
        }

        public void setHeaderAttrMap(Map<String, String> headerAttrMap) {
            this.headerAttrMap = headerAttrMap;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            if (!super.equals(other)) {
                return false;
            }
            TransferResource that = (TransferResource) other;
            return filename.equals(that.filename) && length.equals(that.length) && contentType.equals(that.contentType)
                && mode.equals(that.mode) && headerAttrMap.equals(that.headerAttrMap);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), filename, length, contentType, mode, headerAttrMap);
        }
    }
}