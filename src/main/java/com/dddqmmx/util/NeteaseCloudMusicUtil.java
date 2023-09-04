package com.dddqmmx.util;

public class NeteaseCloudMusicUtil {
    public static String convertCookieString(String cookie) {
        String[] cookies = cookie.split(";");
        StringBuilder sb = new StringBuilder();
        for (String c : cookies) {
            if (c.contains("Max-Age") || c.contains("Expires") || c.contains("Path")) {
                continue;
            }
            sb.append(c.trim()).append("; ");
        }
        return sb.toString();
    }
}
