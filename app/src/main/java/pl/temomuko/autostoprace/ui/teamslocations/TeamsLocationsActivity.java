package pl.temomuko.autostoprace.ui.teamslocations;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;

import butterknife.Bind;
import pl.temomuko.autostoprace.R;
import pl.temomuko.autostoprace.data.model.LocationRecord;
import pl.temomuko.autostoprace.ui.base.drawer.DrawerActivity;
import pl.temomuko.autostoprace.ui.teamslocationmap.adapter.LocationRecordClusterItem;
import pl.temomuko.autostoprace.ui.teamslocationmap.adapter.LocationRecordClusterRenderer;
import pl.temomuko.autostoprace.ui.teamslocations.adapter.TeamLocationInfoWindowAdapter;
import pl.temomuko.autostoprace.util.rx.RxUtil;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Szymon Kozak on 2016-02-05.
 */
public class TeamsLocationsActivity extends DrawerActivity implements TeamsLocationsMvpView, OnMapReadyCallback {

    private static final String TAG = TeamsLocationsActivity.class.getSimpleName();

    @Inject TeamsLocationsPresenter mTeamsLocationsPresenter;
    @Inject TeamLocationInfoWindowAdapter mTeamsLocationInfoWindowAdapter;

    @Bind(R.id.auto_complete_tv_team_number) AutoCompleteTextView mTeamNumberAutocompleteTextView;

    private GoogleMap mMap;
    private CompositeSubscription mSubscriptions;
    private ConcurrentLinkedQueue<LocationRecordClusterItem> mMapNotReadyQueue;
    private ClusterManager<LocationRecordClusterItem> mClusterManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams_location);
        getActivityComponent().inject(this);
        mMapNotReadyQueue = new ConcurrentLinkedQueue<>();
        mSubscriptions = new CompositeSubscription();
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_teams_locations_map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        mTeamsLocationsPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupClusterManager();
        addMarkersFromQueue();
    }

    private void setupClusterManager() {
        mClusterManager = new ClusterManager<>(getApplicationContext(), mMap);
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mClusterManager.setRenderer(new LocationRecordClusterRenderer(getApplicationContext(), mMap, mClusterManager));
        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(mTeamsLocationInfoWindowAdapter);
        mClusterManager.getClusterMarkerCollection().setOnInfoWindowAdapter(mTeamsLocationInfoWindowAdapter);
    }

    private void addMarkersFromQueue() {
        if (!mMapNotReadyQueue.isEmpty()) {
            for (LocationRecordClusterItem locationRecordClusterItem : mMapNotReadyQueue) {
                mClusterManager.addItem(locationRecordClusterItem);
            }
            mMapNotReadyQueue.clear();
        }
    }

    /* MVP View methods */

    @Override
    public void setLocations(@NonNull List<LocationRecord> locationRecords) {
        mSubscriptions.clear();
        mSubscriptions.add(Observable.from(locationRecords)
                .map(LocationRecordClusterItem::new)
                .toList()
                .compose(RxUtil.applyComputationSchedulers())
                .subscribe(this::handleLocationsToSet)
        );
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void handleLocationsToSet(List<LocationRecordClusterItem> locationRecordClusterItems) {
        if (mMap == null || mClusterManager == null) {
            mMapNotReadyQueue.clear();
            mMapNotReadyQueue.addAll(locationRecordClusterItems);
        } else {
            mClusterManager.clearItems();
            mClusterManager.addItems(locationRecordClusterItems);
            mClusterManager.cluster();
        }
    }

    @Override
    public void displayTeam(int teamId) {

    }
}