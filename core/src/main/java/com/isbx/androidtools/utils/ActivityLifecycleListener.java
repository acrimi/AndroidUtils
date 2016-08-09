package com.isbx.androidtools.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by alexs_000 on 6/1/2016.
 */
public abstract class ActivityLifecycleListener implements Application.ActivityLifecycleCallbacks {

    private Activity activity;

    public ActivityLifecycleListener(Activity activity) {
        attachTo(activity);
    }

    public ActivityLifecycleListener(Activity activity, Bundle creationInstanceState) {
        attachTo(activity, creationInstanceState);
    }

    public ActivityLifecycleListener(Activity activity, Application application) {
        attachTo(activity, application);
    }

    public void attachTo(Activity activity) {
        attachTo(activity, (Bundle) null);
    }

    public void attachTo(Activity activity, Bundle creationInstanceState) {
        attachTo(activity, activity.getApplication());
        onCreate(creationInstanceState);
    }

    public void attachTo(Activity activity, Application application) {
        this.activity = activity;
        application.registerActivityLifecycleCallbacks(this);
    }

    protected Activity getActivity() {
        return activity;
    }

    @Override
    public final void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (activity == this.activity) {
            onCreate(savedInstanceState);
        }
    }

    public void onCreate(Bundle savedInstanceState) {

    }

    @Override
    public final void onActivityStarted(Activity activity) {
        if (activity == this.activity) {
            onStart();
        }
    }

    public void onStart() {

    }

    @Override
    public final void onActivityResumed(Activity activity) {
        if (activity == this.activity) {
            onResume();
        }
    }

    public void onResume() {

    }

    @Override
    public final void onActivityPaused(Activity activity) {
        if (activity == this.activity) {
            onPause();
        }
    }

    public void onPause() {

    }

    @Override
    public final void onActivityStopped(Activity activity) {
        if (activity == this.activity) {
            onStop();
        }
    }

    public void onStop() {

    }

    @Override
    public final void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        if (activity == this.activity) {
            onSaveInstanceState(outState);
        }
    }

    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public final void onActivityDestroyed(Activity activity) {
        if (activity == this.activity) {
            activity.getApplication().unregisterActivityLifecycleCallbacks(this);
            onDestroy();
            this.activity = null;
        }
    }

    public void onDestroy() {

    }
}
