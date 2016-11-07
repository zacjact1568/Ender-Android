package com.zack.enderplan.domain.view;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.zack.enderplan.model.bean.FormattedType;

public interface CreateTypeView {

    void showInitialView(FormattedType formattedType);

    void onTypeMarkColorChanged(int colorInt, String colorName);

    void onTypeMarkPatternChanged(boolean hasPattern, @DrawableRes int patternResId, String patternName);

    void onTypeNameChanged(String typeName, String firstChar, boolean isValid);

    void showTypeMarkColorPickerDialog(String defaultColor);

    void showTypeMarkPatternPickerDialog(String defaultPattern);

    void playShakeAnimation(String tag);

    void showToast(@StringRes int msgResId);

    void exitCreateType();
}
