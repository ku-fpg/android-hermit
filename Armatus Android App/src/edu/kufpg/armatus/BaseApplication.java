package edu.kufpg.armatus;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * This class manages the connection between an application's {@link Activity Activities}
 * and the {@link AsyncActivityTask AsyncActivityTasks} that they spawn.
 */
public class BaseApplication extends Application {
	/**
	 * Manages the application's references to {@link AsyncActivityTask AsyncActivitiyTasks}
	 * automatically by tracking each {@link Activity}'s lifecycle. This requires API 14
	 * or greater.
	 */
	private static class TaskCallbacks implements ActivityLifecycleCallbacks {
		private final BaseApplication mApp;

		public TaskCallbacks(BaseApplication app) {
			mApp = app;
		}

		@Override
		public void onActivityResumed(Activity activity) {
			mApp.attachActivity(activity);
		}

		@Override
		public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
			mApp.detachActivity(activity);
		}

		// Unneeded methods
		@Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
		@Override public void onActivityStarted(Activity activity) {}
		@Override public void onActivityPaused(Activity activity) {}
		@Override public void onActivityStopped(Activity activity) {}
		@Override public void onActivityDestroyed(Activity activity) {}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		registerActivityLifecycleCallbacks(new TaskCallbacks(this));
	}

	/**
	 * An {@link Activity} can spawn any number of {@link android.os.AsyncTask AsyncTasks}
	 * simultaneously, so use a {@link ListMultimap} to connect an {@code Activity}'s name
	 * and its {@link AsyncActivityTask AsyncActivityTasks}.
	 */
	private final ListMultimap<String, AsyncActivityTask<? extends Activity,?,?,?>> mActivityTaskMap = ArrayListMultimap.create();

	/**
	 * Removes unused {@link AsyncActivityTask AsyncActivityTasks} after they have
	 * completed execution.
	 * @param activity The {@link Activity} that spawned the {@code AsyncActivityTask}.
	 * @param task The {@code AsyncActivityTask} to remove.
	 */
	public <A extends Activity> void removeTask(A activity, AsyncActivityTask<A,?,?,?> task) {
		mActivityTaskMap.remove(activity.getClass().getName(), task);
	}

	/**
	 * Establishes a connection between an {@link Activity} and a {@link AsyncActivityTask}
	 * that will persist through device rotation or standby.
	 * @param activity The {@code Activity} that spawned the {@code AsyncActivityTask}.
	 * @param task The {@code AsyncActivityTask} to connect.
	 */
	public <A extends Activity> void addTask(A activity, AsyncActivityTask<A,?,?,?> task) {
		mActivityTaskMap.put(activity.getClass().getName(), task);
	}

	/**
	 * While an {@link Activity} rotates or is in standby, attempting to call an {@code Activity}
	 * method from one of its {@link AsyncActivityTask AsyncActivityTasks} can produce
	 * unexpected results. Use this method to set all of an {@code Activity}'s references
	 * in its tasks to {@code null} so that the tasks can work around rotation or standby.
	 * @param activity The {@code Activity} whose references should be set to null.
	 */
	public void detachActivity(Activity activity) {
		for (AsyncActivityTask<? extends Activity,?,?,?> task : mActivityTaskMap.get(activity.getClass().getName())) {
			task.detachActivity();
		}
	}

	/**
	 * Reestablishes the connection between an {@link Activity} and its {@link AsyncActivityTask
	 * AsyncActivityTasks} after the {@code Activity} is resumed.
	 * @param activity The {@code Activity} whose references should be reestablished.
	 */
	public <A extends Activity> void attachActivity(A activity) {
		for (AsyncActivityTask<? extends Activity,?,?,?> task : mActivityTaskMap.get(activity.getClass().getName())) {
			@SuppressWarnings("unchecked")
			AsyncActivityTask<A,?,?,?> castTask = (AsyncActivityTask<A,?,?,?>) task;
			castTask.attachActivity(activity);
		}
	}

	public void cancelTasks(Activity activity) {
		for (AsyncActivityTask<? extends Activity,?,?,?> task : mActivityTaskMap.get(activity.getClass().getName())) {
			task.cancel(true);
		}
	}

}