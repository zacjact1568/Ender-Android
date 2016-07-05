package com.zack.enderplan.interactor.presenter;

import android.content.ContentValues;
import android.view.View;

import com.zack.enderplan.model.bean.Type;
import com.zack.enderplan.model.database.DatabaseDispatcher;
import com.zack.enderplan.event.DataLoadedEvent;
import com.zack.enderplan.event.PlanCreatedEvent;
import com.zack.enderplan.event.PlanDeletedEvent;
import com.zack.enderplan.event.PlanDetailChangedEvent;
import com.zack.enderplan.event.TypeCreatedEvent;
import com.zack.enderplan.event.TypeDetailChangedEvent;
import com.zack.enderplan.interactor.adapter.TypeAdapter;
import com.zack.enderplan.model.ram.DataManager;
import com.zack.enderplan.domain.view.AllTypesView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class AllTypesPresenter implements Presenter<AllTypesView> {

    private AllTypesView allTypesView;
    private DataManager dataManager;
    private TypeAdapter typeAdapter;
    private DatabaseDispatcher databaseDispatcher;
    //private int typeItemClickPosition;

    public AllTypesPresenter(AllTypesView allTypesView) {
        attachView(allTypesView);
        dataManager = DataManager.getInstance();
        databaseDispatcher = DatabaseDispatcher.getInstance();
    }

    @Override
    public void attachView(AllTypesView view) {
        allTypesView = view;
        EventBus.getDefault().register(this);//TODO 这个类里可以不用EventBus？
    }

    @Override
    public void detachView() {
        allTypesView = null;
        EventBus.getDefault().unregister(this);
    }

    //储存类型列表的排序
    public void syncWithDatabase() {
        for (int i = 0; i < dataManager.getTypeCount(); i++) {
            Type type = dataManager.getType(i);
            if (type.getTypeSequence() != i) {
                //更新typeList
                //在移动typeList的item的时候只是交换了items在list中的位置，并没有改变item中的type_sequence
                type.setTypeSequence(i);
                //更新数据库
                ContentValues values = new ContentValues();
                values.put("type_sequence", i);
                databaseDispatcher.editType(type.getTypeCode(), values);
            }
        }
    }

    public void createTypeAdapter() {
        typeAdapter = new TypeAdapter(dataManager.getTypeList(), dataManager.getTypeMarkAndColorResMap(), dataManager.getUcPlanCountOfEachTypeMap());
        typeAdapter.setOnTypeItemClickListener(new TypeAdapter.OnTypeItemClickListener() {
            @Override
            public void onTypeItemClick(View itemView, int position) {
                //typeItemClickPosition = position;
                allTypesView.onShowTypeDetailDialogFragment(position);
            }
        });
    }

    public TypeAdapter getTypeAdapter() {
        return typeAdapter;
    }

    public void notifyTypeSequenceChanged(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                dataManager.swapTypesInTypeList(i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                dataManager.swapTypesInTypeList(i, i - 1);
            }
        }
        typeAdapter.notifyItemMoved(fromPosition, toPosition);
    }

    public void notifyTypeDeleted(int position) {
        Type type = dataManager.getType(position);

        dataManager.removeFromTypeList(position);
        typeAdapter.notifyItemRemoved(position);

        if (dataManager.isUcPlanCountOfOneTypeExists(type.getTypeCode())) {
            dataManager.addToTypeList(position, type);
            typeAdapter.notifyItemInserted(position);
            //弹提示有未完成的计划属于该类型的dialog
            allTypesView.onShowPlanCountOfOneTypeExistsDialog();
        } else {
            dataManager.updateTypeMarkList(type.getTypeMark());
            dataManager.removeMappingInFindingColorResMap(type.getTypeCode(), type.getTypeMark());
            //弹提示删除成功的SnackBar
            allTypesView.onTypeDeleted(type.getTypeName(), position, type);
            databaseDispatcher.deleteType(type.getTypeCode());
        }
    }

    public void notifyTypeRecreated(int position, Type type) {
        dataManager.addToTypeList(position, type);
        dataManager.updateTypeMarkList(type.getTypeMark());
        dataManager.putMappingInFindingColorResMap(type.getTypeCode(), type.getTypeMark());
        typeAdapter.notifyItemInserted(position);
        databaseDispatcher.saveType(type);
    }

    @Subscribe
    public void onTypeListLoaded(DataLoadedEvent event) {
        typeAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onTypeCreated(TypeCreatedEvent event) {
        typeAdapter.notifyItemInserted(dataManager.getTypeCount() - 1);
    }

    @Subscribe
    public void onPlanCreated(PlanCreatedEvent event) {
        //在Plan方面的状态改变，一般用全部刷新
        typeAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onTypeDetailChanged(TypeDetailChangedEvent event) {
        typeAdapter.notifyItemChanged(event.position);
    }

    @Subscribe
    public void onPlanDetailChanged(PlanDetailChangedEvent event) {
        //类型、完成情况改变后的刷新（普通改变不需要在此界面上呈现）
        //因为可能有多个item需要更新，比较麻烦，所以直接全部刷新了
        typeAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onPlanDeleted(PlanDeletedEvent event) {
        //TODO 后续可改成定点刷新
        typeAdapter.notifyDataSetChanged();
    }
}