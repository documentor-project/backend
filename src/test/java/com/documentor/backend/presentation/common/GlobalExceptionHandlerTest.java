package com.documentor.backend.presentation.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.documentor.backend.domain.common.BusinessException;
import com.documentor.backend.domain.common.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void businessExceptionReturnsDefinedResponseAndWritesWarnLog(CapturedOutput output) {
        BusinessException exception = new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "사용자를 찾을 수 없습니다."
        );

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(ErrorResponse.of(
                ErrorCode.RESOURCE_NOT_FOUND.name(),
                "사용자를 찾을 수 없습니다.",
                null
        ));
        assertThat(output)
                .contains("WARN")
                .contains("BusinessException [RESOURCE_NOT_FOUND]: 사용자를 찾을 수 없습니다.");
    }

    @Test
    void unhandledExceptionReturnsSafeResponseAndWritesStackTrace(CapturedOutput output) {
        NullPointerException exception = new NullPointerException("외부에 노출하면 안 되는 내부 오류");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUnhandledException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo(ErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                "서버 내부 오류가 발생했습니다.",
                null
        ));
        assertThat(response.getBody().message()).doesNotContain(exception.getMessage());
        assertThat(output)
                .contains("ERROR")
                .contains("Unhandled Exception Occurred:")
                .contains("java.lang.NullPointerException: 외부에 노출하면 안 되는 내부 오류");
    }
}
