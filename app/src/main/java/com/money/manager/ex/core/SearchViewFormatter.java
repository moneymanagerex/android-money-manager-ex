/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.money.manager.ex.R;

public class SearchViewFormatter {
    protected int mSearchBackGroundResource = 0;
    protected int mSearchIconResource = 0;
    protected boolean mSearchIconInside = false;
    protected boolean mSearchIconOutside = false;
    protected int mSearchVoiceIconResource = 0;
    protected int mSearchTextColorResource = 0;
    protected int mSearchHintColorResource = 0;
    protected String mSearchHintText = "";
    protected int mSearchHintTextResource = 0;
    protected int mInputType = Integer.MIN_VALUE;
    protected int mSearchCloseIconResource = 0;
    protected int mSearchCollapsedSearchIconResource = 0;
    protected TextView.OnEditorActionListener mEditorActionListener;
    protected Resources mResources;

    public SearchViewFormatter setSearchBackGroundResource(int searchBackGroundResource) {
        mSearchBackGroundResource = searchBackGroundResource;
        return this;
    }

    public SearchViewFormatter setSearchIconResource(int searchIconResource, boolean inside, boolean outside) {
        mSearchIconResource = searchIconResource;
        mSearchIconInside = inside;
        mSearchIconOutside = outside;
        return this;
    }

    public SearchViewFormatter setSearchVoiceIconResource(int searchVoiceIconResource) {
        mSearchVoiceIconResource = searchVoiceIconResource;
        return this;
    }

    public SearchViewFormatter setSearchTextColorResource(int searchTextColorResource) {
        mSearchTextColorResource = searchTextColorResource;
        return this;
    }

    public SearchViewFormatter setSearchHintColorResource(int searchHintColorResource) {
        mSearchHintColorResource = searchHintColorResource;
        return this;
    }

    public SearchViewFormatter setSearchHintText(String searchHintText) {
        mSearchHintText = searchHintText;
        return this;
    }

    public SearchViewFormatter setSearchHintTextResource(int searchHintText) {
        mSearchHintTextResource = searchHintText;
        return this;
    }

    public SearchViewFormatter setInputType(int inputType) {
        mInputType = inputType;
        return this;
    }

    public SearchViewFormatter setSearchCloseIconResource(int searchCloseIconResource) {
        mSearchCloseIconResource = searchCloseIconResource;
        return this;
    }

    public SearchViewFormatter setEditorActionListener(TextView.OnEditorActionListener editorActionListener) {
        mEditorActionListener = editorActionListener;
        return this;
    }

    public SearchViewFormatter setSearchCollapsedIconResource(int searchCollapsedIconResource) {
        this.mSearchCollapsedSearchIconResource = searchCollapsedIconResource;
        return this;
    }

    public void format(SearchView searchView) {
        if (searchView == null) {
            return;
        }

        mResources = searchView.getContext().getResources();
        if (mSearchBackGroundResource != 0) {
            View view = searchView.findViewById(R.id.search_plate);
            view.setBackgroundResource(mSearchBackGroundResource);

            view = searchView.findViewById(R.id.submit_area);
            view.setBackgroundResource(mSearchBackGroundResource);
        }

        if (mSearchVoiceIconResource != 0) {
            ImageView view = (ImageView) searchView.findViewById(R.id.search_voice_btn);
            view.setImageResource(mSearchVoiceIconResource);
        }

        if (mSearchCollapsedSearchIconResource != 0) {
            ImageView view = (ImageView) searchView.findViewById(R.id.search_button);
            view.setImageResource(mSearchCollapsedSearchIconResource);
        }

        if (mSearchCloseIconResource != 0) {
            ImageView view = (ImageView) searchView.findViewById(R.id.search_close_btn);
            view.setImageResource(mSearchCloseIconResource);
        }

        TextView view = (TextView) searchView.findViewById(R.id.search_src_text);
        if (mSearchTextColorResource != 0) {
            //view.setTextColor(mResources.getColor(mSearchTextColorResource));
            view.setTextColor(ContextCompat.getColor(searchView.getContext(), mSearchTextColorResource));
        }
        if (mSearchHintColorResource != 0) {
            //view.setHintTextColor(mResources.getColor(mSearchHintColorResource));
            view.setHintTextColor(ContextCompat.getColor(searchView.getContext(), mSearchHintColorResource));
        }
        if (mInputType > Integer.MIN_VALUE) {
            view.setInputType(mInputType);
        }
        if (mSearchIconResource != 0) {
            ImageView imageView = (ImageView) searchView.findViewById(R.id.search_mag_icon);

            if (mSearchIconInside) {
                //Drawable searchIconDrawable = mResources.getDrawable(mSearchIconResource);
                Drawable searchIconDrawable = ContextCompat.getDrawable(searchView.getContext(), mSearchIconResource);
                int size = (int) (view.getTextSize() * 1.25f);
                searchIconDrawable.setBounds(0, 0, size, size);

                if (mSearchHintTextResource != 0) {
                    mSearchHintText = mResources.getString(mSearchHintTextResource);
                }

                SpannableStringBuilder hintBuilder = new SpannableStringBuilder("   ");
                hintBuilder.append(mSearchHintText);
                hintBuilder.setSpan(
                        new ImageSpan(searchIconDrawable),
                        1,
                        2,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                view.setHint(hintBuilder);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            }
            if (mSearchIconOutside) {
                imageView = (ImageView) searchView.findViewById(R.id.search_button);

                imageView.setImageResource(mSearchIconResource);
            }
        }

        if (mEditorActionListener != null) {
            view.setOnEditorActionListener(mEditorActionListener);
        }
    }
}