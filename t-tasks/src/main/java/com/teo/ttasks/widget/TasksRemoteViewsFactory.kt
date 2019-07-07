package com.teo.ttasks.widget

import android.content.Context
import android.content.Intent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.teo.ttasks.R
import com.teo.ttasks.data.model.Task
import com.teo.ttasks.data.remote.TasksHelper
import com.teo.ttasks.ui.task_detail.TaskDetailActivity
import com.teo.ttasks.ui.task_detail.TaskDetailActivity.Companion.EXTRA_TASK_ID
import com.teo.ttasks.util.DateUtils
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber

class TasksRemoteViewsFactory
internal constructor(
    private val context: Context,
    intent: Intent,
    private val tasksHelper: TasksHelper
) : RemoteViewsService.RemoteViewsFactory {

    private val taskListId: String = intent.getStringExtra(TaskDetailActivity.EXTRA_TASK_LIST_ID)
    private val packageName: String = context.packageName
    private var tasks: List<Task>? = null

    override fun onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
    }

    override fun onDataSetChanged() {
        tasksHelper.getUnManagedTasks(taskListId)
            .filter { it.completedDate == null } // only active tasks
            .toList()
            .subscribe(
                { taskItems -> this.tasks = taskItems },
                { Timber.e(it, "Error while retrieving widget data") })
        Timber.d("Widget tasks count %d", tasks?.size ?: 0)
    }

    override fun onDestroy() {
        tasks = null
    }

    override fun getCount(): Int {
        return tasks?.size ?: 0
    }

    override fun getViewAt(position: Int): RemoteViews {
        val task = tasks!![position]

        val rv = RemoteViews(packageName, R.layout.item_task_widget)

        // Title
        rv.setTextViewText(R.id.task_title, task.title)

        // Set the click action
        val intent = Intent()
        intent.putExtra(EXTRA_TASK_ID, task.id)
        rv.setOnClickFillInIntent(R.id.item_task_widget, intent)

        // Task description
        val notes = task.notes
        if (notes.isNullOrBlank()) {
            rv.setViewVisibility(R.id.task_description, GONE)
        } else {
            rv.setTextViewText(R.id.task_description, notes)
            rv.setViewVisibility(R.id.task_description, VISIBLE)
        }

        // Due date
        val dueDate = task.dueDate
        if (dueDate != null) {
            rv.setTextViewText(R.id.date_day_name, dueDate.format(DateUtils.formatterDayName))
            rv.setTextViewText(R.id.date_day_number, dueDate.format(DateUtils.formatterDayNumber))

            // Reminder
            val reminder = task.reminderDate
            if (reminder == null) {
                rv.setViewVisibility(R.id.reminder, GONE)
            } else {
                rv.setTextViewText(R.id.reminder, reminder.format(DateTimeFormatter.ISO_LOCAL_TIME))
                rv.setViewVisibility(R.id.reminder, VISIBLE)
            }
        } else {
            rv.setTextViewText(R.id.date_day_name, null)
            rv.setTextViewText(R.id.date_day_number, null)
        }

        // Return the remote views object.
        return rv
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return tasks!![position].id.hashCode().toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}
