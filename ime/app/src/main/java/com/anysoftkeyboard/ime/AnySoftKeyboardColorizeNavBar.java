package com.anysoftkeyboard.ime;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.BoolRes;
import android.support.annotation.DimenRes;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.R;

public abstract class AnySoftKeyboardColorizeNavBar extends AnySoftKeyboardIncognito {

    private static final int NO_ID = 0;

    @DimenRes
    private int mNavigationBarHeightId = NO_ID;
    @BoolRes
    private int mNavigationBarShownId = NO_ID;
    private boolean mPrefsToShow;

    private int mNavigationBarMinHeight;

    @Override
    public void onCreate() {
        super.onCreate();
        mNavigationBarMinHeight = getResources().getDimensionPixelOffset(R.dimen.navigation_bar_min_height);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mNavigationBarHeightId =
                    getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            mNavigationBarShownId =
                    getResources().getIdentifier("config_showNavigationBar", "bool", "android");

            Logger.d(
                    TAG,
                    "Colorized nav-bar resources: navigation_bar_height %d, config_showNavigationBar %d",
                    mNavigationBarHeightId,
                    mNavigationBarShownId);

            addDisposable(
                    prefs().getBoolean(
                            R.string.settings_key_colorize_nav_bar,
                            R.bool.settings_default_colorize_nav_bar)
                            .asObservable()
                            .subscribe(
                                    val -> mPrefsToShow = val,
                                    GenericOnError.onError("settings_key_colorize_nav_bar")));
        }
    }

    private boolean doesOsShowNavigationBar() {
        if (mNavigationBarShownId != NO_ID) {
            return getResources().getBoolean(mNavigationBarShownId);
        } else {
            return false;
        }
    }

    private boolean isInPortrait() {
        return getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mNavigationBarMinHeight = getResources().getDimensionPixelOffset(R.dimen.navigation_bar_min_height);
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isInPortrait() && doesOsShowNavigationBar()) {
                final Window w = getWindow().getWindow();
                if (w != null) {
                    final int navBarHeight = getNavBarHeight();
                    if (navBarHeight > 0 && mPrefsToShow) {
                        Logger.d(TAG, "Showing Colorized nav-bar with height %d", navBarHeight);
                        w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                        w.setNavigationBarColor(Color.TRANSPARENT);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            w.setDecorFitsSystemWindows(false);
                        }
                        getInputViewContainer().setBottomPadding(Math.max(navBarHeight, mNavigationBarMinHeight));
                    } else {
                        Logger.d(
                                TAG,
                                "Showing Colorized nav-bar with height %d and prefs %s",
                                navBarHeight,
                                mPrefsToShow);
                        clearColorizedNavBar(w);
                    }
                }
            } else {
                Logger.w(
                        TAG,
                        "Will not show Colorized nav-bar since isInPortrait %s and doesOsShowNavigationBar %s",
                        isInPortrait(),
                        doesOsShowNavigationBar());
            }
        }
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        if (finishingInput) {
            final Window w = getWindow().getWindow();
            if (w != null) {
                clearColorizedNavBar(w);
            }
        }
    }

    private void clearColorizedNavBar(Window w) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            w.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                w.setDecorFitsSystemWindows(true);
            }
            getInputViewContainer().setBottomPadding(0);
        }
    }

    private int getNavBarHeight() {
        if (mNavigationBarHeightId != NO_ID) {
            return getResources().getDimensionPixelSize(mNavigationBarHeightId);
        } else {
            return 0;
        }
    }
}
