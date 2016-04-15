package pl.temomuko.autostoprace.ui.teamslocationsmap.adapter;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.Collection;
import java.util.Iterator;

import pl.temomuko.autostoprace.R;

/**
 * Created by Rafał Naniewicz on 02.04.2016.
 */
public class LocationRecordClusterRenderer extends DefaultClusterRenderer<LocationRecordClusterItem> {

    private static final int MIN_CLUSTER_SIZE = 10;
    private Context mContext;

    public LocationRecordClusterRenderer(Context context, GoogleMap map, ClusterManager<LocationRecordClusterItem> clusterManager) {
        super(context, map, clusterManager);
        mContext = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(LocationRecordClusterItem item, MarkerOptions markerOptions) {
        String message = item.getMessage();
        markerOptions
                .title(message == null || message.isEmpty() ?
                        mContext.getString(R.string.msg_location_record_received) : message)
                .snippet(item.getReceiptDateString())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.asr_marker_48dp));
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<LocationRecordClusterItem> cluster, MarkerOptions markerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions);
        LocationRecordClusterItem lastClusterItem = getNewestClusterItem(cluster.getItems());
        String lastMessage = lastClusterItem.getMessage();
        if (lastMessage == null) {
            markerOptions.title(mContext.getString(R.string.msg_last_location_record_received))
                    .snippet(lastClusterItem.getReceiptDateString());
        } else {
            markerOptions.title(mContext.getString(R.string.msg_last_location_record_message) + "\n"
                    + lastClusterItem.getMessage())
                    .snippet(lastClusterItem.getReceiptDateString());
        }
    }

    private LocationRecordClusterItem getNewestClusterItem(Collection<LocationRecordClusterItem> locationRecordClusterItems) {
        if (!locationRecordClusterItems.isEmpty()) {
            Iterator<LocationRecordClusterItem> locationRecordClusterItemIterator
                    = locationRecordClusterItems.iterator();
            LocationRecordClusterItem newestLocationRecordClusterItem =
                    locationRecordClusterItemIterator.next();
            LocationRecordClusterItem currentLocationRecordCluster;
            while (locationRecordClusterItemIterator.hasNext()) {
                currentLocationRecordCluster = locationRecordClusterItemIterator.next();
                if (newestLocationRecordClusterItem.getReceiptDate().before(
                        currentLocationRecordCluster.getReceiptDate())) {
                    newestLocationRecordClusterItem = currentLocationRecordCluster;
                }
            }
            return newestLocationRecordClusterItem;
        } else {
            return new LocationRecordClusterItem(0, 0, "something went wrong", null);
        }
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<LocationRecordClusterItem> cluster) {
        return cluster.getSize() > MIN_CLUSTER_SIZE;
    }
}
