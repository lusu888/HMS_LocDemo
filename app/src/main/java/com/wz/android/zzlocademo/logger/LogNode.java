/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.wz.android.zzlocademo.logger;;

public interface LogNode {
    void println(int priority, String tag, String msg, Throwable tr);
}
