package com.hxh.mybooking.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hxh.mybooking.AppContext;
import com.hxh.mybooking.R;
import com.hxh.mybooking.bean.Bill;
import com.hxh.mybooking.thread.MultiThreadHelper;
import com.hxh.mybooking.util.DateUtil;
import com.hxh.mybooking.util.ExcelUtil;
import com.hxh.mybooking.util.ShareUtil;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 100;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.rcv)
    RecyclerView mRcv;
    @Bind(R.id.srl)
    SmartRefreshLayout mRefreshLayout;
    @Bind(R.id.tv_summary)
    TextView mTvSummary;

    private ProgressDialog mDialog;

    private List<Bill> mList = new ArrayList<>();
    private CommonAdapter<Bill> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("正在加载中...");
        mDialog.setCancelable(false);

        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);
        //设置是否有NvagitionIcon（返回图标）
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Menu点击事件监听
        mToolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.add:
                    startActivityForResult(new Intent(this, AddOrEditActivity.class), REQUEST_CODE);
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
                case R.id.capital:
                    startActivity(new Intent(MainActivity.this, CapitalActivity.class));
                    break;
            }
            return true;
        });

        mRcv.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CommonAdapter<Bill>(this, R.layout.item_main, mList) {

            @Override
            protected void convert(ViewHolder holder, Bill bill, int position) {
                holder.getView(R.id.line1)
                        .setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                holder.getView(R.id.ll1)
                        .setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                holder.getView(R.id.line3)
                        .setVisibility(position == mList.size() - 1 ? View.VISIBLE : View.GONE);

                holder.setText(R.id.tv5, bill.isIncome() ? "收入" : "支出");
                holder.setText(R.id.tv6, bill.getUse());
                holder.setText(R.id.tv7, bill.getMoney() + "");
                holder.setText(R.id.tv8,
                        DateUtil.timeStamp2Date(bill.getMill(), DateUtil.YYYY_MM_DD_HH_MM));

                holder.getView(R.id.ll2).setOnLongClickListener(v -> {
                    List<String> list = Arrays.asList("编辑", "删除");
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("提示")
                            .setAdapter(new com.zhy.adapter.abslistview.CommonAdapter<String>(MainActivity.this,
                                    android.R.layout.simple_list_item_1, list) {
                                @Override
                                protected void convert(com.zhy.adapter.abslistview.ViewHolder viewHolder, String item, int position) {
                                    viewHolder.setText(android.R.id.text1, item);
                                }
                            }, (dialog, which) -> {
                                if (which == 0) {
                                    startActivityForResult(new Intent(MainActivity.this, AddOrEditActivity.class)
                                            .putExtra(AddOrEditActivity.KEY_BILL, bill), REQUEST_CODE);
                                } else {
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("提示")
                                            .setMessage("确定删除")
                                            .setPositiveButton("确定", (dialog1, which1) -> delete(bill))
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private void delete(Bill bill) {
        mDialog.show();
        MultiThreadHelper.execute(new MultiThreadHelper.RunOrUpdateListener<Integer>() {
            @Override
            public Integer onRunInThread() {
                return AppContext.getLiteOrm().delete(bill);
            }

            @Override
            public void onRunUiThread(Integer obj) {
                mDialog.dismiss();
                if (obj == -1) {
                    Toast.makeText(MainActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                mList.remove(bill);
                mAdapter.notifyDataSetChanged();
                summery();
            }
        });
    }

    private void summery() {
        long allIncome = mList.stream()
                .filter(Bill::isIncome)
                .mapToLong(Bill::getMoney)
                .sum();
        long allOutcome = mList.stream()
                .filter(bill -> !bill.isIncome())
                .mapToLong(Bill::getMoney)
                .sum();
        Spanned spanned = Html.fromHtml("<font color='#2196F3'>总收入:"
                + allIncome
                + "元</font> <font color='#009688'>总支出:"
                + allOutcome
                + "元</font> <br/><font color='#FF5722'>总收入-总支出:"
                + (allIncome - allOutcome) + "元", Html.FROM_HTML_MODE_LEGACY);
        mTvSummary.setText(spanned);
    }

    private void fresh() {
        mDialog.show();
        MultiThreadHelper.execute(new MultiThreadHelper.RunOrUpdateListener<ArrayList<Bill>>() {
            @Override
            public ArrayList<Bill> onRunInThread() {
                QueryBuilder<Bill> builder = new QueryBuilder<>(Bill.class)
                        .appendOrderDescBy(Bill.MILL);
                return AppContext.getLiteOrm().query(builder);
            }

            @Override
            public void onRunUiThread(ArrayList<Bill> list) {
                mList.clear();
                mList.addAll(list);
                mDialog.dismiss();
                mRefreshLayout.finishRefresh();
                mAdapter.notifyDataSetChanged();
                summery();
                if (mList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
        super.onDestroy();
    }

    @SuppressWarnings("all")
    private void out2Excel() {
        if (mList.isEmpty()) {
            Toast.makeText(this, "暂无数据", Toast.LENGTH_SHORT).show();
            return;
        }
        mDialog.show();
        MultiThreadHelper.execute(new MultiThreadHelper.RunOrUpdateListener<Boolean>() {
            @Override
            public Boolean onRunInThread() {
                String filePath = Environment.getExternalStorageDirectory() + "/AMyBooking/Bill";
                File file = new File(filePath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                String time = DateUtil.timeStamp2Date(System.currentTimeMillis(), "yyyyMMddHHmm");
                String excelFileName = "/账单-" + time + ".xls";
                String[] title = {"收/支", "用途", "金额(元)", "时间"};
                filePath = filePath + excelFileName;
                ExcelUtil.initExcel(filePath, title);
                return ExcelUtil.writeObjListToExcel(mList, filePath, MainActivity.this);
            }

            @Override
            public void onRunUiThread(Boolean obj) {
                mDialog.dismiss();
                if (obj) {
                    Toast.makeText(MainActivity.this, "导出成功(在sdcard/AMyBooking/Bill/目录查看最近excel文件)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "导出失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("ObsoleteSdkInt")
    private void inShareExcel() {
        String filePath = Environment.getExternalStorageDirectory() + "/AMyBooking/Bill";
        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(this, "请将excel文件放置到sdcard/AMyBooking/Bill/目录", Toast.LENGTH_SHORT).show();
            return;
        }
        File[] files = file.listFiles(pathname ->
                pathname.getName().toLowerCase().endsWith(".xls") || pathname.getName().toLowerCase().endsWith(".xlsx"));
        if (files == null || files.length == 0) {
            Toast.makeText(this, "sdcard/AMyBooking/Bill/目录无excel文件", Toast.LENGTH_SHORT).show();
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
                                        MainActivity.this,
                                        "com.hxh.mybooking.fileprovider",
                                        files[which]);
                            }
                            // TODO: 2019/7/30 分享到百度云有问题,暂不知道如何解决
                            ShareUtil.shareFile(MainActivity.this, "分享文件", uri);
                            /*Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            shareIntent.setType("file/*");
                            shareIntent.setType(files[which].getName().toLowerCase().endsWith(".xls") ?
                                    "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                            startActivity(shareIntent);*/
                        }
                ).show();
    }

    private void inFromExcel() {
        String filePath = Environment.getExternalStorageDirectory() + "/AMyBooking/Bill";
        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(this, "请将excel文件放置到sdcard/AMyBooking/Bill/目录", Toast.LENGTH_SHORT).show();
            return;
        }
        File[] files = file.listFiles(pathname ->
                pathname.getName().toLowerCase().endsWith(".xls") || pathname.getName().toLowerCase().endsWith(".xlsx"));
        if (files == null || files.length == 0) {
            Toast.makeText(this, "sdcard/AMyBooking/Bill/目录无excel文件", Toast.LENGTH_SHORT).show();
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
                            }, (dialog, which) -> new AlertDialog.Builder(MainActivity.this)
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
                List<Bill> list = new ArrayList<>();
                try {
                    ExcelUtil.readExcel(list, file, true, (sheet, row) -> {
                        Bill bill = new Bill();
                        String c = sheet.getCell(0, row).getContents();
                        bill.setIncome("收入".equals(c));
                        c = sheet.getCell(1, row).getContents();
                        bill.setUse(c);
                        c = sheet.getCell(2, row).getContents();
                        bill.setMoney(Long.parseLong(c));
                        c = sheet.getCell(3, row).getContents();
                        bill.setMill(DateUtil.date2TimeStamp(c, DateUtil.YYYY_MM_DD_HH_MM));
                        return bill;
                    });
                    AppContext.getLiteOrm().delete(Bill.class);
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
                    Toast.makeText(MainActivity.this, "导入失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, "导入成功", Toast.LENGTH_SHORT).show();
                fresh();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                fresh();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
