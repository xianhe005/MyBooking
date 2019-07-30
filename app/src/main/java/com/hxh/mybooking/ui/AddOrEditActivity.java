package com.hxh.mybooking.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hxh.mybooking.AppContext;
import com.hxh.mybooking.R;
import com.hxh.mybooking.bean.Bill;
import com.hxh.mybooking.thread.MultiThreadHelper;
import com.hxh.mybooking.util.DateUtil;
import com.hxh.mybooking.util.HandlerUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by HXH at 2019/7/29
 * 添加记录
 */
public class AddOrEditActivity extends AppCompatActivity {

    public static final String KEY_BILL = "key_bill";

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.rb1)
    RadioButton mRb1;
    @Bind(R.id.rb2)
    RadioButton mRb2;
    @Bind(R.id.rg)
    RadioGroup mRg;
    @Bind(R.id.et_use)
    EditText mEtUse;
    @Bind(R.id.et_money)
    EditText mEtMoney;
    @Bind(R.id.tv_time)
    TextView mTvTime;
    @Bind(R.id.btn_save)
    TextView mBtnSave;

    private ProgressDialog mDialog;

    private Bill mBill;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_edit);
        ButterKnife.bind(this);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("加载中...");
        mDialog.setCancelable(false);

        mBill = (Bill) getIntent().getSerializableExtra(KEY_BILL);
        if (mBill == null) {
            mToolbar.setTitle("添加记录");
            mBtnSave.setText("保存记录");
        } else {
            mToolbar.setTitle("编辑记录");
            mBtnSave.setText("更新记录");
            mRg.check(mBill.isIncome() ? R.id.rb1 : R.id.rb2);
            mEtUse.setText(mBill.getUse());
            mEtMoney.setText(mBill.getMoney() + "");
            mTvTime.setText(DateUtil.timeStamp2Date(mBill.getMill(), DateUtil.YYYY_MM_DD_HH_MM));
        }
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
        super.onDestroy();
    }

    @OnClick({R.id.tv_time, R.id.btn_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_time:
                String timeStr2 = mTvTime.getText().toString().trim();
                long time;
                if (TextUtils.isEmpty(timeStr2)) {
                    time = DateUtil.date2TimeStamp(DateUtil.timeStamp2Date(System.currentTimeMillis(), DateUtil.YYYY_MM_DD_HH_MM), DateUtil.YYYY_MM_DD_HH_MM);
                } else {
                    time = DateUtil.date2TimeStamp(timeStr2, DateUtil.YYYY_MM_DD_HH_MM);
                }
                DateUtil.newTimePickerDialogYYYYMMDDHHMMAround10Year(this, time, (timePickerView, mills) -> {
                    mTvTime.setText(DateUtil.timeStamp2Date(mills, DateUtil.YYYY_MM_DD_HH_MM));
                    timePickerView.dismiss();
                }).show(getSupportFragmentManager(), "time_picker_view");
                break;
            case R.id.btn_save:
                String use = mEtUse.getText().toString().trim();
                if (TextUtils.isEmpty(use)) {
                    Toast.makeText(this, mEtUse.getHint(), Toast.LENGTH_SHORT).show();
                    return;
                }
                String moneyStr = mEtMoney.getText().toString().trim();
                if (TextUtils.isEmpty(moneyStr)) {
                    Toast.makeText(this, mEtMoney.getHint(), Toast.LENGTH_SHORT).show();
                    return;
                }
                String timeStr = mTvTime.getText().toString().trim();
                if (TextUtils.isEmpty(timeStr)) {
                    Toast.makeText(this, mTvTime.getHint(), Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean isIncome = mRg.getCheckedRadioButtonId() == R.id.rb1;

                Bill bill;
                if (mBill != null) {
                    mBill.setIncome(isIncome);
                    mBill.setUse(use);
                    mBill.setMoney(Long.parseLong(moneyStr));
                    mBill.setMill(DateUtil.date2TimeStamp(timeStr, DateUtil.YYYY_MM_DD_HH_MM));
                    bill = mBill;
                } else {
                    bill = new Bill(use, DateUtil.date2TimeStamp(timeStr, DateUtil.YYYY_MM_DD_HH_MM),
                            isIncome, Long.parseLong(moneyStr));
                }

                mDialog.show();
                MultiThreadHelper.execute(new MultiThreadHelper.RunOrUpdateListener<Long>() {
                    @Override
                    public Long onRunInThread() {
                        if (mBill == null) {
                            return AppContext.getLiteOrm().save(bill);
                        } else {
                            return (long) AppContext.getLiteOrm().update(bill);
                        }
                    }

                    @Override
                    public void onRunUiThread(Long obj) {
                        if (obj == -1) {
                            mDialog.dismiss();
                            Toast.makeText(AddOrEditActivity.this, mBill == null ? "保存失败" : "更新失败", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(AddOrEditActivity.this, mBill == null ? "保存成功" : "更新成功", Toast.LENGTH_SHORT).show();
                        HandlerUtil.runOnUiThreadDelay(() -> {
                            mDialog.dismiss();
                            setResult(RESULT_OK);
                            finish();
                        }, 1000);
                    }
                });
                break;
        }
    }
}
