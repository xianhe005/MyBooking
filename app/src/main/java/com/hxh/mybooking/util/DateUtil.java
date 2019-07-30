package com.hxh.mybooking.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import com.hxh.mybooking.R;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by HXH at 2019/7/30
 * 日期时间
 */
public class DateUtil {
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    private static final Calendar CALENDAR = Calendar.getInstance();

    ///////////////////////////////////////////////////////////////////////////
    // static methods
    ///////////////////////////////////////////////////////////////////////////
    public static TimePickerDialog newTimePickerDialog(Activity activity,
                                                       long current,
                                                       long mix,
                                                       long max,
                                                       Type type,
                                                       OnDateSetListener dateSetListener) {
        return new TimePickerDialog.Builder()
                .setCallBack(dateSetListener)
                .setCancelStringId("取消")
                .setSureStringId("确定")
                .setTitleStringId("选择日期")
                .setYearText("年")
                .setMonthText("月")
                .setDayText("日")
                .setHourText("时")
                .setMinuteText("分")
                .setCyclic(false)
                .setMinMillseconds(mix)
                .setMaxMillseconds(max)
                .setCurrentMillseconds(current)
                .setThemeColor(ActivityCompat.getColor(activity, R.color.colorPrimary))
                .setType(type)
                .setWheelItemTextNormalColor(ActivityCompat.getColor(activity, R.color.timetimepicker_default_text_color))
                .setWheelItemTextSelectorColor(ActivityCompat.getColor(activity, R.color.colorPrimary))
                .setWheelItemTextSize(18)
                .build();
    }

    /**
     * 时间戳转换成日期格式
     *
     * @param mills  精确到毫秒
     * @param format 如：yyyy-MM-dd HH:mm
     */
    public static String timeStamp2Date(long mills, String format) {
        if (format == null || format.isEmpty()) {
            format = YYYY_MM_DD_HH_MM;
        }
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(mills));
    }

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param dateStr 字符串日期 如“2016-02-26 12:00:00”
     * @param format  如：yyyy-MM-dd HH:mm:ss
     */
    public static long date2TimeStamp(String dateStr, String format) {
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(dateStr).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static TimePickerDialog newTimePickerDialogYYYYMMDDHHMMAround10Year(Activity activity,
                                                                               long current,
                                                                               OnDateSetListener dateSetListener) {
        long c = date2TimeStamp(timeStamp2Date(System.currentTimeMillis(), YYYY_MM_DD_HH_MM), YYYY_MM_DD_HH_MM);
        CALENDAR.setTimeInMillis(c);
        CALENDAR.add(Calendar.YEAR, -10);
        long min = CALENDAR.getTimeInMillis();
        CALENDAR.add(Calendar.YEAR, 20);
        long max = CALENDAR.getTimeInMillis();
        if (current < min) {
            min = current;
        } else if (current > max) {
            max = current;
        }
        return newTimePickerDialog(activity,
                current,
                min,
                max,
                Type.ALL,
                (timePickerView, millSeconds) -> {
                    if (dateSetListener != null) {
                        dateSetListener.onDateSet(timePickerView, millSeconds);
                    }
                });
    }
}
