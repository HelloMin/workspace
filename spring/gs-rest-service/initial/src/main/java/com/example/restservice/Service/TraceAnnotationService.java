package com.example.restservice.Service;

import com.example.restservice.Entity.TraceInfo;
import com.example.restservice.jaeger.JaegerTracing;
import com.example.restservice.jaeger.Traced;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TraceAnnotationService {

	private static final String template = "Time: %s!";
	private final AtomicLong counter = new AtomicLong();

	@Traced
	public TraceInfo callGreeting(Integer loop, Map<String, String> headers) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		try {
			String url = "http://localhost:8081/greeting";
			HttpGet request = new HttpGet(url);

			// inject current span
			JaegerTracing.attachTraceInfo(request);
			// send request
			CloseableHttpResponse response = httpClient.execute(request);

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
	@Traced
	public TraceInfo callMyself(Integer loop, Map<String, String> headers) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		try {
			String url = "http://localhost:8081/traceLooper?loop="+loop;
			HttpGet request = new HttpGet(url);
			// inject current span
			JaegerTracing.attachTraceInfo(request);
			// send request
			CloseableHttpResponse response = httpClient.execute(request);

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