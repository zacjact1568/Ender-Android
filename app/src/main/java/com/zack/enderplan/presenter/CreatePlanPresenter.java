package com.zack.enderplan.presenter;

import android.text.TextUtils;
import android.text.format.DateFormat;

import com.zack.enderplan.R;
import com.zack.enderplan.event.PlanCreatedEvent;
import com.zack.enderplan.view.adapter.SimpleTypeAdapter;
import com.zack.enderplan.model.bean.Plan;
import com.zack.enderplan.model.DataManager;
import com.zack.enderplan.common.Constant;
import com.zack.enderplan.common.Util;
import com.zack.enderplan.view.contract.CreatePlanViewContract;

import org.greenrobot.eventbus.EventBus;

public class CreatePlanPresenter extends BasePresenter<CreatePlanViewContract> {

    private CreatePlanViewContract mCreatePlanViewContract;
    private DataManager mDataManager;
    private Plan mPlan;

    public CreatePlanPresenter(CreatePlanViewContract createPlanViewContract) {
        attachView(createPlanViewContract);
        mDataManager = DataManager.getInstance();
        mPlan = new Plan(Util.makeCode());
    }

    @Override
    public void attachView(CreatePlanViewContract viewContract) {
        mCreatePlanViewContract = viewContract;
    }

    @Override
    public void detachView() {
        mCreatePlanViewContract = null;
    }

    public void setInitialView() {
        mCreatePlanViewContract.showInitialView(new SimpleTypeAdapter(mDataManager.getTypeList(), SimpleTypeAdapter.STYLE_SPINNER));
    }

    public void notifyContentChanged(String content) {
        mPlan.setContent(content);
        mCreatePlanViewContract.onContentChanged(!TextUtils.isEmpty(content));
    }

    public void notifyTypeCodeChanged(int spinnerPos) {
        mPlan.setTypeCode(mDataManager.getType(spinnerPos).getTypeCode());
    }

    public void notifyDeadlineChanged(long deadline) {
        if (mPlan.getDeadline() == deadline) return;
        mPlan.setDeadline(deadline);
        mCreatePlanViewContract.onDeadlineChanged(mPlan.hasDeadline(), formatDateTime(deadline));
    }

    public void notifyReminderTimeChanged(long reminderTime) {
        if (mPlan.getReminderTime() == reminderTime) return;
        mPlan.setReminderTime(reminderTime);
        mCreatePlanViewContract.onReminderTimeChanged(mPlan.hasReminder(), formatDateTime(reminderTime));
    }

    public void notifyStarStatusChanged() {
        mPlan.invertStarStatus();
        mCreatePlanViewContract.onStarStatusChanged(mPlan.isStarred());
    }

    //TODO 以后都用这种形式，即notifySetting***，更换控件就不用改方法名了
    public void notifySettingDeadline() {
        mCreatePlanViewContract.showDeadlinePickerDialog(mPlan.getDeadline());
    }

    public void notifySettingReminder() {
        mCreatePlanViewContract.showReminderTimePickerDialog(mPlan.getReminderTime());
    }

    public void notifyCreatingPlan() {
        if (TextUtils.isEmpty(mPlan.getContent())) {
            mCreatePlanViewContract.onDetectedEmptyContent();
        } else {
            mPlan.setCreationTime(System.currentTimeMillis());
            mDataManager.notifyPlanCreated(mPlan);
            EventBus.getDefault().post(new PlanCreatedEvent(
                    getPresenterName(),
                    mPlan.getPlanCode(),
                    mDataManager.getRecentlyCreatedPlanLocation()
            ));
            mCreatePlanViewContract.exit();
        }
    }

    public void notifyPlanCreationCanceled() {
        //TODO 判断是否已编辑过
        mCreatePlanViewContract.exit();
    }

    private String formatDateTime(long timeInMillis) {
        if (timeInMillis == Constant.TIME_UNDEFINED) {
            return Util.getString(R.string.dscpt_click_to_set);
        } else {
            return DateFormat.format(Util.getString(R.string.date_time_format), timeInMillis).toString();
        }
    }
}