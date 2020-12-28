package com.example.restservice.Controller;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.example.restservice.Entity.Greeting;
import com.example.restservice.Entity.TraceInfo;
import io.opentracing.Span;
import io.opentracing.Tracer;
import jaeger.JaegerTracing;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

	private static final String template = "Time: %s!";
	private final AtomicLong counter = new AtomicLong();

	@GetMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}
	@GetMapping("/traceLooper")
	public TraceInfo traceLooper(@RequestParam(value = "loop") Integer loop, @RequestHeader Map<String, String> headers) throws IOException {
		if (loop == 0) {
			return new TraceInfo(loop, String.format(template, System.currentTimeMillis()));
		} else if (loop > 0){
			if ((loop % 2) == 0) {
				callGreeting(loop, headers);
			}
			return callMyself(loop - 1, headers);
		} else {
			return new TraceInfo(loop, String.format(template, "ERROR"));
		}
	}
	private TraceInfo callGreeting(Integer loop, Map<String, String> headers) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		try {
			String url = "http://localhost:8081/greeting";
			HttpGet request = new HttpGet(url);
			// get parent span
			Tracer tracer = JaegerTracing.getTracer();
			String spanname = "greeting";
			Span span = JaegerTracing.extractTraceInfo(headers, JaegerTracing.getTracer(), spanname);
			tracer.scopeManager().activate(span);
			tracer.activeSpan().setTag("methodName", "trace-loop-handler");
			// inject current span
			JaegerTracing.attachTraceInfo(request, JaegerTracing.getTracer(), span);
			// send request
			CloseableHttpResponse response = httpClient.execute(request);
			span.finish();

			try {
				// Get HttpResponse Status
				System.out.println(response.getStatusLine().toString());        // HTTP/1.1 200 OK

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					// return it as a String
					String result = EntityUtils.toString(entity);
					System.out.println(result);
				}
				return new TraceInfo(loop, url);
			} finally {
				response.close();
			}
		} finally {
			httpClient.close();
		}
	}
	private TraceInfo callMyself(Integer loop, Map<String, String> headers) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		try {
			String url = "http://localhost:8081/traceLooper?loop="+loop;
			HttpGet request = new HttpGet(url);
			// get parent span
			Tracer tracer = JaegerTracing.getTracer();
			String spanname = "hellomin-spanname";
			Span span = JaegerTracing.extractTraceInfo(headers, JaegerTracing.getTracer(), spanname);
			tracer.scopeManager().activate(span);
			tracer.activeSpan().setTag("methodName", "trace-loop-handler");
			// inject current span
			JaegerTracing.attachTraceInfo(request, JaegerTracing.getTracer(), span);
			// send request
			CloseableHttpResponse response = httpClient.execute(request);

			span.finish();

			try {
				// Get HttpResponse Status
				System.out.println(response.getStatusLine().toString());        // HTTP/1.1 200 OK

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					// return it as a String
					String result = EntityUtils.toString(entity);
					System.out.println(result);
				}
				return new TraceInfo(loop, url);
			} finally {
				response.close();
			}
		} finally {
			httpClient.close();
		}
	}
}