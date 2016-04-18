package pl.temomuko.autostoprace.ui.contact;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import pl.temomuko.autostoprace.R;
import pl.temomuko.autostoprace.data.model.ContactField;
import pl.temomuko.autostoprace.ui.base.drawer.DrawerActivity;
import pl.temomuko.autostoprace.ui.contact.adapter.AppBarStateChangeListener;
import pl.temomuko.autostoprace.ui.contact.adapter.ContactRowsAdapter;
import pl.temomuko.autostoprace.ui.contact.helper.ContactHandler;
import pl.temomuko.autostoprace.ui.contact.helper.NoIntentHandlerException;
import pl.temomuko.autostoprace.ui.widget.VerticalDividerItemDecoration;

/**
 * Created by Szymon Kozak on 2016-02-05.
 */
public class ContactActivity extends DrawerActivity implements ContactMvpView, ContactRowsAdapter.OnContactRowClickListener {

    private static final String TAG = ContactActivity.class.getSimpleName();
    public static final int TITLE_ANIMATION_DURATION = 500;

    @Inject ContactPresenter mContactPresenter;
    @Inject ContactRowsAdapter mContactRowsAdapter;
    @Inject VerticalDividerItemDecoration mVerticalDividerItemDecorator;
    @Inject ContactHandler mContactContactHandler;

    @Bind(R.id.collapsing_toolbar_layout) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @Bind(R.id.app_bar) AppBarLayout mAppBarLayout;
    @Bind(R.id.iv_collapsing_toolbar_background) ImageView mCollapsingToolbarBackground;
    @Bind(R.id.fab_contact_action) FloatingActionButton mFabContactAction;
    @Bind(R.id.rv_contact_rows) RecyclerView mRecyclerView;
    private ValueAnimator mCollapsingToolbarTitleAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        getActivityComponent().inject(this);
        mContactPresenter.attachView(this);
        mContactPresenter.setupUserInfoInDrawer();
        setupCollapsingToolbar();
        setupRecyclerView();
        mContactPresenter.loadContactData();
    }

    private void setupCollapsingToolbar() {
        setupCollapsingToolbarTitleAnimation();
        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener(mCollapsingToolbarLayout) {
            @Override
            public void onScrollStateExpanded() {
                if (mCollapsingToolbarLayout.getTitle() == null) {
                    mCollapsingToolbarLayout.setTitle(getString(R.string.title_activity_contact));
                    mCollapsingToolbarTitleAnimation.start();
                }
            }

            @Override
            public void onScrollStateCollapsed() {
                if (mCollapsingToolbarLayout.getTitle() != null) {
                    mCollapsingToolbarLayout.setTitle(null);
                }
            }
        });
        Picasso.with(this).load(R.drawable.img_team_asr)
                .placeholder(R.drawable.img_team_asr_placeholder)
                .noFade()
                .fit()
                .centerCrop()
                .into(mCollapsingToolbarBackground);
    }

    private void setupCollapsingToolbarTitleAnimation() {
        mCollapsingToolbarTitleAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                ContextCompat.getColor(getApplicationContext(), R.color.transparent_white),
                ContextCompat.getColor(getApplicationContext(), R.color.primary_light_text_standard));
        mCollapsingToolbarTitleAnimation.setDuration(TITLE_ANIMATION_DURATION);
        mCollapsingToolbarTitleAnimation.addUpdateListener(animation ->
                mCollapsingToolbarLayout.setCollapsedTitleTextColor((Integer) animation.getAnimatedValue())
        );
    }

    private void setupRecyclerView() {
        mContactRowsAdapter.setOnContactRowClickListener(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mContactRowsAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.addItemDecoration(mVerticalDividerItemDecorator);
    }

    @Override
    protected void onDestroy() {
        mContactPresenter.detachView();
        super.onDestroy();
    }

    /* MVP View methods */

    @Override
    public void setContactRows(List<ContactField> contactFields) {
        mContactRowsAdapter.updateContactRows(contactFields);
    }

    @Override
    public void setUpFab(ContactField fabContactField) {
        mFabContactAction.setVisibility(View.VISIBLE);
        mFabContactAction.setImageResource(ContactHandler.getIcon(fabContactField.getType()));
        mFabContactAction.setOnClickListener(view -> {
            try {
                mContactContactHandler.startIntent(fabContactField.getType(), fabContactField.getValue());
            } catch (NoIntentHandlerException e) {
                showNoIntentHandlerError();
            }
        });
    }

    /* Contact row listener methods */

    @Override
    public void onContactRowClick(String type, String value) {
        try {
            mContactContactHandler.startIntent(type, value);
        } catch (NoIntentHandlerException e) {
            showNoIntentHandlerError();
        }
    }

    private void showNoIntentHandlerError() {
        Toast.makeText(this, R.string.error_action_cant_be_handled, Toast.LENGTH_SHORT).show();
    }
}
