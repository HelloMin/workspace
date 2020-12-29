package com.example.restservice.Controller;

import com.example.restservice.Entity.TraceInfo;
import com.example.restservice.Service.TraceAnnotationService;
import com.example.restservice.jaeger.JaegerTracing;
import com.example.restservice.jaeger.Traced;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class TraceAnnotationController {

	private static final String template = "Time: %s!";
	private final AtomicLong counter = new AtomicLong();
	@Autowired
	private TraceAnnotationService traceAnnotationService;

	@GetMapping("/traceAnno")
	public TraceInfo traceLooper(@RequestParam(value = "loop") Integer loop, @RequestHeader Map<String, String> headers) throws IOException {
		JaegerTracing.setParentHeader(headers);
		if (loop == 0) {
			return new TraceInfo(loop, String.format(template, System.currentTimeMillis()));
		} else if (loop > 0){
			if ((loop % 2) == 0) {
				traceAnnotationService.callGreeting(loop, headers);
			}
			return traceAnnotationService.callMyself(loop - 1, headers);
		} else {
			return new TraceInfo(loop, String.format(template, "ERROR"));
		}
	}
}