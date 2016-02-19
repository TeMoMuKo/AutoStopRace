package pl.temomuko.autostoprace.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by szymen on 2016-02-19.
 */
public final class AndroidComponentUtil {

    private AndroidComponentUtil() {
        throw new AssertionError();
    }

    public static void toggleComponent(Context context, Class componentClass, boolean state) {
        ComponentName componentName = new ComponentName(context, componentClass);
        context.getPackageManager().setComponentEnabledSetting(componentName,
                state ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
