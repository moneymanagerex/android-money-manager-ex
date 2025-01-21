package com.money.manager.ex.tag;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.IntentFactory;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.database.SQLTypeTransaction;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.datalayer.TagRepository;
import com.money.manager.ex.domainmodel.Tag;
import com.money.manager.ex.search.SearchParameters;
import com.money.manager.ex.servicelayer.TagService;
import com.money.manager.ex.settings.AppSettings;

public class TagListFragment     extends BaseListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {


    public static String mAction = Intent.ACTION_EDIT;

    private static final int ID_LOADER_TAG = 0;

    private static final String SORT_BY_NAME = "UPPER(" + Tag.TAGNAME + ")";
    private static final String SORT_BY_USAGE = "(SELECT COUNT(*) FROM TAGLINK_V1 WHERE TAG_V1.TAGID = TAGLINK_V1.TAGID ) DESC";

    private Context mContext;
    private String mCurFilter;
    private int mSort = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity();

        setSearchMenuVisible(true);
        // Focus on search menu if set in preferences.
        AppSettings settings = new AppSettings(mContext);
        boolean focusOnSearch = settings.getBehaviourSettings().getFilterInSelectors();
        setMenuItemSearchIconified(!focusOnSearch);

        setEmptyText(getActivity().getResources().getString(R.string.tag_empty_list));
        setHasOptionsMenu(true);

        int layout = android.R.layout.simple_list_item_1;

        // associate adapter
        MoneySimpleCursorAdapter adapter = new MoneySimpleCursorAdapter(getActivity(),
                layout, null, new String[] { Tag.TAGNAME },
                new int[]{android.R.id.text1}, 0);
        // set adapter
        setListAdapter(adapter);

        registerForContextMenu(getListView());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        setListShown(false);
        mSort = 0;

        // start loader
        getLoaderManager().initLoader(ID_LOADER_TAG, null, this);

        // set floating button visible
        setFloatingActionButtonVisible(true);
        attachFloatingActionButtonToListView();
    }

    // Menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_sort, menu);

        int sort = 0;
        
        //Check the default sort order
        final MenuItem item;
        // PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(getString(PreferenceConstants.PREF_SORT_tag), 0)
        switch (sort) {
            case 0:
                item = menu.findItem(R.id.menu_sort_name);
                item.setChecked(true);
                break;
            case 1:
                item = menu.findItem(R.id.menu_sort_usage);
                item.setChecked(true);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_sort_name:
                mSort = 0;
                item.setChecked(true);
                // restart search
                restartLoader();
                return true;

            case R.id.menu_sort_usage:
                mSort = 1;
                item.setChecked(true);
                // restart search
                restartLoader();
                return true;

            case android.R.id.home:
                getActivity().setResult(TagActivity.RESULT_CANCELED);
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Context Menu

    @SuppressLint("Range")
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
        cursor.moveToPosition(info.position);
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(Tag.TAGNAME)));

        menu.add(Menu.NONE, ContextMenuIds.EDIT.getId(), Menu.NONE, getString(R.string.edit));
        menu.add(Menu.NONE, ContextMenuIds.DELETE.getId(), Menu.NONE, getString(R.string.delete));
        menu.add(Menu.NONE, ContextMenuIds.VIEW_TRANSACTIONS.getId(), Menu.NONE, getString(R.string.view_transactions));
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = null;
        if (item.getMenuInfo() instanceof AdapterView.AdapterContextMenuInfo) {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } else {
            return false;
        }
//        if (item.getMenuInfo() instanceof ExpandableListView.ExpandableListContextMenuInfo) {
//            info = item.getMenuInfo();
//        }

        Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
        cursor.moveToPosition(info.position);

        // Read values from cursor.
        Tag tag = new Tag();
        tag.loadFromCursor(cursor);

        ContextMenuIds menuId = ContextMenuIds.get(item.getItemId());
        if (menuId == null) return false;

        switch (menuId) {
            case EDIT:
                showDialogEdittagName(SQLTypeTransaction.UPDATE, tag.getId(), tag.getName());
                break;

            case DELETE:
                TagService service = new TagService(getActivity());
                if (!service.isUsed(tag.getId())) {
                    showDialogDeletetag(tag.getId());
                } else {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.attention)
                            .setIcon(new UIHelper(getActivity()).getIcon(GoogleMaterial.Icon.gmd_warning))
                            .setMessage(R.string.tag_can_not_deleted)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                            .create()
                            .show();
                }
                break;

            case VIEW_TRANSACTIONS:
                SearchParameters parameters = new SearchParameters();
                parameters.tagId = tag.getId();
                parameters.tagName = tag.getName();
                Intent intent = IntentFactory.getSearchIntent(getActivity(), parameters);
                startActivity(intent);
        }
        return false;
    }

    // Loader

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == ID_LOADER_TAG) {
            String whereClause = "ACTIVE = 1";
            String[] selectionArgs = null;
            if (!TextUtils.isEmpty(mCurFilter)) {
                whereClause += " AND " + Tag.TAGNAME + " LIKE ?";
                selectionArgs = new String[]{mCurFilter + '%'};
            }
            TagRepository repo = new TagRepository(getActivity());
            Select query = new Select(repo.getAllColumns())
                    .where(whereClause, selectionArgs)
                    .orderBy(mSort == 1 ? SORT_BY_USAGE : SORT_BY_NAME);

            return new MmxCursorLoader(getActivity(), repo.getUri(), query);
        }

        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == ID_LOADER_TAG) {
            MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
//                adapter.swapCursor(null);
            adapter.changeCursor(null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) return;

        if (loader.getId() == ID_LOADER_TAG) {
            MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
            String highlightFilter = mCurFilter != null
                    ? mCurFilter.replace("%", "")
                    : "";
            adapter.setHighlightFilter(highlightFilter);
//                adapter.swapCursor(data);
            adapter.changeCursor(data);

            if (isResumed()) {
                setListShown(true);
                if (data.getCount() <= 0 && getFloatingActionButton() != null) {
                    getFloatingActionButton().show(true);
                }
            } else {
                setListShownNoAnimation(true);
            }
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        restartLoader();
        return true;
    }

    @Override
    protected void setResult() {
        if (Intent.ACTION_PICK.equals(mAction)) {
            // Cursor that is already in the desired position, because positioned in the event onListItemClick
            Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
            @SuppressLint("Range") long tagId = cursor.getLong(cursor.getColumnIndex(Tag.TAGID));
            @SuppressLint("Range") String tagName = cursor.getString(cursor.getColumnIndex(Tag.TAGNAME));

            sendResultToActivity(tagId, tagName);

            return;
        }

        getActivity().setResult(TagActivity.RESULT_CANCELED);
    }

    private void sendResultToActivity(long tagId, String tagName) {
        Intent result = new Intent();
        result.putExtra(TagActivity.INTENT_RESULT_TAGID, tagId);
        result.putExtra(TagActivity.INTENT_RESULT_TAGNAME, tagName);

        getActivity().setResult(AppCompatActivity.RESULT_OK, result);

        getActivity().finish();
    }

    private void showDialogDeletetag(final long tagId) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.delete_tag)
                .setIcon(new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_warning))
                .setMessage(R.string.confirmDelete)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TagRepository repo = new TagRepository(getActivity());
                        boolean success = repo.delete(tagId);
                        if (success) {
                            Toast.makeText(getActivity(), R.string.delete_success, Toast.LENGTH_SHORT).show();
                        }
                        restartLoader();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    private void showDialogEdittagName(final SQLTypeTransaction type, final long tagId, final String tagName) {
        View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_edit_tag, null);
        final EditText edtTagName = viewDialog.findViewById(R.id.editTextTagName);

        edtTagName.setText(tagName);
        if (!TextUtils.isEmpty(tagName)) {
            edtTagName.setSelection(tagName.length());
        }

        UIHelper ui = new UIHelper(getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(viewDialog);

        builder.setIcon(ui.getIcon(GoogleMaterial.Icon.gmd_person))
                .setTitle(R.string.edit_tagName)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // take tag name from the input field.
                        String name = edtTagName.getText().toString();

                        // issue #2030: PC version does not support space
                        if (name.contains(" ") || name.contains("&") || name.contains("|")) {
                            name = name.replaceAll("[ &|]", "_");
                            Toast.makeText(getContext(), R.string.space_replaced_with__,Toast.LENGTH_LONG).show();
                        }

                        TagService service = new TagService(mContext);

                        // check if action is update or insert
                        switch (type) {
                            case INSERT:
                                Tag tag = service.createNew(name);
                                if (tag != null) {
                                    // Created a new tag. But only if picking a tag for another activity.
                                    if (mAction.equalsIgnoreCase(Intent.ACTION_PICK)) {
                                        // Select it and close.
                                        sendResultToActivity(tag.getId(), name);
                                        return;
                                    }
                                } else {
                                    // error inserting.
                                    Toast.makeText(mContext, R.string.db_insert_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case UPDATE:
                                long updateResult = service.update(tagId, name);
                                if (updateResult <= 0) {
                                    Toast.makeText(mContext, R.string.db_update_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case DELETE:
                                break;
                            default:
                                break;
                        }
                        // restart loader
                        restartLoader();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Create and show the AlertDialog
        builder.create().show();
    }

    @Override
    public String getSubTitle() {
        return getString(R.string.tag);
    }

    @Override
    public void onFloatingActionButtonClicked() {
        String tagSearch = !TextUtils.isEmpty(mCurFilter) ? mCurFilter.replace("%", "") : "";
        showDialogEdittagName(SQLTypeTransaction.INSERT, 0, tagSearch);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // On select go back to the calling activity (if there is one)
        if (getActivity().getCallingActivity() != null) {
            Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
            if (cursor != null) {
                if (cursor.moveToPosition(position)) {
                    setResultAndFinish();
                }
            }
        } else {
            // No calling activity, this is the independent tags view. Show context menu.
            getActivity().openContextMenu(v);
        }
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(ID_LOADER_TAG, null, this);
    }


}
