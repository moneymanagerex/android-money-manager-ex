package com.money.manager.ex.checkingaccount;

import android.app.Activity;
import android.app.AlertDialog;
import android.support.v4.app.DialogFragment;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 *
 */
public class YesNoDialog extends DialogFragment {
    public static final String PURPOSE_DELETE_SPLITS_WHEN_SWITCHING_TO_TRANSFER = "delete-splits";

    public YesNoDialog() {

    }

    /**
     * Here we store the identifier in which context the dialog is used.
     * Since this dialog can be used for any binary outcome, there needs to be a way
     * to distinguish which one it is handling.
     * This is used in the caller to distinguish which action to take in case there are
     * multiple instances of yes-no dialog.
     * If there is only one then it does not need to be used.
     */
    private String mPurpose;
    private YesNoDialogListener mListener;

    public String getPurpose() {
        return mPurpose;
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (YesNoDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle args = getArguments();
        String title = args.getString("title", "");
        String message = args.getString("message", "");
        this.mPurpose = args.getString("purpose", "");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
//                        getActivity().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
//                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
                        mListener.onDialogPositiveClick(YesNoDialog.this);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
//                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
                        mListener.onDialogNegativeClick(YesNoDialog.this);
                    }
                })
                .create();
    }
}
