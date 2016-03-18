package pl.temomuko.autostoprace.data.local.gms;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

import javax.inject.Inject;
import javax.inject.Singleton;

import pl.temomuko.autostoprace.Constants;
import pl.temomuko.autostoprace.injection.AppContext;
import rx.Observable;

/**
 * Created by Rafał Naniewicz on 15.02.2016.
 */
@Singleton
public class GmsLocationHelper {

    private Context mContext;

    @Inject
    public GmsLocationHelper(@AppContext Context context) {
        mContext = context;
    }

    public Observable<Location> getDeviceLocation() {
        return LocationObservable.create(mContext, getLocationRequest());
    }

    public Observable<LocationSettingsResult> checkLocationSettings() {
        return ApiClientObservable.create(mContext, LocationServices.API)
                .flatMap(googleApiClient -> PendingResultObservable.create(
                        LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                                getLocationSettingsRequest(getLocationRequest()))));
    }

    private LocationSettingsRequest getLocationSettingsRequest(LocationRequest locationRequest) {
        return new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build();
    }

    private static LocationRequest getLocationRequest() {
        return new LocationRequest()
                .setFastestInterval(Constants.LOCATION_FASTEST_UPDATE_INTERVAL_MILLISECONDS)
                .setInterval(Constants.LOCATION_UPDATE_INTERVAL_MILLISECONDS)
                .setPriority(Constants.APP_LOCATION_ACCURACY);
    }
}
