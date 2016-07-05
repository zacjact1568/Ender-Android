package com.zack.enderplan.domain.fragment;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zack.enderplan.R;
import com.zack.enderplan.model.bean.Type;
import com.zack.enderplan.interactor.presenter.AllTypesPresenter;
import com.zack.enderplan.util.Util;
import com.zack.enderplan.domain.view.AllTypesView;

public class AllTypesFragment extends Fragment implements AllTypesView {

    //private List<Type> typeList;
    //private TypeAdapter typeAdapter;
    //private DatabaseDispatcher databaseDispatcher;
    //private TypeModel typeModel;
    private AllTypesPresenter allTypesPresenter;
    private RecyclerView recyclerView;

    //private static final String ARG_PLAN_COUNT_OF_EACH_TYPE = "plan_count_of_each_type";

    //private Bundle planCountOfEachType;

    public AllTypesFragment() {
        // Required empty public constructor
    }

    /*public static AllTypesFragment newInstance(Bundle planCountOfEachType) {
        AllTypesFragment fragment = new AllTypesFragment();
        Bundle args = new Bundle();
        args.putBundle(ARG_PLAN_COUNT_OF_EACH_TYPE, planCountOfEachType);
        fragment.setArguments(args);
        return fragment;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            planCountOfEachType = getArguments().getBundle(ARG_PLAN_COUNT_OF_EACH_TYPE);
        }*/
        /*databaseDispatcher = DatabaseDispatcher.getInstance();
        typeModel = TypeModel.getInstance();
        typeList = typeModel.getTypeList();*/

        allTypesPresenter = new AllTypesPresenter(this);

        allTypesPresenter.createTypeAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_types, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(allTypesPresenter.getTypeAdapter());
        new ItemTouchHelper(new TypeListItemTouchCallback()).attachToRecyclerView(recyclerView);
    }

    @Override
    public void onPause() {
        super.onPause();
        allTypesPresenter.syncWithDatabase();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        allTypesPresenter.detachView();
    }

    @Override
    public void onShowTypeDetailDialogFragment(int position) {
        TypeDetailDialogFragment bottomSheet = TypeDetailDialogFragment.newInstance(position);
        bottomSheet.show(getFragmentManager(), "type_detail");
    }

    @Override
    public void onTypeDeleted(String typeName, final int position, final Type typeUseForTakingBack) {
        Util.makeShortVibrate();
        String text = typeName + " " + getResources().getString(R.string.deleted_prompt);
        Snackbar snackbar = Snackbar.make(recyclerView, text, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allTypesPresenter.notifyTypeRecreated(position, typeUseForTakingBack);
            }
        });
        snackbar.show();
    }

    @Override
    public void onShowPlanCountOfOneTypeExistsDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_dialog_type_not_empty)
                .setMessage(R.string.msg_dialog_type_not_empty)
                .setPositiveButton(R.string.dialog_button_ok, null)
                .show();
        /*builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                typeList.add(position, type);
                typeAdapter.notifyItemInserted(position);
            }
        });*/
    }

    private class TypeListItemTouchCallback extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            allTypesPresenter.notifyTypeSequenceChanged(viewHolder.getLayoutPosition(), target.getLayoutPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            allTypesPresenter.notifyTypeDeleted(viewHolder.getLayoutPosition());
        }

        @Override
        public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
            return .7f;
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                float alpha = 1 - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                viewHolder.itemView.setAlpha(alpha);
                viewHolder.itemView.setTranslationX(dX);
            }
        }
    }
}