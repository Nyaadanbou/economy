/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class UtilString {

    public static String format(double money, boolean decimal) {
        return BigDecimal
                .valueOf(money)
                .setScale(decimal ? 2 : 0, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

}
