package com.isbx.androidtools.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Extending this class allows you to hook in to the major lifecycle events of a specific
 * {@link Activity} instance.
 *
 * <p>
 * Due to the way starting activities is implemented in Android, a common use case will entail
 * attaching the listener in the activity's onCreate() method, or possibly even afterwards. For
 * these situations, you should instantiate the listener using
 * {@link ActivityLifecycleListener#ActivityLifecycleListener(Activity)} or
 * {@link ActivityLifecycleListener#ActivityLifecycleListener(Activity, Bundle)}. These constructors
 * assume onCreate() has already been called, and will trigger the appropriate callbacks.
 * </p>
 *
 * <p>
 * The only time you should use
 * {@link ActivityLifecycleListener#ActivityLifecycleListener(Activity, Application)} is in the rare
 * case that you need to instantiate the listener before the activity's onCreate() method is called.
 * This specific constructor assumes onCreate() has <strong>not</strong> been called and will wait
 * until it is before triggering the relevant callback.
 * </p>
 *
 * @see Activity#onCreate(Bundle)
 * @see Activity#onStart()
 * @see Activity#onResume()
 * @see Activity#onPause()
 * @see Activity#onStop()
 * @see Activity#onDestroy()
 * @see Activity#onSaveInstanceState(Bundle)
 */
public abstract class ActivityLifecycleListener implements Application.ActivityLifecycleCallbacks {

    private Activity activity;

    /**
     * Creates a new instance that will listen to the lifecycle events of the given
     * {@link Activity}. This assumes {@code activity}'s onCreate() method has already been invoked,
     * and will instantly trigger this listener's {@link ActivityLifecycleListener#onCreate(Bundle)}
     * with a null {@link Bundle} parameter.
     *
     * @param activity The {@link Activity} to listen to for lifecycle events
     *
     * @see ActivityLifecycleListener#attachTo(Activity)
     */
    public ActivityLifecycleListener(Activity activity) {
        attachTo(activity);
    }

    /**
     * Creates a new instance that will listen to the lifecycle events of the given
     * {@link Activity}. This assumes {@code activity}'s onCreate() method has already been invoked,
     * and will instantly trigger this listener's {@link ActivityLifecycleListener#onCreate(Bundle)}
     * with {@code creationInstanceState} as a parameter.
     *
     * @param activity The {@link Activity} to listen to for lifecycle events
     * @param creationInstanceState The {@link Bundle} that was passed in to {@code activity} in its
     *                              onCreate() method.
     *
     * @see ActivityLifecycleListener#attachTo(Activity, Bundle)
     */
    public ActivityLifecycleListener(Activity activity, Bundle creationInstanceState) {
        attachTo(activity, creationInstanceState);
    }

    /**
     * Creates a new instance that will listen to the lifecycle events of the given
     * {@link Activity}. This assumes {@code activity}'s onCreate() method has <strong>not</strong>
     * already been invoked, and until it is this listener's
     * {@link ActivityLifecycleListener#onCreate(Bundle)} will not be called.
     *
     * @param activity The {@link Activity} to listen to for lifecycle events
     * @param application The {@link Application} instance for this activity, required to actually
     *                    listen for lifecycle events
     *
     * @see ActivityLifecycleListener#attachTo(Activity, Application)
     */
    public ActivityLifecycleListener(Activity activity, Application application) {
        attachTo(activity, application);
    }

    /**
     * Attach this listener to the given {@link Activity} to listen for its lifecycle events. This
     * assumes {@code activity}'s onCreate() method has already been invoked, and will instantly
     * trigger this listener's {@link ActivityLifecycleListener#onCreate(Bundle)} with a null
     * {@link Bundle} parameter. This listener will stop receiving lifecycle events for any
     * activities it was previously attached to.
     *
     * @param activity The {@link Activity} to listen to for lifecycle events
     */
    public void attachTo(Activity activity) {
        attachTo(activity, (Bundle) null);
    }

    /**
     * Attach this listener to the given {@link Activity} to listen for its lifecycle events. This
     * assumes {@code activity}'s onCreate() method has already been invoked, and will instantly
     * trigger this listener's {@link ActivityLifecycleListener#onCreate(Bundle)} with
     * {@code creationInstanceState} as a parameter. This listener will stop receiving lifecycle
     * events for any activities it was previously attached to.
     *
     * @param activity The {@link Activity} to listen to for lifecycle events
     * @param creationInstanceState The {@link Bundle} that was passed in to {@code activity} in its
     *                              onCreate() method.
     */
    public void attachTo(Activity activity, Bundle creationInstanceState) {
        attachTo(activity, activity.getApplication());
        onCreate(creationInstanceState);
    }

    /**
     * Attach this listener to the given {@link Activity} to listen for its lifecycle events. This
     * assumes {@code activity}'s onCreate() method has <strong>not</strong> already been invoked,
     * and until it is this listener's {@link ActivityLifecycleListener#onCreate(Bundle)} will not
     * be called. This listener will stop receiving lifecycle events for any activities it was
     * previously attached to.
     *
     * @param activity The {@link Activity} to listen to for lifecycle events
     * @param application The {@link Application} instance for this activity, required to actually
     *                    listen for lifecycle events
     */
    public void attachTo(Activity activity, Application application) {
        this.activity = activity;
        application.registerActivityLifecycleCallbacks(this);
    }

    /**
     * Returns the {@link Activity} this listener is currently listening to for lifecycle events.
     *
     * @return The {@link Activity} this listener is attached to
     */
    protected Activity getActivity() {
        return activity;
    }

    /**
     * Checks if the {@link Activity} this event is for matches the current attached activity, and,
     * if so, calls {@link ActivityLifecycleListener#onCreate(Bundle)}.
     */
    @Override
    public final void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (activity == this.activity) {
            onCreate(savedInstanceState);
        }
    }

    /**
     * Callback method to be implemented by subclasses to capture the activity's onCreate() event.
     *
     * @param savedInstanceState The {@link Bundle} that was passed in to the activity's onCreate()
     *                           method.
     *
     * @see Activity#onCreate(Bundle)
     */
    public void onCreate(Bundle savedInstanceState) {

    }

    /**
     * Checks if the {@link Activity} this event is for matches the current attached activity, and,
     * if so, calls {@link ActivityLifecycleListener#onStart()}.
     */
    @Override
    public final void onActivityStarted(Activity activity) {
        if (activity == this.activity) {
            onStart();
        }
    }

    /**
     * Callback method to be implemented by subclasses to capture the activity's onStart() event.
     *
     * @see Activity#onStart()
     */
    public void onStart() {

    }

    /**
     * Checks if the {@link Activity} this event is for matches the current attached activity, and,
     * if so, calls {@link ActivityLifecycleListener#onResume()}.
     */
    @Override
    public final void onActivityResumed(Activity activity) {
        if (activity == this.activity) {
            onResume();
        }
    }

    /**
     * Callback method to be implemented by subclasses to capture the activity's onResume() event.
     *
     * @see Activity#onResume()
     */
    public void onResume() {

    }

    /**
     * Checks if the {@link Activity} this event is for matches the current attached activity, and,
     * if so, calls {@link ActivityLifecycleListener#onPause()}.
     */
    @Override
    public final void onActivityPaused(Activity activity) {
        if (activity == this.activity) {
            onPause();
        }
    }

    /**
     * Callback method to be implemented by subclasses to capture the activity's onPause() event.
     *
     * @see Activity#onPause()
     */
    public void onPause() {

    }

    /**
     * Checks if the {@link Activity} this event is for matches the current attached activity, and,
     * if so, calls {@link ActivityLifecycleListener#onStop()}.
     */
    @Override
    public final void onActivityStopped(Activity activity) {
        if (activity == this.activity) {
            onStop();
        }
    }

    /**
     * Callback method to be implemented by subclasses to capture the activity's onStop() event.
     *
     * @see Activity#onStop()
     */
    public void onStop() {

    }

    /**
     * Checks if the {@link Activity} this event is for matches the current attached activity, and,
     * if so, calls {@link ActivityLifecycleListener#onSaveInstanceState(Bundle)}.
     */
    @Override
    public final void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        if (activity == this.activity) {
            onSaveInstanceState(outState);
        }
    }

    /**
     * Callback method to be implemented by subclasses to capture the activity's
     * onSaveInstanceState() event.
     *
     * @param outState The {@link Bundle} that was passed in to the activity's onCreate()
     *                           method.
     *
     * @see Activity#onSaveInstanceState(Bundle)
     */
    public void onSaveInstanceState(Bundle outState) {

    }

    /**
     * Checks if the {@link Activity} this event is for matches the current attached activity, and,
     * if so, calls {@link ActivityLifecycleListener#onDestroy()}.
     */
    @Override
    public final void onActivityDestroyed(Activity activity) {
        if (activity == this.activity) {
            activity.getApplication().unregisterActivityLifecycleCallbacks(this);
            onDestroy();
            this.activity = null;
        }
    }

    /**
     * Callback method to be implemented by subclasses to capture the activity's onDestroy() event.
     *
     * @see Activity#onDestroy()
     */
    public void onDestroy() {

    }
}
