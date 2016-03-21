package com.teo.ttasks.ui.fragments.tasks;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.teo.ttasks.R;
import com.teo.ttasks.TTasksApp;
import com.teo.ttasks.ui.activities.main.MainActivity;
import com.teo.ttasks.ui.fragments.BaseFragment;
import com.teo.ttasks.ui.items.TaskItem;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class TasksFragment extends BaseFragment implements TasksView, SwipeRefreshLayout.OnRefreshListener {

    private static final String ARG_TASK_LIST_ID = "taskListId";
    private static final String ARG_TASK_LIST_NAME = "taskListName";

    @Bind(R.id.list) RecyclerView mTaskList;
    @Bind(R.id.fab) FloatingActionButton mFloatingActionButton;
    @Bind(R.id.items_loading_ui) View loadingUiView;
    @Bind(R.id.items_loading_error_ui) View errorUiView;
    @Bind(R.id.items_empty) View emptyUiView;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;

    @Inject TasksPresenter mTasksPresenter;

    private FastAdapter<TaskItem> mFastAdapter;
    private ItemAdapter<TaskItem> mItemAdapter;

    private String mTaskListId;
    private String mTaskListName;

    /**
     * Create a new instance of this fragment using the provided task list ID
     */
    public static TasksFragment newInstance(@NonNull String taskListId, @NonNull String taskListName) {
        TasksFragment tasksFragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_LIST_ID, taskListId);
        args.putString(ARG_TASK_LIST_NAME, taskListName);
        tasksFragment.setArguments(args);
        return tasksFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mTaskListId = getArguments().getString(ARG_TASK_LIST_ID);
        mTaskListName = getArguments().getString(ARG_TASK_LIST_NAME);
        TTasksApp.get(context).getTasksComponent().inject(this);
        mFastAdapter = new FastAdapter<>();
        mItemAdapter = new ItemAdapter<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //noinspection ConstantConditions
        ((MainActivity) getActivity()).toolbar().setTitle(mTaskListName);
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mTasksPresenter.bindView(this);

        // All the task items have the same size
        mTaskList.setHasFixedSize(true);
        mTaskList.setLayoutManager(new LinearLayoutManager(getContext()));
        //mTaskList.addItemDecoration(new DividerItemDecoration(getContext(), null));
        ((SimpleItemAnimator) mTaskList.getItemAnimator()).setSupportsChangeAnimations(false);
        mTaskList.setAdapter(mItemAdapter.wrap(mFastAdapter));

        mFloatingActionButton.setOnClickListener(view1 -> Toast.makeText(getContext(), "Click", Toast.LENGTH_SHORT).show());

        mSwipeRefreshLayout.setOnRefreshListener(this);

        mTasksPresenter.loadTasks(mTaskListId);
    }

    @Override
    public void onRefresh() {
        mTasksPresenter.reloadTasks(mTaskListId);
    }

    @Override
    public void showLoadingUi() {
        runOnUiThreadIfFragmentAlive(() -> {
            loadingUiView.setVisibility(VISIBLE);
            errorUiView.setVisibility(GONE);
            emptyUiView.setVisibility(GONE);
        });
    }

    @Override
    public void showErrorUi() {
        runOnUiThreadIfFragmentAlive(() -> {
            mSwipeRefreshLayout.setRefreshing(false);
            mItemAdapter.clear();
            loadingUiView.setVisibility(GONE);
            errorUiView.setVisibility(VISIBLE);
            emptyUiView.setVisibility(GONE);
        });
    }

    @Override
    public void showEmptyUi() {
        runOnUiThreadIfFragmentAlive(() -> {
            mItemAdapter.clear();
            mSwipeRefreshLayout.setRefreshing(false);
            loadingUiView.setVisibility(GONE);
            errorUiView.setVisibility(GONE);
            emptyUiView.setVisibility(VISIBLE);
        });
    }

    @Override
    public void showContentUi(@NonNull List<TaskItem> taskItems) {
        runOnUiThreadIfFragmentAlive(() -> {
            mItemAdapter.setNewList(taskItems);
            mSwipeRefreshLayout.setRefreshing(false);
            loadingUiView.setVisibility(GONE);
            errorUiView.setVisibility(GONE);
            emptyUiView.setVisibility(GONE);
        });
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        mTasksPresenter.unbindView(this);
        super.onDestroyView();
    }

    public String getTaskListId() {
        return mTaskListId;
    }
}