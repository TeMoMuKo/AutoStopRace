package pl.temomuko.autostoprace.ui.contact.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import pl.temomuko.autostoprace.R;
import pl.temomuko.autostoprace.data.model.ContactField;
import pl.temomuko.autostoprace.injection.AppContext;
import pl.temomuko.autostoprace.ui.contact.helper.ContactHandler;

/**
 * Created by Rafał Naniewicz on 17.04.2016.
 */
public class ContactRowsAdapter extends RecyclerView.Adapter<ContactRowsAdapter.ViewHolder> {

    private static final String TAG = ContactRowsAdapter.class.getSimpleName();

    private List<ContactField> mContactFieldList;
    private Context mContext;
    private OnContactRowClickListener mOnContactRowClickListener;

    public interface OnContactRowClickListener {

        void onContactRowClick(String type, String value);
    }

    @Inject
    public ContactRowsAdapter(@AppContext Context context) {
        mContactFieldList = new ArrayList<>();
        mContext = context;
    }

    public void setOnContactRowClickListener(OnContactRowClickListener onContactRowClickListener) {
        mOnContactRowClickListener = onContactRowClickListener;
    }

    public void updateContactRows(List<ContactField> contactFields) {
        mContactFieldList = contactFields;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_contact_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ContactField currentContactField = mContactFieldList.get(position);
        Picasso.with(mContext)
                .load(ContactHandler.getIcon(currentContactField.getType()))
                .into(holder.mActionIconImageView);
        setColorFilter(holder, currentContactField.getType());
        holder.mTvContent.setText(currentContactField.getDisplayedValue());
        holder.mTvContentDescription.setText(currentContactField.getDescription());
        holder.itemView.setOnClickListener(view ->
                mOnContactRowClickListener.onContactRowClick(currentContactField.getType(), currentContactField.getValue())
        );
    }

    private void setColorFilter(ViewHolder holder, String currentContactRowType) {
        if (ContactHandler.canSetColorFilter(currentContactRowType)) {
            holder.mActionIconImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.accent));
        } else {
            holder.mActionIconImageView.clearColorFilter();
        }
    }

    @Override
    public int getItemCount() {
        return mContactFieldList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.image_action_icon) ImageView mActionIconImageView;
        @Bind(R.id.tv_displayed_value) TextView mTvContent;
        @Bind(R.id.tv_description) TextView mTvContentDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
