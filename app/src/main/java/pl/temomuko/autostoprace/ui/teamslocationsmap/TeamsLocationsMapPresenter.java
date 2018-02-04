package pl.temomuko.autostoprace.ui.teamslocationsmap;

import android.app.Activity;
import android.net.Uri;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import pl.temomuko.autostoprace.data.DataManager;
import pl.temomuko.autostoprace.data.local.LocationsViewMode;
import pl.temomuko.autostoprace.data.model.LocationRecord;
import pl.temomuko.autostoprace.data.model.Team;
import pl.temomuko.autostoprace.data.remote.ErrorHandler;
import pl.temomuko.autostoprace.data.remote.HttpStatus;
import pl.temomuko.autostoprace.data.remote.TeamNotFoundException;
import pl.temomuko.autostoprace.ui.base.drawer.DrawerBasePresenter;
import pl.temomuko.autostoprace.ui.teamslocationsmap.adapter.map.LocationRecordClusterItem;
import pl.temomuko.autostoprace.ui.teamslocationsmap.adapter.wall.WallItem;
import pl.temomuko.autostoprace.ui.teamslocationsmap.adapter.wall.WallItemsCreator;
import pl.temomuko.autostoprace.util.LogUtil;
import pl.temomuko.autostoprace.util.rx.RxCacheHelper;
import pl.temomuko.autostoprace.util.rx.RxUtil;
import retrofit2.Response;
import rx.Observable;
import rx.Single;
import rx.Subscription;

public class TeamsLocationsMapPresenter extends DrawerBasePresenter<TeamsLocationsMapMvpView> {

    private static final String TAG = TeamsLocationsMapPresenter.class.getSimpleName();

    private final ErrorHandler mErrorHandler;
    private final WallItemsCreator mWallItemsCreator;
    private Subscription mLoadAllTeamsSubscription;
    private Subscription mLoadTeamSubscription;
    private Subscription mHandleClusterSubscription;
    private RxCacheHelper<Response<List<Team>>> mRxAllTeamsCacheHelper;
    private RxCacheHelper<Response<List<LocationRecord>>> mRxTeamLocationsCacheHelper;

    @Inject
    public TeamsLocationsMapPresenter(
            DataManager dataManager,
            ErrorHandler errorHandler,
            WallItemsCreator wallItemsCreator
    ) {
        super(dataManager);
        mErrorHandler = errorHandler;
        mWallItemsCreator = wallItemsCreator;
    }

    @Override
    public void attachView(TeamsLocationsMapMvpView mvpView) {
        super.attachView(mvpView);
        getMvpView().setLocationsViewMode(mDataManager.getLocationsViewMode());
        if (mRxAllTeamsCacheHelper.isCached()) {
            continueCachedAllTeamsRequest();
        }
        if (mRxTeamLocationsCacheHelper.isCached()) {
            continueCachedTeamLocationsRequest();
        }
    }

    @Override
    public void detachView() {
        if (mLoadTeamSubscription != null) mLoadTeamSubscription.unsubscribe();
        if (mLoadAllTeamsSubscription != null) mLoadAllTeamsSubscription.unsubscribe();
        if (mHandleClusterSubscription != null) mHandleClusterSubscription.unsubscribe();
        super.detachView();
    }

    public void setupRxCacheHelper(Activity activity,
                                   RxCacheHelper<Response<List<Team>>> rxAllTeamsCacheHelper,
                                   RxCacheHelper<Response<List<LocationRecord>>> rxTeamLocationsCacheHelper) {
        mRxAllTeamsCacheHelper = rxAllTeamsCacheHelper;
        mRxAllTeamsCacheHelper.setup(activity);

        mRxTeamLocationsCacheHelper = rxTeamLocationsCacheHelper;
        mRxTeamLocationsCacheHelper.setup(activity);
    }

    public void loadAllTeams() {
        mRxAllTeamsCacheHelper.cache(
                mDataManager.getAllTeams()
                        .flatMap(HttpStatus::requireOk)
                        .compose(RxUtil.applyIoSchedulers())
        );
        continueCachedAllTeamsRequest();
    }

    public void loadTeam(String text) {
        try {
            int teamId = Integer.parseInt(text);
            loadTeam(teamId);
        } catch (NumberFormatException e) {
            getMvpView().showInvalidFormatError();
        }
    }

    public void loadTeam(int teamNumber) {
        getMvpView().clearCurrentTeamLocations();
        mRxTeamLocationsCacheHelper.cache(
                mDataManager.getTeamLocationRecordsFromServer(teamNumber)
                        .flatMap(listResponse -> listResponse.code() == HttpStatus.NOT_FOUND ?
                                Observable.error(new TeamNotFoundException(listResponse)) :
                                Observable.just(listResponse))
                        .flatMap(HttpStatus::requireOk)
                        .compose(RxUtil.applyIoSchedulers())
        );
        continueCachedTeamLocationsRequest();
    }

    public void toggleLocationsViewMode() {
        mDataManager.toggleLocationsViewMode();
        getMvpView().setLocationsViewMode(mDataManager.getLocationsViewMode());
    }

    public void setLocationsViewMode(LocationsViewMode mode) {
        mDataManager.setLocationsViewMode(mode);
        getMvpView().setLocationsViewMode(mDataManager.getLocationsViewMode());
    }

    public void handleMarkerClick(Uri imageUri) {
        if (imageUri != null) {
            getMvpView().openFullscreenImage(imageUri);
        }
    }

    public void handleClusterMarkerClick(final Collection<LocationRecordClusterItem> clusterItems) {
        if (mHandleClusterSubscription != null) mHandleClusterSubscription.unsubscribe();

        mHandleClusterSubscription = Single.fromCallable(() -> ClasterUtil.getNewestClusterItem(clusterItems))
                .map(LocationRecordClusterItem::getImageUri)
                .toObservable()
                .filter(uri -> uri != null)
                .compose(RxUtil.applyIoSchedulers())
                .subscribe(getMvpView()::openFullscreenImage,
                        throwable -> LogUtil.e(TAG, "Error occurred while looking for newest cluster:" + throwable.getMessage())
                );
    }

    /* Private helper methods */

    private void continueCachedAllTeamsRequest() {
        getMvpView().setAllTeamsProgress(true);
        if (mLoadAllTeamsSubscription != null) mLoadAllTeamsSubscription.unsubscribe();
        mLoadAllTeamsSubscription = mRxAllTeamsCacheHelper.getRestoredCachedObservable()
                .subscribe(
                        teams -> {
                            handleAllTeams(teams.body());
                            mRxAllTeamsCacheHelper.clearCache();
                        },
                        this::handleLoadAllTeamsError
                );
    }

    private void handleAllTeams(List<Team> teams) {
        getMvpView().setAllTeamsProgress(false);
        getMvpView().setHints(teams);
    }

    private void handleLoadAllTeamsError(Throwable throwable) {
        mRxAllTeamsCacheHelper.clearCache();
        getMvpView().setAllTeamsProgress(false);
        getMvpView().showError(mErrorHandler.getMessage(throwable));
    }

    private void continueCachedTeamLocationsRequest() {
        getMvpView().setTeamProgress(true);
        if (mLoadTeamSubscription != null) mLoadTeamSubscription.unsubscribe();
        mLoadTeamSubscription = mRxTeamLocationsCacheHelper.getRestoredCachedObservable()
                .subscribe(
                        listResponse -> {
                            handleTeamLocation(listResponse.body());
                            mRxTeamLocationsCacheHelper.clearCache();
                        },
                        this::handleLoadTeamError
                );
    }

    private void handleTeamLocation(List<LocationRecord> locations) {
        showLocationsForMap(locations);
        showLocationsForWall(locations);
    }

    private void showLocationsForMap(List<LocationRecord> locations) {
        getMvpView().setTeamProgress(false);
        getMvpView().setLocationsForMap(locations);
        if (locations.isEmpty()) {
            getMvpView().showNoLocationRecordsInfoForMap();
        }
    }

    private void showLocationsForWall(List<LocationRecord> locations) {
        List<WallItem> wallItems = mWallItemsCreator.createFromLocationRecords(locations);
        getMvpView().setWallItems(wallItems);
        if (locations.isEmpty()) {
            getMvpView().showNoLocationRecordsInfoForWall();
        }
    }

    private void handleLoadTeamError(Throwable throwable) {
        mRxTeamLocationsCacheHelper.clearCache();
        getMvpView().showError(mErrorHandler.getMessage(throwable));
        getMvpView().setTeamProgress(false);
    }
}
