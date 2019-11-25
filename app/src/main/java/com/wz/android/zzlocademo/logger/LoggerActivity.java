
package com.wz.android.zzlocademo.logger;;

import com.wz.android.zzlocademo.LogFragment;
import com.wz.android.zzlocademo.R;

import android.app.Activity;

/**
 * 工具类，用于将日志输出到界面
 */
public class LoggerActivity extends Activity {

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        initializeLogging();
    }

    private void initializeLogging() {
        LogFragment logFragment = (LogFragment) getFragmentManager().findFragmentById(R.id.framelog);

        LogCatWrapper logcat = new LogCatWrapper();
        logcat.setNext(logFragment.getLogView());

        LocationLog.setLogNode(logcat);
    }
}
