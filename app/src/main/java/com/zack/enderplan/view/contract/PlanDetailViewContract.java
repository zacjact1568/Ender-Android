package com.zack.enderplan.view.contract;

import com.zack.enderplan.model.bean.FormattedType;
import com.zack.enderplan.model.bean.FormattedPlan;

public interface PlanDetailViewContract extends BaseViewContract {

    void showInitialView(FormattedPlan formattedPlan, FormattedType formattedType);

    void onAppBarScrolled(float headerLayoutAlpha);

    void onAppBarScrolledToCriticalPoint(String toolbarTitle);

    void showPlanDeletionDialog(String content);

    void onPlanStatusChanged(boolean isCompleted);

    void showContentEditorDialog(String content);

    void onContentChanged(String newContent);

    void onStarStatusChanged(boolean isStarred);

    void onTypeOfPlanChanged(FormattedType formattedType);

    void showTypePickerDialog(int defaultTypeListPos);

    void showDeadlinePickerDialog(long defaultDeadline);

    void showReminderTimePickerDialog(long defaultReminderTime);

    void onDeadlineChanged(CharSequence newDeadline);

    void onReminderTimeChanged(CharSequence newReminderTime);

    void backToTop();

    void pressBack();
}
