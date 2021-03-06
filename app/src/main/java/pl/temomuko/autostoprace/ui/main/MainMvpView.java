package pl.temomuko.autostoprace.ui.main;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;

import java.util.List;

import pl.temomuko.autostoprace.domain.model.LocationRecord;
import pl.temomuko.autostoprace.ui.base.drawer.DrawerMvpView;

/**
 * Created by Szymon Kozak on 2016-01-09.
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

    void dismissWarning();

    void onUserResolvableLocationSettings(Status status);

    void showLocationSettingsWarning();

    void showInadequateSettingsWarning();

    void onGmsConnectionResultResolutionRequired(ConnectionResult connectionResult);

    void onGmsConnectionResultNoResolution(int errorCode);

    void setGoToPostLocationHandled();

    void startLocationSyncService();

    void startPhrasebookActivity();

    void startTeamLocationsMapActivity();

    void disablePostLocationShortcut();
}
