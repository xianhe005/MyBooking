package com.hxh.mybooking.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hxh.mybooking.AppContext;
import com.hxh.mybooking.R;
import com.hxh.mybooking.bean.Capital;
import com.hxh.mybooking.thread.MultiThreadHelper;
import com.hxh.mybooking.util.DateUtil;
import com.hxh.mybooking.util.ExcelUtil;
import com.hxh.mybooking.util.ShareUtil;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.hxh.mybooking.util.DateUtil.YYYY_MM_DD_HH_MM;
import static com.hxh.mybooking.util.DateUtil.date2TimeStamp;
import static com.hxh.mybooking.util.DateUtil.newTimePickerDialogYYYYMMDDHHMMAround10Year;
import static com.hxh.mybooking.util.DateUtil.timeStamp2Date;

/**
 * Created by HXH at 2019/7/30
 * 余额
 */
public class CapitalActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.rcv)
    RecyclerView mRcv;
    @Bind(R.id.srl)
    SmartRefreshLayout mRefreshLayout;

    private ProgressDialog mDialog;

    private List<Capital> mList = new ArrayList<>();
    private CommonAdapter<Capital> mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capital);
        ButterKnife.bind(this);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("正在加载中...");
        mDialog.setCancelable(false);

        mToolbar.setTitle("余额流水");
        setSupportActionBar(mToolbar);
        //设置是否有NvagitionIcon（返回图标）
        //Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        // Menu点击事件监听
        mToolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.add:
                    addOrEdit(null);
                    break;
                case R.id.out:
                    out2Excel();
                    break;
                case R.id.in:
                    inFromExcel();
                    break;
                case R.id.share:
                    inShareExcel();
                    break;
            }
            return true;
        });
        mToolbar.setNavigationOnClickListener(v -> finish());

        mRcv.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CommonAdapter<Capital>(this, R.layout.item_capital, mList) {

            @Override
            protected void convert(ViewHolder holder, Capital capital, int position) {
                holder.getView(R.id.line1)
                        .setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                holder.getView(R.id.ll1)
                        .setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                holder.getView(R.id.line3)
                        .setVisibility(position == mList.size() - 1 ? View.VISIBLE : View.GONE);

                holder.setText(R.id.tv7, capital.getMoney() + "");
                holder.setText(R.id.tv8,
                        timeStamp2Date(capital.getMill(), YYYY_MM_DD_HH_MM));

                holder.getView(R.id.ll2).setOnLongClickListener(v -> {
                    List<String> list = Arrays.asList("编辑", "删除");
                    new AlertDialog.Builder(CapitalActivity.this)
                            .setTitle("提示")
                            .setAdapter(new com.zhy.adapter.abslistview.CommonAdapter<String>(CapitalActivity.this,
                                    android.R.layout.simple_list_item_1, list) {
                                @Override
                                protected void convert(com.zhy.adapter.abslistview.ViewHolder viewHolder, String item, int position) {
                                    viewHolder.setText(android.R.id.text1, item);
                                }
                            }, (dialog, which) -> {
                                if (which == 0) {
                                    addOrEdit(capital);
                                } else {
                                    new AlertDialog.Builder(CapitalActivity.this)
                                            .setTitle("提示")
                                            .setMessage("确定删除")
                                            .setPositiveButton("确定", (dialog1, which1) -> delete(capital))
                                            .setNegativeButton("取消", null)
                                            .show();
                                }
                            }).show();
                    return true;
                });
            }
        };
        mRcv.setAdapter(mAdapter);

        mRefreshLayout.setEnableRefresh(true)
                .setEnableLoadMore(false)
                .setOnRefreshListener(refreshLayout -> fresh());
        mRefreshLayout.post(() -> mRefreshLayout.autoRefresh());
    }

    @SuppressLint("ObsoleteSdkInt")
    private void inShareExcel() {
        String filePath = Environment.getExternalStorageDirectory() + "/AMyBooking/Capital";
        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(this, "请将excel文件放置到sdcard/AMyBooking/Capital/目录", Toast.LENGTH_SHORT).show();
            return;
        }
        File[] files = file.listFiles(pathname ->
                pathname.getName().toLowerCase().endsWith(".xls") || pathname.getName().toLowerCase().endsWith(".xlsx"));
        if (files == null || files.length == 0) {
            Toast.makeText(this, "sdcard/AMyBooking/Capital/目录无excel文件", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("请选择要分享的文件")
                .setAdapter(new com.zhy.adapter.abslistview.CommonAdapter<File>(this,
                                    android.R.layout.simple_list_item_1, Arrays.asList(files)) {
                                @Override
                                protected void convert(com.zhy.adapter.abslistview.ViewHolder viewHolder, File item, int position) {
                                    viewHolder.setText(android.R.id.text1, item.getName());
                                }
                            }, (dialog, which) -> {
                            //传输文件 采用流的方式
                            Uri uri;
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                                uri = Uri.fromFile(files[which]);
                            } else {
                                uri = FileProvider.getUriForFile(
                                        CapitalActivity.this,
                                        "com.hxh.mybooking.fileprovider",
                                        files[which]);
                            }
                            // TODO: 2019/7/30 分享到百度云有问题,暂不知道如何解决
                            ShareUtil.shareFile(CapitalActivity.this, "分享文件", uri);
                            /*Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            shareIntent.setType(files[which].getName().toLowerCase().endsWith(".xls") ?
                                    "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                            startActivity(shareIntent);*/
                        }
                ).show();
    }

    private void inFromExcel() {
        String filePath = Environment.getExternalStorageDirectory() + "/AMyBooking/Capital";
        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(this, "请将excel文件放置到sdcard/AMyBooking/Capital/目录", Toast.LENGTH_SHORT).show();
            return;
        }
        File[] files = file.listFiles(pathname ->
                pathname.getName().toLowerCase().endsWith(".xls") || pathname.getName().toLowerCase().endsWith(".xlsx"));
        if (files == null || files.length == 0) {
            Toast.makeText(this, "sdcard/AMyBooking/Capital/目录无excel文件", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("请选择要导入的文件")
                .setAdapter(new com.zhy.adapter.abslistview.CommonAdapter<File>(this,
                                    android.R.layout.simple_list_item_1, Arrays.asList(files)) {
                                @Override
                                protected void convert(com.zhy.adapter.abslistview.ViewHolder viewHolder, File item, int position) {
                                    viewHolder.setText(android.R.id.text1, item.getName());
                                }
                            }, (dialog, which) -> new AlertDialog.Builder(CapitalActivity.this)
                                .setTitle("提示")
                                .setMessage("确定导入" + files[which].getName())
                                .setPositiveButton("确定", (dialog1, which1) -> readFromExcel(files[which]))
                                .setNegativeButton("取消", null)
                                .show()
                ).show();
    }

    private void readFromExcel(File file) {
        mDialog.show();
        MultiThreadHelper.execute(new MultiThreadHelper.RunOrUpdateListener<Boolean>() {
            @Override
            public Boolean onRunInThread() {
                List<Capital> list = new ArrayList<>();
                try {
                    ExcelUtil.readExcel(list, file, true, (sheet, row) -> {
                        Capital capital = new Capital();
                        String c = sheet.getCell(0, row).getContents();
                        capital.setMoney(Long.parseLong(c));
                        c = sheet.getCell(1, row).getContents();
                        capital.setMill(DateUtil.date2TimeStamp(c, DateUtil.YYYY_MM_DD_HH_MM));
                        return capital;
                    });
                    AppContext.getLiteOrm().delete(Capital.class);
                    AppContext.getLiteOrm().insert(list);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            @Override
            public void onRunUiThread(Boolean obj) {
                mDialog.dismiss();
                if (!obj) {
                    Toast.makeText(CapitalActivity.this, "导入失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(CapitalActivity.this, "导入成功", Toast.LENGTH_SHORT).show();
                fresh();
            }
        });
    }

    private void out2Excel() {
        if (mList.isEmpty()) {
            Toast.makeText(this, "暂无数据", Toast.LENGTH_SHORT).show();
            return;
        }
        mDialog.show();
        MultiThreadHelper.execute(new MultiThreadHelper.RunOrUpdateListener<Boolean>() {
            @Override
            public Boolean onRunInThread() {
                String filePath = Environment.getExternalStorageDirectory() + "/AMyBooking/Capital";
                File file = new File(filePath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                String time = DateUtil.timeStamp2Date(System.currentTimeMillis(), "yyyyMMddHHmm");
                String excelFileName = "/余额-" + time + ".xls";
                String[] title = {"金额(元)", "时间"};
                filePath = filePath + excelFileName;
                ExcelUtil.initExcel(filePath, title);
                return ExcelUtil.writeObjListToExcel(mList, filePath, CapitalActivity.this);
            }

            @Override
            public void onRunUiThread(Boolean obj) {
                mDialog.dismiss();
                if (obj) {
                    Toast.makeText(CapitalActivity.this, "导出成功(在sdcard/AMyBooking/Capital/目录查看最近excel文件)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CapitalActivity.this, "导出失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void addOrEdit(@Nullable Capital capital) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.view_capital, null);
        EditText etMoney = view.findViewById(R.id.et_money);
        TextView tvTime = view.findViewById(R.id.tv_time);
        tvTime.setOnClickListener(v -> {
            String timeStr2 = tvTime.getText().toString().trim();
            long time;
            if (TextUtils.isEmpty(timeStr2)) {
                time = date2TimeStamp(timeStamp2Date(System.currentTimeMillis(), YYYY_MM_DD_HH_MM), YYYY_MM_DD_HH_MM);
            } else {
                time = date2TimeStamp(timeStr2, YYYY_MM_DD_HH_MM);
            }
            newTimePickerDialogYYYYMMDDHHMMAround10Year(this, time, (timePickerView, mills) -> {
                tvTime.setText(timeStamp2Date(mills, YYYY_MM_DD_HH_MM));
                timePickerView.dismiss();
            }).show(getSupportFragmentManager(), "time_picker_view");
        });
        String title;
        if (capital == null) {//add
            title = "添加";
        } else {//edit
            title = "编辑";
            etMoney.setText(capital.getMoney() + "");
            tvTime.setText(timeStamp2Date(capital.getMill(), YYYY_MM_DD_HH_MM));
        }
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(view)
                .setPositiveButton("确定", (dialog, which) -> {
                    setDialogCancelEnable(dialog, false);
                    String moneyStr = etMoney.getText().toString().trim();
                    if (TextUtils.isEmpty(moneyStr)) {
                        Toast.makeText(this, etMoney.getHint(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String timeStr = tvTime.getText().toString().trim();
                    if (TextUtils.isEmpty(timeStr)) {
                        Toast.makeText(this, tvTime.getHint(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Capital cp;
                    if (capital == null) {
                        cp = new Capital();
                    } else {
                        cp = capital;
                    }
                    cp.setMoney(Long.parseLong(moneyStr));
                    cp.setMill(date2TimeStamp(timeStr, YYYY_MM_DD_HH_MM));
                    long row;
                    if (capital == null) {
                        row = AppContext.getLiteOrm().insert(cp);
                        if (row == -1L) {
                            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                            fresh();
                        }
                    } else {
                        row = AppContext.getLiteOrm().update(cp);
                        if (row == -1L) {
                            Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                    setDialogCancelEnable(dialog, true);
                    dialog.dismiss();
                }).setNegativeButton("取消",
                (dialog, which) -> {
                    setDialogCancelEnable(dialog, true);
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * 反射设置dialog按钮点击是否消失
     */
    @SuppressWarnings("all")
    public static void setDialogCancelEnable(DialogInterface dialog, boolean enable) {
        Field field;
        try {
            //通过反射获取dialog中的私有属性mShowing
            field = Dialog.class.getDeclaredField("mShowing");
            field.setAccessible(true);//设置该属性可以访问
            //dialog是否可关闭
            field.set(dialog, enable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void delete(Capital capital) {
        mDialog.show();
        MultiThreadHelper.execute(new MultiThreadHelper.RunOrUpdateListener<Integer>() {
            @Override
            public Integer onRunInThread() {
                return AppContext.getLiteOrm().delete(capital);
            }

            @Override
            public void onRunUiThread(Integer obj) {
                mDialog.dismiss();
                if (obj == -1) {
                    Toast.makeText(CapitalActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                mList.remove(capital);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_capital, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            if (menu.getClass() == MenuBuilder.class) {
                try {
                    @SuppressLint("PrivateApi") Method m = menu.getClass()
                            .getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    private void fresh() {
        mDialog.show();
        MultiThreadHelper.execute(new MultiThreadHelper.RunOrUpdateListener<ArrayList<Capital>>() {
            @Override
            public ArrayList<Capital> onRunInThread() {
                QueryBuilder<Capital> builder = new QueryBuilder<>(Capital.class)
                        .appendOrderDescBy(Capital.MILL);
                return AppContext.getLiteOrm().query(builder);
            }

            @Override
            public void onRunUiThread(ArrayList<Capital> list) {
                mList.clear();
                mList.addAll(list);
                mDialog.dismiss();
                mRefreshLayout.finishRefresh();
                mAdapter.notifyDataSetChanged();
                if (mList.isEmpty()) {
                    Toast.makeText(CapitalActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
