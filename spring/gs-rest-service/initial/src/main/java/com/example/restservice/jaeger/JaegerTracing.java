package com.example.restservice.jaeger;

import io.jaegertracing.Configuration;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.util.GlobalTracer;
import okhttp3.Request;
import org.apache.http.client.methods.HttpGet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JaegerTracing
{
    static ThreadLocal<Map<String, String>> requestHeaderMap = new ThreadLocal<Map<String, String>> ();

    private static void initTracer() {
        Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv().withType("const").withParam(1);

        Configuration.SenderConfiguration sender = new Configuration.SenderConfiguration().withEndpoint("http://10.86.33.9:14268/api/traces");

        Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv().withLogSpans(true).withSender(sender);

        Configuration config = new Configuration("com.example.restservice.jaeger-hellomin-test").withSampler(samplerConfig).withReporter(reporterConfig);
        GlobalTracer.registerIfAbsent(config.getTracer());
    }

    public static Tracer getTracer() {
        if (!GlobalTracer.isRegistered()) {
            initTracer();

        }
        return GlobalTracer.get();
    }
    public static void attachTraceInfo(final HttpGet request, Tracer tracer, Span span) {
        tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMap() {
            @Override
            public void put(String key, String value) {
                request.setHeader(key, value);
            }
            @Override
            public Iterator<Map.Entry<String, String>> iterator() {
                throw new UnsupportedOperationException("TextMapInjectAdapter should only be used with Tracer.inject()");
            }
        });
    }
    public static void attachTraceInfo(final HttpGet request) {
        Tracer tracer = JaegerTracing.getTracer();
        Span span = tracer.scopeManager().activeSpan();
        tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMap() {
            @Override
            public void put(String key, String value) {
                request.setHeader(key, value);
            }
            @Override
            public Iterator<Map.Entry<String, String>> iterator() {
                throw new UnsupportedOperationException("TextMapInjectAdapter should only be used with Tracer.inject()");
            }
        });
    }
    public static Span extractTraceInfo(Request request, Tracer tracer, String spanname) {
        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(spanname).withTag("hellomin-tag", "test-span");
        // format the headers for extraction
        final Map<String, String> headers = new HashMap<String, String>();
        for (String key : request.headers().names()) {
            headers.put(key, request.headers().get(key));
        }
        return extractTraceInfo(headers, tracer, spanname);
    }
    public static Span extractTraceInfo(Map<String, String> headers, Tracer tracer, String spanname) {
        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(spanname).withTag("hellomin-tag", "test-span");
        try {
            SpanContext parentSpanCtx = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
            if (parentSpanCtx != null) {
                spanBuilder.asChildOf(parentSpanCtx);
            }
        } catch (Exception e) {
            spanBuilder.withTag("Error", "extract from request fail, error msg:" + e.getMessage());
        }
        return spanBuilder.start();
    }
    public static Span buildSpan(String spanName) {
        Tracer tracer = JaegerTracing.getTracer();
        Span span;
        if (requestHeaderMap != null) {
            span = JaegerTracing.extractTraceInfo(requestHeaderMap.get(), JaegerTracing.getTracer(), spanName);
        } else {
            span = tracer.buildSpan(spanName).start();
        }
        // get parent span
        tracer.scopeManager().activate(span);
        return span;
    }
    public static void setParentHeader(Map<String, String> header) {
        requestHeaderMap.set(header);
    }
    public static Map<String, String> getParentHeader() {
        return requestHeaderMap.get();
    }
}