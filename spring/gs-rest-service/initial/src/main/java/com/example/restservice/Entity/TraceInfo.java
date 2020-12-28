package com.example.restservice.Entity;

public class TraceInfo {

	private final long count;
	private final String content;

	public TraceInfo(long count, String content) {
		this.count = count;
		this.content = content;
	}

	public long getCount() {
		return count;
	}

	public String getContent() {
		return content;
	}
}