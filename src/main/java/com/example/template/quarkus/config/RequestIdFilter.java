package com.example.template.quarkus.config;

import io.opentelemetry.api.trace.Span;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter untuk mengisi MDC dengan requestId dan traceId setiap HTTP request.
 *
 * Menggantikan Spring Boot's TraceIdLoggingFilter (OncePerRequestFilter).
 *
 * - requestId: dibaca dari header X-Request-Id atau di-generate UUID baru
 * - traceId: diambil dari OpenTelemetry Span yang aktif
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class RequestIdFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // requestId: ambil dari header atau generate UUID
        String requestId = requestContext.getHeaderString(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(REQUEST_ID_KEY, requestId);

        // traceId: ambil dari OTel span aktif
        try {
            String traceId = Span.current().getSpanContext().getTraceId();
            if (traceId != null && !traceId.isBlank() && !"0000000000000000".equals(traceId.replace("0", ""))) {
                MDC.put(TRACE_ID_KEY, traceId);
            } else {
                MDC.put(TRACE_ID_KEY, "NO_TRACE");
            }
        } catch (Exception e) {
            MDC.put(TRACE_ID_KEY, "NO_TRACE");
        }

        // Simpan requestId di property agar bisa diakses di response filter
        requestContext.setProperty(REQUEST_ID_KEY, requestId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // Kembalikan requestId di response header
        String requestId = (String) requestContext.getProperty(REQUEST_ID_KEY);
        if (requestId != null) {
            responseContext.getHeaders().add(REQUEST_ID_HEADER, requestId);
        }

        // Bersihkan MDC
        MDC.remove(REQUEST_ID_KEY);
        MDC.remove(TRACE_ID_KEY);
    }
}
