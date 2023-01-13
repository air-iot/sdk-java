package cn.airiot.sdk.driver.data.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Log {

	private Instant time;
	private Object msg;
//	private Gson gson = new GsonBuilder().create();

	public static String get(String uid, Object msg) {
		return "{\"time\":\""
				+ LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
						.format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"))
				+ "\",\"message\":\"" + msg + "\",\"uid\":\"" + uid + "\"}";
	}

	public static void main(String[] args) {
//		Clock clock = Clock.systemUTC();
	}

	public Log() {
		// TODO Auto-generated constructor stub
	}

	public Log(Instant time, Object msg) {
		super();
		this.time = time;
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "Log [time=" + time + ", msg=" + msg + "]";
	}

}
