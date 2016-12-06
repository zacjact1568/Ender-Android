package com.zack.enderplan.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zack.enderplan.model.database.DatabaseManager;
import com.zack.enderplan.utility.Util;

import java.util.Map;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Map<String, Long> reminderTimeMap = DatabaseManager.getInstance().queryReminderTimeWithEnabledReminder();
        for (Map.Entry<String, Long> entry : reminderTimeMap.entrySet()) {
            Util.setReminder(entry.getKey(), entry.getValue());
        }
    }
}
