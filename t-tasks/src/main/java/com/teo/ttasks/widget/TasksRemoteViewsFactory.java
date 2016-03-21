package com.teo.ttasks.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.teo.ttasks.R;
import com.teo.ttasks.TTasksApp;
import com.teo.ttasks.data.local.RealmHelper;
import com.teo.ttasks.data.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.realm.Realm;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class TasksRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE", Locale.getDefault());

    @Inject
    RealmHelper mRealmHelper;

    private List<Task> mTasks;
    private Context mContext;
    private int mAppWidgetId;

    public TasksRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        TTasksApp.get(mContext).applicationComponent().inject(this);
        mRealmHelper.loadTaskLists()
                .switchMap(taskLists -> mRealmHelper.loadTasks(taskLists.get(0).getId()))
                .subscribe(
                        tasks -> {
                            mTasks = Realm.getDefaultInstance().copyFromRealm(tasks);
                        },
                        throwable -> Timber.e(throwable.toString()));
    }

    @Override
    public void onDestroy() {
        mTasks.clear();
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getCount() {
        return mTasks.size();
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Task task = mTasks.get(position);

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.item_task_widget);
        rv.setTextViewText(R.id.task_title, task.getTitle());

        // Task description
        if (task.getNotes() == null) {
            rv.setViewVisibility(R.id.task_description, GONE);
        } else {
            rv.setTextViewText(R.id.task_description, task.getNotes());
            rv.setViewVisibility(R.id.task_description, VISIBLE);
        }

        if (task.getDue() != null) {
            Date dueDate = task.getDue();
            simpleDateFormat.applyLocalizedPattern("EEE");
            rv.setTextViewText(R.id.date_day_name, simpleDateFormat.format(dueDate));
            simpleDateFormat.applyLocalizedPattern("d");
            rv.setTextViewText(R.id.date_day_number, simpleDateFormat.format(dueDate));

            Date reminder = task.getReminder();
            if (reminder != null) {
                simpleDateFormat.applyLocalizedPattern("hh:mma");
                rv.setTextViewText(R.id.task_reminder, simpleDateFormat.format(reminder));
                rv.setViewVisibility(R.id.task_reminder, VISIBLE);
            } else {
                rv.setViewVisibility(R.id.task_reminder, GONE);
            }
        } else {
            rv.setViewVisibility(R.id.task_description, VISIBLE);
            rv.setTextViewText(R.id.date_day_name, null);
            rv.setTextViewText(R.id.date_day_number, null);
        }

        // Return the remote views object.
        return rv;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}