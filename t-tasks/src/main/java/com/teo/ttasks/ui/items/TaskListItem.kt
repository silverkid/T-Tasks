package com.teo.ttasks.ui.items

import android.databinding.DataBindingUtil
import android.view.View
import com.teo.ttasks.R
import com.teo.ttasks.data.model.TTaskList
import com.teo.ttasks.databinding.ItemTaskListBinding
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import timber.log.Timber

class TaskListItem(private val taskList: TTaskList, private val taskCount: Long) : AbstractFlexibleItem<TaskListItem.ViewHolder>() {

    init {
        Timber.d("count %d", taskCount)
    }

    val title: String
        get() = taskList.title

    val id: String?
        get() = taskList.id

    override fun getLayoutRes(): Int {
        return R.layout.item_task_list
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<out IFlexible<*>>, viewHolder: ViewHolder, position: Int, payloads: MutableList<Any?>) {
        val itemTaskBinding = viewHolder.itemTaskListBinding
        val context = itemTaskBinding.root.context

        itemTaskBinding.taskListTitle.text = taskList.title
        itemTaskBinding.taskListSize.text = if (taskCount > 0) context.getString(R.string.task_list_size, taskCount) else context.getString(R.string.empty_task_list)
    }

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<out IFlexible<*>>): ViewHolder {
        return ViewHolder(view, adapter)
    }

    override fun equals(other: Any?): Boolean = other is TaskListItem && id == other.id

    class ViewHolder internal constructor(view: View, adapter: FlexibleAdapter<out IFlexible<*>>) : FlexibleViewHolder(view, adapter) {
        var itemTaskListBinding: ItemTaskListBinding = DataBindingUtil.bind(view)
    }
}
