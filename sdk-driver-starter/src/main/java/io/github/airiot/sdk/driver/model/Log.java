/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.airiot.sdk.driver.model;

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
