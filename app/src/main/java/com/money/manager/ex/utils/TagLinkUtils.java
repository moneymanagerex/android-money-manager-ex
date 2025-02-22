package com.money.manager.ex.utils;

import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.money.manager.ex.R;
import com.money.manager.ex.datalayer.TagRepository;
import com.money.manager.ex.datalayer.TaglinkRepository;
import com.money.manager.ex.domainmodel.RefType;
import com.money.manager.ex.domainmodel.Tag;
import com.money.manager.ex.domainmodel.TagLink;

import java.util.ArrayList;
import java.util.Objects;

public class TagLinkUtils {
    private Context mContext;
    private AlertDialog mDialog;
    private ArrayList<Tag> mTagsList;
    private ArrayList<TagLink> mTagLinks;
    private TaglinkRepository taglinkRepository;
    private TagRepository tagRepository;

    public interface OnTagSelected {
        void onTagSelected(ArrayList<TagLink> tagLinks);
    }

    public TagLinkUtils(@NonNull Context context) {
        mContext = context;
        mTagsList = new ArrayList<Tag>();
        mTagLinks = new ArrayList<TagLink>();
        taglinkRepository = new TaglinkRepository(mContext);
        tagRepository = new TagRepository(mContext);
        // load tags for controls
        mTagsList = tagRepository.getAllActiveTag();
    }

    // set and get method for taglink instance
    public ArrayList<TagLink> getTagsLink() { return mTagLinks; }
    public void setTagsLink(ArrayList<TagLink> tagLinks) { mTagLinks = tagLinks; }

    // private method
    private void setMDialog(AlertDialog a) {
        this.mDialog = a;
    }
    private AlertDialog getMDialog() {return mDialog;};

    /**
     * @param tagTextView   : TextView to display tags
     * @param tagLink       : List of tags coming from transaction
     * @param transactionId : transaction id
     * @param tagRefType    : type of transaction (see TagLink.REFTYPE*)
     * @param onTagSelected : call back event after dialog dismiss
     */
    public void initTagControls(TextView tagTextView,
                                ArrayList<TagLink> tagLink,
                                Long transactionId,
                                RefType tagRefType,
                                OnTagSelected onTagSelected ) {
        if( tagTextView == null ) return;
        mTagLinks = Objects.requireNonNullElseGet(tagLink, ArrayList::new);

        // inizialize display
        displayTags(tagTextView);

        tagTextView.setOnClickListener(v -> {

            boolean[] tagsFlag = new boolean[mTagsList.size()];
            String[] tagsListString = new String[mTagsList.size()];
            for (int i = 0; i < mTagsList.size(); i++) {
                tagsListString[i] = mTagsList.get(i).getName();
                // set default from mTagLink
                long tagId = mTagsList.get(i).getId();
                if ( mTagLinks.stream().filter(x -> x.getTagId() == tagId ).findFirst().isPresent() ) {
                    tagsFlag[i] = true;
                };
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            // set title
            builder.setTitle(R.string.tagsList_transactions);
            builder.setCancelable(false);
            builder.setMultiChoiceItems(tagsListString, tagsFlag,  new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                    tagsFlag[i] = b;
                }
            });

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Initialize string builder
                    // Save also taglink, loop at mtaglink to check actual record
                    for (int j = 0; j < mTagsList.size(); j++) {
                        long tagId = mTagsList.get(j).getId();
                        TagLink taglink ;
                        try {
                            taglink = mTagLinks.stream().filter(x -> x.getTagId() == tagId ).findFirst().get();
                        } catch ( Exception e) {
                            taglink = null;
                        }
                        if (taglink == null ) {
                            if ( ! tagsFlag[j] ) {
                                // flag off and mlink not present, nothing to do
                            } else {
                                // flag on and mlink not present, create
                                taglink = new TagLink();
                                taglink.setRefType(tagRefType);
                                taglink.setRefId(transactionId);
                                taglink.setTagId(tagId);
                                mTagLinks.add(taglink);
                            }
                        } else {
                            if ( ! tagsFlag[j] ) {
                                // flag off and mlink is present, delete
                                mTagLinks.remove(taglink);
                            } else {
                                // flag on and mlink present  nothing
                            }
                        }
                    }
                    // update UI field
                    displayTags(tagTextView);
                    // callback
                    callback(onTagSelected);
                }
            });

            builder.setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // dismiss dialog
                    callback(onTagSelected);
//                    dialogInterface.dismiss();
                }
            });

            builder.setNeutralButton(R.string.CLEAR_ALL, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mTagLinks.clear();
                    displayTags(tagTextView);
                    callback(onTagSelected);
                }
            });
            // show dialog
            setMDialog(builder.create());
            builder.show();
        });

    }

    public void displayTags(TextView tagTextView) {
        if( tagTextView == null ) return;
        tagTextView.setText( taglinkRepository.loadTagsfor(mTagLinks) );
    }

    private void callback(OnTagSelected onTagSelected) {
        if (onTagSelected != null) { onTagSelected.onTagSelected(mTagLinks); };
    }

    /*
    TODO:
    refactor transaction code

    app/src/main/java/com/money/manager/ex/transactions/SplitCategoriesActivity.java
    app/src/main/java/com/money/manager/ex/transactions/SplitCategoriesAdapter.java
    app/src/main/java/com/money/manager/ex/transactions/SplitItemViewHolder.java
    app/src/main/java/com/money/manager/ex/transactions/events/TagRequestedEvent.java
    app/src/main/res/layout/item_splittransaction.xml
    */

}
