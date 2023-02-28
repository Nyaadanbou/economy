/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@SuppressWarnings("unused")
public class UtilTime {

    public static final String DATE_FORMAT_NOW = "yyyy/MM/dd HH:mm:ss";
    public static final String DATE_FORMAT_DAY = "yyyy/MM/dd";

    public static String now() {
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }

    public static String when(final long time) {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(time);
    }

    public static String date() {
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DAY);
        return sdf.format(cal.getTime());
    }

    public static String clock(boolean seconds) {
        TimeZone zone = TimeZone.getDefault();
        Date date = new Date();
        SimpleDateFormat sdf;
        if (seconds) {
            sdf = new SimpleDateFormat("HH:mm:ss");
        } else {
            sdf = new SimpleDateFormat("HH:mm");
        }
        sdf.setTimeZone(zone);
        return sdf.format(date);
    }

    public static boolean elapsed(final long from, final long required) {
        return System.currentTimeMillis() - from > required;
    }

    public static String getCurrentDay() {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.CHINESE);
        Calendar calendar = Calendar.getInstance();
        return dayFormat.format(calendar.getTime());
    }

}
