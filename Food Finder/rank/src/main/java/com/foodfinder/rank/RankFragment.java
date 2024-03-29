package com.foodfinder.rank;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

public class RankFragment extends Fragment {

    private RankViewModel mViewModel;
    private Context mContext;

    private RecyclerView recyclerView;
    private SearchView searchView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view =inflater.inflate(R.layout.rank_fragment, container, false);

        initializeComponent(view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(RankViewModel.class);

        mViewModel.initializeViewModel(mContext);
        mViewModel.readUsers(recyclerView);
        searchView.setOnQueryTextListener(mViewModel.getSearchListener());

    }

    private void initializeComponent(View view)
    {
        recyclerView = (RecyclerView) view.findViewById(R.id.rank_recycle_view);
        searchView=(SearchView) view.findViewById(R.id.search_view_rank);
        mContext=getActivity().getApplicationContext();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

    }


}
