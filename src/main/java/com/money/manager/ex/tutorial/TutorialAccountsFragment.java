/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.tutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.money.manager.ex.R;
import com.shamanland.fonticon.FontIconDrawable;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TutorialAccountsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TutorialAccountsFragment
        extends Fragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TutorialAccountsFragment.
     */
    public static TutorialAccountsFragment newInstance() {
        TutorialAccountsFragment fragment = new TutorialAccountsFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public TutorialAccountsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tutorial_accounts, container, false);

        // customize the icon
//        FontIconDrawable drawable = FontIconDrawable.inflate(getActivity(), R.xml.ic_tutorial_accounts);
//        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
//        imageView.setImageDrawable(drawable);
        // this inflates pixelated image

        return view;
    }


}
