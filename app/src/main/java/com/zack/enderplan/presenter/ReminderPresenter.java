package com.zack.enderplan.presenter;

import com.zack.enderplan.R;
import com.zack.enderplan.util.ResourceUtil;
import com.zack.enderplan.util.TimeUtil;
import com.zack.enderplan.model.bean.Plan;
import com.zack.enderplan.event.PlanDetailChangedEvent;
import com.zack.enderplan.model.DataManager;
import com.zack.enderplan.view.contract.ReminderViewContract;
import com.zack.enderplan.util.Constant;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import javax.inject.Inject;

public class ReminderPresenter extends BasePresenter {

    private ReminderViewContract mReminderViewContract;
    private int mPlanListPosition;
    private Plan mPlan;
    private DataManager mDataManager;
    private int mReminderCoordinateY;

    @Inject
    public ReminderPresenter(ReminderViewContract reminderViewContract, int planListPosition, DataManager dataManager) {
        mReminderViewContract = reminderViewContract;
        mPlanListPosition = planListPosition;
        mDataManager = dataManager;

        mPlan = mDataManager.getPlan(mPlanListPosition);
    }

    @Override
    public void attach() {
        mReminderViewContract.showInitialView(
                mPlan.getContent(),
                mPlan.hasDeadline(),
                mPlan.hasDeadline() ? TimeUtil.formatTime(mPlan.getDeadline()) : null
        );
    }

    @Override
    public void detach() {
        mReminderViewContract = null;
    }

    public void notifyPreDrawingReminder(int reminderCoordinateY) {
        mReminderCoordinateY = reminderCoordinateY;
        mReminderViewContract.playEnterAnimation();
    }

    public void notifyTouchFinished(float coordinateY) {
        if (coordinateY < mReminderCoordinateY) {
            mReminderViewContract.exit();
        }
    }

    public void notifyDelayingReminder(String delay) {
        Calendar calendar = Calendar.getInstance();
        String delayDscpt;
        switch (delay) {
            case Constant.ONE_HOUR:
                calendar.add(Calendar.HOUR, 1);
                delayDscpt = ResourceUtil.getString(R.string.toast_delay_1_hour);
                break;
            case Constant.TOMORROW:
                calendar.add(Calendar.DATE, 1);
                delayDscpt = ResourceUtil.getString(R.string.toast_delay_tomorrow);
                break;
            default:
                throw new IllegalArgumentException("The argument delayTime cannot be " + delay);
        }
        updateReminderTime(calendar.getTimeInMillis(), String.format(ResourceUtil.getString(R.string.toast_reminder_delayed_format), delayDscpt));
    }

    public void notifyUpdatingReminderTime(long reminderTime) {
        if (reminderTime == Constant.UNDEFINED_TIME) return;
        if (TimeUtil.isValidDateTimePickerTime(reminderTime)) {
            updateReminderTime(
                    reminderTime,
                    String.format(ResourceUtil.getString(R.string.toast_reminder_delayed_format), String.format(ResourceUtil.getString(R.string.toast_delay_more), TimeUtil.formatTime(reminderTime)))
            );
        } else {
            mReminderViewContract.showToast(R.string.toast_past_reminder_time);
        }
    }

    private void updateReminderTime(long reminderTime, String toastMsg) {
        mDataManager.notifyReminderTimeChanged(mPlanListPosition, reminderTime);
        EventBus.getDefault().post(new PlanDetailChangedEvent(
                getPresenterName(),
                mPlan.getPlanCode(),
                mPlanListPosition,
                PlanDetailChangedEvent.FIELD_REMINDER_TIME
        ));
        mReminderViewContract.showToast(toastMsg);
        mReminderViewContract.exit();
    }

    public void notifyPlanCompleted() {
        //不需要检测是否有reminder，因为这里一定是没有reminder的
        mDataManager.notifyPlanStatusChanged(mPlanListPosition);
        mPlanListPosition = mPlan.isCompleted() ? mDataManager.getUcPlanCount() : 0;
        EventBus.getDefault().post(new PlanDetailChangedEvent(
                getPresenterName(),
                mPlan.getPlanCode(),
                mPlanListPosition,
                PlanDetailChangedEvent.FIELD_PLAN_STATUS
        ));
        mReminderViewContract.showToast(R.string.toast_plan_completed);
        mReminderViewContract.exit();
    }

    public void notifyEnteringPlanDetail() {
        mReminderViewContract.enterPlanDetail(mPlanListPosition);
        mReminderViewContract.exit();
    }
}
