package com.hxh.mybooking.util;

import android.app.Activity;
import android.net.Uri;

import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;

/**
 * Created by HXH at 2019/7/30
 * 分享
 */
public class ShareUtil {

    //分享文本
    public static void shareTxt(Activity activity, String title, String content) {
        new Share2.Builder(activity)
                .setContentType(ShareContentType.TEXT)
                // 设置要分享的文本内容
                .setTitle(title)
                .setTextContent(content)
                .build()
                .shareBySystem();
    }

    //分享图片
    public static void shareImg(Activity activity, String title, Uri uri) {
        new Share2.Builder(activity)
                .setContentType(ShareContentType.IMAGE)
                .setShareFileUri(uri)
                .setTitle(title)
                //分享图片到指定界面，比如分享到微信朋友圈
                //.setShareToComponent("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI")
                .build()
                .shareBySystem();
    }

    //分享文件
    public static void shareFile(Activity activity, String title, Uri uri) {
        new Share2.Builder(activity)
                .setContentType(ShareContentType.FILE)
                .setShareFileUri(uri)
                .setTitle(title)
                .build()
                .shareBySystem();
    }
}
