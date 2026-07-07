package com.example.template.controller;

import com.example.template.aspect.Logged;
import com.example.template.dto.response.ApiResponse;
import io.opentelemetry.api.trace.Span;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.MDC;

import java.util.LinkedHashMap;
import java.util.Map;

@Path("/api/diagnostic")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Diagnostic", description = "Verifikasi status tracing OpenTelemetry")
@Logged
@Slf4j
public class DiagnosticController {

    @GET
    @Path("/trace")
    @Operation(summary = "Verifikasi Tracing", description = "Cek apakah traceId dan requestId terisi dengan benar di MDC")
    public Response getTraceInfo() {
        String traceId = Span.current().getSpanContext().getTraceId();
        String mdcTraceId = MDC.get("traceId");
        String mdcRequestId = MDC.get("requestId");

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("status", "OK");
        info.put("trace_id", traceId);
        info.put("mdc_trace_id", mdcTraceId != null ? mdcTraceId : "NO_TRACE");
        info.put("mdc_request_id", mdcRequestId != null ? mdcRequestId : "NO_REQ");
        info.put("trace_active", traceId != null && !traceId.isBlank() && !"00000000000000000000000000000000".equals(traceId));

        log.info("Diagnostic trace check: traceId={}, requestId={}", traceId, mdcRequestId);
        return Response.ok(ApiResponse.success(info)).build();
    }
}
