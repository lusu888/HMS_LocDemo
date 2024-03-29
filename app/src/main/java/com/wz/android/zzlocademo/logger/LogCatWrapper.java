/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2016. All rights reserved.
 * See LICENSE.txt for this sample's licensing information.
 */

package com.wz.android.zzlocademo.logger;

/**
 * 工具类，用于将日志输出到界面
 */
public class LogCatWrapper implements LogNode {

    private LogNode mNext;

    public LogNode getNext() {
        return mNext;
    }

    public void setNext(LogNode node) {
        mNext = node;
    }

    @Override
    public void println(int priority, String tag, String msg, Throwable tr) {
        String useMsg = msg;
        if (useMsg == null) {
            useMsg = "";
        }

        if (tr != null) {
            useMsg += "\n" + android.util.Log.getStackTraceString(tr);
        }

        android.util.Log.println(priority, tag, useMsg);

        if (mNext != null) {
            mNext.println(priority, tag, msg, tr);
        }
    }

}
