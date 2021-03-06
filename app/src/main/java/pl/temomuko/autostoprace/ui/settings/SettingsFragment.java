package pl.temomuko.autostoprace.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

import pl.temomuko.autostoprace.R;
import pl.temomuko.autostoprace.data.local.Preferences;
import pl.temomuko.autostoprace.ui.base.BaseActivity;
import pl.temomuko.autostoprace.ui.main.Shortcuts;
import pl.temomuko.autostoprace.ui.staticdata.launcher.LauncherActivity;
import pl.temomuko.autostoprace.util.DialogFactory;

/**
 * Created by Szymon Kozak on 2016-02-05.
 */
public class SettingsFragment extends PreferenceFragment implements SettingsMvpView {

    private static final String BUNDLE_IS_LOGOUT_DIALOG_SHOWN = "bundle_is_progress_logout_shown";

    @Inject Shortcuts shortcuts;
    @Inject SettingsPresenter mSettingsPresenter;

    private Preference mLogoutPreference;
    private MaterialDialog mLogoutInfoDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mLogoutPreference = findPreference(Preferences.PREF_LOGOUT);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        mSettingsPresenter.attachView(this);
        mSettingsPresenter.setupLogoutPreference();
        createLogoutInfoDialog();
        loadLoginInfoDialogState(savedInstanceState);
        setListeners();
    }

    @Override
    public void onDestroy() {
        mSettingsPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveLogoutInfoDialogState(outState);
    }

    private void createLogoutInfoDialog() {
        mLogoutInfoDialog = DialogFactory.createLogoutInfoDialog(getActivity(), mSettingsPresenter);
    }

    private void loadLoginInfoDialogState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(BUNDLE_IS_LOGOUT_DIALOG_SHOWN)) {
                mLogoutInfoDialog.show();
            }
        }
    }

    private void setListeners() {
        mLogoutPreference.setOnPreferenceClickListener(preference -> {
            mLogoutInfoDialog.show();
            return true;
        });
    }

    private void saveLogoutInfoDialogState(Bundle outState) {
        if (mLogoutInfoDialog != null && mLogoutInfoDialog.isShowing()) {
            mLogoutInfoDialog.dismiss();
            outState.putBoolean(BUNDLE_IS_LOGOUT_DIALOG_SHOWN, true);
        } else {
            outState.putBoolean(BUNDLE_IS_LOGOUT_DIALOG_SHOWN, false);
        }
    }

    /* MVP View methods */

    @Override
    public void showLogoutMessage() {
        Toast.makeText(getActivity(), R.string.msg_logout_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void startLauncherActivity() {
        Intent intent = new Intent(getActivity(), LauncherActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void setupLogoutPreferenceEnabled(boolean state) {
        mLogoutPreference.setEnabled(state);
    }

    @Override
    public void disablePostLocationShortcut() {
        shortcuts.setPostLocationShortcutEnabled(false);
    }

    @Override
    public void setupUserLogoutPreferenceSummary(String username) {
        String summary = getActivity().getString(R.string.pref_logout_summary, username);
        mLogoutPreference.setSummary(summary);
    }

    @Override
    public void setupGuestLogoutPreferenceSummary() {
        String summary = getActivity().getString(R.string.pref_logout_summary_not_logged);
        mLogoutPreference.setSummary(summary);
    }
}
