package com.example.training_project.advice;

import com.example.training_project.dto.error.ApiErrorResponse;
import com.example.training_project.exception.DuplicateResourceException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerUnitTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleValidationErrorsWithSortedDetails() throws Exception {
        Method method = this.getClass().getDeclaredMethod("dummyMethod", String.class);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "zField", "z message"));
        bindingResult.addError(new FieldError("request", "aField", "a message"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(method, 0),
                bindingResult
        );

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(exception, request("/api/test"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().details()).containsExactly("aField: a message", "zField: z message");
    }

    @Test
    void shouldHandleBadRequestFamily() {
        ResponseEntity<ApiErrorResponse> mismatch = handler.handleBadRequest(
                new MethodArgumentTypeMismatchException("x", String.class, "id", null, new RuntimeException("boom")),
                request("/api/wrong")
        );
        ResponseEntity<ApiErrorResponse> unreadable = handler.handleBadRequest(
                new HttpMessageNotReadableException("bad payload"),
                request("/api/wrong")
        );
        ResponseEntity<ApiErrorResponse> illegal = handler.handleBadRequest(
                new IllegalArgumentException("illegal"),
                request("/api/wrong")
        );

        assertThat(mismatch.getStatusCode().value()).isEqualTo(400);
        assertThat(unreadable.getStatusCode().value()).isEqualTo(400);
        assertThat(illegal.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void shouldHandleNotFoundConflictAndUnexpected() {
        ResponseEntity<ApiErrorResponse> notFoundA = handler.handleNotFound(
                new EntityNotFoundException("missing"), request("/api/a")
        );
        ResponseEntity<ApiErrorResponse> notFoundB = handler.handleNotFound(
                new EmptyResultDataAccessException(1), request("/api/a")
        );
        ResponseEntity<ApiErrorResponse> conflictA = handler.handleConflict(
                new DuplicateResourceException("dup"), request("/api/b")
        );
        ResponseEntity<ApiErrorResponse> conflictB = handler.handleConflict(
                new DataIntegrityViolationException("integrity"), request("/api/b")
        );
        ResponseEntity<ApiErrorResponse> unexpected = handler.handleUnexpected(
                new RuntimeException("oops"), request("/api/c")
        );

        assertThat(notFoundA.getStatusCode().value()).isEqualTo(404);
        assertThat(notFoundB.getStatusCode().value()).isEqualTo(404);
        assertThat(conflictA.getStatusCode().value()).isEqualTo(409);
        assertThat(conflictB.getStatusCode().value()).isEqualTo(409);
        assertThat(unexpected.getStatusCode().value()).isEqualTo(500);
        assertThat(unexpected.getBody()).isNotNull();
        assertThat(unexpected.getBody().message()).isEqualTo("Unexpected server error");
    }

    private HttpServletRequest request(final String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return request;
    }

    private void dummyMethod(@RequestBody final String payload) {
    }
}
