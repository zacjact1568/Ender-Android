package com.zack.enderplan.interactor.presenter;

import android.view.View;

import com.zack.enderplan.model.bean.Type;
import com.zack.enderplan.event.DataLoadedEvent;
import com.zack.enderplan.event.PlanCreatedEvent;
import com.zack.enderplan.event.PlanDeletedEvent;
import com.zack.enderplan.event.PlanDetailChangedEvent;
import com.zack.enderplan.event.TypeCreatedEvent;
import com.zack.enderplan.event.TypeDetailChangedEvent;
import com.zack.enderplan.interactor.adapter.TypeAdapter;
import com.zack.enderplan.model.DataManager;
import com.zack.enderplan.domain.view.AllTypesView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class AllTypesPresenter implements Presenter<AllTypesView> {

    private AllTypesView mAllTypesView;
    private DataManager mDataManager;
    private TypeAdapter mTypeAdapter;

    public AllTypesPresenter(AllTypesView allTypesView) {
        attachView(allTypesView);
        mDataManager = DataManager.getInstance();
    }

    @Override
    public void attachView(AllTypesView view) {
        mAllTypesView = view;
        EventBus.getDefault().register(this);
    }

    @Override
    public void detachView() {
        mAllTypesView = null;
        EventBus.getDefault().unregister(this);
    }

    public void setInitialView() {
        mTypeAdapter = new TypeAdapter(mDataManager.getTypeList(), mDataManager.getTypeMarkAndColorResMap(), mDataManager.getUcPlanCountOfEachTypeMap());
        mAllTypesView.showInitialView(mTypeAdapter);
    }

    //储存类型列表的排序
    public void syncWithDatabase() {
        mDataManager.notifyTypeSequenceRearranged();
    }

    public void notifyTypeItemClicked(int position) {
        mAllTypesView.onShowTypeDetailDialogFragment(position);
    }

    public void notifyTypeSequenceChanged(int fromPosition, int toPosition) {
        mDataManager.swapTypesInTypeList(fromPosition, toPosition);
        mTypeAdapter.notifyItemMoved(fromPosition, toPosition);
    }

    public void notifyTypeDeleted(int position) {
        Type type = mDataManager.getType(position);

        if (mDataManager.isUcPlanCountOfOneTypeExists(type.getTypeCode())) {
            //Some uc plans belong to this type, do fake deleting
            mDataManager.removeFromTypeList(position);
            mTypeAdapter.notifyItemRemoved(position);

            //弹提示有未完成的计划属于该类型的dialog
            mAllTypesView.onShowPlanCountOfOneTypeExistsDialog();

            mDataManager.addToTypeList(position, type);
            mTypeAdapter.notifyItemInserted(position);
        } else {
            //Do real deleting
            mDataManager.notifyTypeDeleted(position);
            mTypeAdapter.notifyItemRemoved(position);
            //弹提示删除成功的SnackBar
            mAllTypesView.onTypeDeleted(type.getTypeName(), position, type);
        }
    }

    public void notifyTypeRecreated(int position, Type type) {
        mDataManager.notifyTypeCreated(position, type);
        mTypeAdapter.notifyItemInserted(position);
    }

    @Subscribe
    public void onTypeListLoaded(DataLoadedEvent event) {
        mTypeAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onTypeCreated(TypeCreatedEvent event) {
        mTypeAdapter.notifyItemInserted(mDataManager.getTypeCount() - 1);
    }

    @Subscribe
    public void onPlanCreated(PlanCreatedEvent event) {
        //在Plan方面的状态改变，一般用全部刷新
        mTypeAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onTypeDetailChanged(TypeDetailChangedEvent event) {
        mTypeAdapter.notifyItemChanged(event.getPosition());
    }

    @Subscribe
    public void onPlanDetailChanged(PlanDetailChangedEvent event) {
        //类型、完成情况改变后的刷新（普通改变不需要在此界面上呈现）
        //因为可能有多个item需要更新，比较麻烦，所以直接全部刷新了
        mTypeAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onPlanDeleted(PlanDeletedEvent event) {
        //TODO 后续可改成定点刷新
        mTypeAdapter.notifyDataSetChanged();
    }
}
