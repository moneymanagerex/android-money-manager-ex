/*
 * Copyright (C) 2025-2025 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.core;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.money.manager.ex.R;

import java.util.Objects;

public abstract class AbsRecyclerFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private View mEmptyView;
    private View mProgressContainer;
    private View mListContainer;
    private boolean mListShown = true;
    private RecyclerView.Adapter<?> mAdapter;

    // Handler to manage UI updates like focusing on the RecyclerView
    private final Handler mHandler = new Handler();

    // Check if RecyclerView is empty
    private final Runnable mRequestFocus = new Runnable() {
        @Override
        public void run() {
            mRecyclerView.requestFocus();
        }
    };

    // Create view for the fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.abs_recycler_fragment, container, false);
    }

    // Initialize views and RecyclerView settings
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mEmptyView = view.findViewById(R.id.emptyView);
        mProgressContainer = view.findViewById(R.id.progressContainer);
        mListContainer = view.findViewById(R.id.listContainer);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
    }

    // Update RecyclerView adapter
    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        this.mAdapter = adapter;
        mRecyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                checkEmpty();
            }
        });
        checkEmpty();
    }

    // Check if RecyclerView is empty and adjust visibility accordingly
    private void checkEmpty() {
        boolean empty = mRecyclerView.getAdapter() == null || mRecyclerView.getAdapter().getItemCount() == 0;
        mEmptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        mRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // Set visibility of the list view (RecyclerView)
    protected void setListShown(boolean shown) {
        setListShown(shown, true);
    }

    // Control visibility with or without animation
    protected void setListShownNoAnimation(boolean shown) {
        setListShown(shown, false);
    }

    // Control visibility of list view, with an option for animation
    private void setListShown(boolean shown, boolean animate) {
        if (mProgressContainer == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }

        if (mListShown == shown) {
            return;
        }
        mListShown = shown;

        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }

            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }

            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.GONE);
        }
    }

    // Set the empty view text
    public void setEmptyText(CharSequence text) {
        if (mEmptyView instanceof TextView) {
            ((TextView) mEmptyView).setText(text);
        } else {
            throw new IllegalStateException("Empty view must be a TextView");
        }
    }

    // Handle item clicks (can be overridden by subclasses)
    public void onItemClick(View view, int position) {
        // Implement in subclass if needed
    }

    // Handle item long clicks (can be overridden by subclasses)
    public void onItemLongClick(View view, int position) {
        // Implement in subclass if needed
    }

    // Set the selection position of the RecyclerView
    public void setSelection(int position) {
        if (mRecyclerView != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPosition(position);
            }
        }
    }

    // Get the position of the selected item
    public int getSelectedItemPosition() {
        // Custom logic can be implemented to track selection
        return -1;
    }

    // Get the selected item ID (can be implemented based on your data model)
    public long getSelectedItemId() {
        // Custom logic can be implemented to track selection ID
        return -1;
    }

    // Get the RecyclerView itself
    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    // Handle fragment destruction and clean up
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacks(mRequestFocus);
        mRecyclerView = null;
        mEmptyView = null;
        mProgressContainer = null;
        mListContainer = null;
    }
}