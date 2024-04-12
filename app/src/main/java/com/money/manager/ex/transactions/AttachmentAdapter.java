/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.transactions;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.money.manager.ex.R;

import java.util.List;

// Custom RecyclerView adapter
class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.ViewHolder> {

    private List<String> attachmentUris;

    public AttachmentAdapter(List<String> attachmentUris) {
        this.attachmentUris = attachmentUris;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attachment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data and set click listener
        String uri = attachmentUris.get(position);
        holder.bind(uri);
    }

    @Override
    public int getItemCount() {
        return attachmentUris != null ? attachmentUris.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;
        private final Button buttonPreview;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewAttachment);
            buttonPreview = itemView.findViewById(R.id.buttonAttachment);
        }

        void bind(String uri) {
            textView.setText(Uri.parse(uri).getLastPathSegment());
            // Set click listener to open attachment using SAF
            buttonPreview.setOnClickListener(v -> openAttachmentWithSAF(Uri.parse(uri)));
        }

        private void openAttachmentWithSAF(Uri uri) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // This is necessary if you're passing a content URI
            itemView.getContext().startActivity(intent);
        }
    }
}
