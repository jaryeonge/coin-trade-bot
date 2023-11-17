package com.jr.coin.trade.bot.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ScheduleCode {

    MINUTE("minute"),
    HOUR("hour"),
    DAY("day"),
    ;

    public String name;

}
