package pl.temomuko.autostoprace.ui.main;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;

import java.util.List;

import pl.temomuko.autostoprace.data.model.LocationRecord;
import pl.temomuko.autostoprace.ui.base.drawer.DrawerMvpView;

/**
 * Created by szymen on 2016-01-09.
 */
public interface MainMvpView extends DrawerMvpView {

    void updateLocationRecordsList(List<LocationRecord> locationRecords);

    void showEmptyInfo();

    void startLauncherActivity();

    void startPostActivity();

    void startLoginActivity();

    void showError(String message);

    void setProgress(boolean state);

    void showSessionExpiredError();

    void compatRequestFineLocationPermission();

    void showNoFineLocationPermissionWarning();

    void dismissWarningSnackbar();

    void startLocationSettingsStatusResolution(Status status);

    void showLocationSettingsWarning();

    void startConnectionResultResolution(ConnectionResult connectionResult);
}
