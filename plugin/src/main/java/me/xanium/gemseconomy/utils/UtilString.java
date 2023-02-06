/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.utils;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class UtilString {

    public static String format(double money) {
        DecimalFormat format = new DecimalFormat();
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        format.setDecimalFormatSymbols(symbols);
        format.setGroupingUsed(true);
        format.setGroupingSize(3);
        double roundOff = Math.round(money * 100.0) / 100.0;
        return format.format(roundOff);
    }

    public static String colorize(String string){
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> colorize(List<String> message){
        List<String> colorizedList = Lists.newArrayList();
        for(String str : message){
            colorizedList.add(colorize(str));
        }
        return colorizedList;
    }

}
