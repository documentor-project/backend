package com.documentor.backend.domain.document;

import com.documentor.backend.domain.common.BusinessException;
import com.documentor.backend.domain.common.ErrorCode;

public enum DocumentFileType {
    PDF,
    MD,
    TXT;

    public static DocumentFileType fromFileName(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "파일 확장자를 확인할 수 없습니다.");
        }

        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "pdf" -> PDF;
            case "md" -> MD;
            case "txt" -> TXT;
            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST, "지원하지 않는 파일 형식입니다.");
        };
    }
}
