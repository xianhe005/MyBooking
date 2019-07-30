package com.hxh.mybooking.bean;

import android.support.annotation.NonNull;

import com.hxh.mybooking.util.DateUtil;
import com.hxh.mybooking.util.ExcelUtil;
import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.NotNull;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HXH at 2019/7/29
 * 账单
 */
@Table("bill")
public class Bill implements Serializable, ExcelUtil.Obj2StrListListener {

    public static final String MILL = "mill";

    // 指定自增，每个对象需要有一个主键
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private int id;

    // 用途
    // 非空字段
    @NotNull
    private String use;

    // 时间
    @NotNull
    @Column(MILL)
    private long mill;

    // 是否是收入
    @NotNull
    private boolean isIncome;

    // 金额
    @NotNull
    private long money;

    public Bill() {
    }

    public Bill(String use, long mill, boolean isIncome, long money) {
        this.use = use;
        this.mill = mill;
        this.isIncome = isIncome;
        this.money = money;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public long getMill() {
        return mill;
    }

    public void setMill(long mill) {
        this.mill = mill;
    }

    public boolean isIncome() {
        return isIncome;
    }

    public void setIncome(boolean income) {
        isIncome = income;
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
        list.add(isIncome ? "收入" : "支出");
        list.add(use);
        list.add(money + "");
        list.add(DateUtil.timeStamp2Date(mill, DateUtil.YYYY_MM_DD_HH_MM));
        return list;
    }
}
