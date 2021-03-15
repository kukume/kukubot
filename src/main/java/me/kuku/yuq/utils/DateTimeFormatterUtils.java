package me.kuku.yuq.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

public class DateTimeFormatterUtils {
	private final static ConcurrentHashMap<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>();

	public static String format(LocalDateTime localDateTime, String pattern){
		DateTimeFormatter dtf = creat(pattern);
		return dtf.format(localDateTime);
	}

	public static String formatNow(String pattern){
		DateTimeFormatter dtf = creat(pattern);
		return dtf.format(LocalDateTime.now());
	}

	public static String format(long time, String pattern){
		LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(time / 1000, 0, ZoneOffset.ofHours(8));
		return creat(pattern).format(localDateTime);
	}

	public static DateTimeFormatter creat(String pattern){
		DateTimeFormatter dtf = FORMATTER_CACHE.get(pattern);
		if (dtf == null){
			dtf = DateTimeFormatter.ofPattern(pattern);
			DateTimeFormatter oldFormatter = FORMATTER_CACHE.putIfAbsent(pattern, dtf);
			if (oldFormatter != null){
				dtf = oldFormatter;
			}
		}
		return dtf;
	}

}
