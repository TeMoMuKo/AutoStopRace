package pl.temomuko.autostoprace.ui.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import pl.temomuko.autostoprace.AsrApplication;
import pl.temomuko.autostoprace.injection.component.ActivityComponent;
import pl.temomuko.autostoprace.injection.component.DaggerActivityComponent;
import pl.temomuko.autostoprace.injection.module.ActivityModule;

/**
 * Created by szymen on 2016-01-06.
 */
public class BaseActivity extends AppCompatActivity {

    private ActivityComponent mActivityComponent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityComponent().inject(this);
    }

    public ActivityComponent getActivityComponent() {
        if (mActivityComponent == null) {
            mActivityComponent = DaggerActivityComponent.builder()
                    .activityModule(new ActivityModule(this))
                    .applicationComponent(AsrApplication.get(this).getComponent())
                    .build();
        }
        return mActivityComponent;
    }
}
