/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.money.manager.ex.currency.recycler;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.view.DividerItemDecoration;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CurrencyRecyclerListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CurrencyRecyclerListFragment
    extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public CurrencyRecyclerListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CurrencyRecyclerListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CurrencyRecyclerListFragment newInstance(String param1, String param2) {
        CurrencyRecyclerListFragment fragment = new CurrencyRecyclerListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        initializeList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_currency_recycler_list, container, false);
    }

    private void initializeList() {
        Context context = getActivity();

        RecyclerView list = (RecyclerView) getActivity().findViewById(R.id.list);
        if (list == null) return;

        // Layout manager
        list.setLayoutManager(new LinearLayoutManager(context));

        // Adapter
        CurrencyRecyclerListAdapter adapter = new CurrencyRecyclerListAdapter();
        list.setAdapter(adapter);

        // load data
        CurrencyService service = new CurrencyService(getActivity());
        adapter.usedCurrencies = service.getUsedCurrencies();
        adapter.unusedCurrencies = service.getUnusedCurrencies();

        list.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
    }

}
