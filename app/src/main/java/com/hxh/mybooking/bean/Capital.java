package com.hxh.mybooking.bean;

import android.support.annotation.NonNull;

import com.hxh.mybooking.util.DateUtil;
import com.hxh.mybooking.util.ExcelUtil;
import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.NotNull;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HXH at 2019/7/30
 * 余额
 */
@Table("capital")
public class Capital implements ExcelUtil.Obj2StrListListener {
    public static final String MILL = "mill";

    // 指定自增，每个对象需要有一个主键
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private int id;

    // 时间
    @Column(MILL)
    @NotNull
    private long mill;

    // 金额
    @NotNull
    private long money;

    public long getMill() {
        return mill;
    }

    public void setMill(long mill) {
        this.mill = mill;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    @NonNull
    @Override
    public List<String> convert() {
        List<String> list = new ArrayList<>();
        list.add(money + "");
        list.add(DateUtil.timeStamp2Date(mill, DateUtil.YYYY_MM_DD_HH_MM));
        return list;
    }
}
