package com.example.cal.calshop.ui.activeListDetails;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import com.example.cal.calshop.R;
import com.example.cal.calshop.model.ShoppingList;
import com.example.cal.calshop.utils.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Lets user edit the list name for all copies of the current list
 */
public class EditListNameDialogFragment extends EditListDialogFragment {
    private static final String LOG_TAG = ActiveListDetailsActivity.class.getSimpleName();
    private String mListName;
    private String mListId;

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static EditListNameDialogFragment newInstance(ShoppingList shoppingList, String listId) {
        EditListNameDialogFragment editListNameDialogFragment = new EditListNameDialogFragment();
        Bundle bundle = EditListDialogFragment.newInstanceHelper(shoppingList, R.layout.dialog_edit_list);
        bundle.putString(Constants.KEY_LIST_NAME, shoppingList.getListName());
        bundle.putString(Constants.KEY_LIST_ID, listId);
        editListNameDialogFragment.setArguments(bundle);
        return editListNameDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListName = getArguments().getString(Constants.KEY_LIST_NAME);
        mListId = getArguments().getString(Constants.KEY_LIST_ID);
        //Log.v(LOG_TAG, "mListName: " + mListName);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         **/
        Dialog dialog = super.createDialogHelper(R.string.positive_button_edit_item);
        helpSetDefaultValueEditText(mListName);

        return dialog;
    }

    /**
     * Changes the list name in all copies of the current list
     */
    protected void doListEdit() {
        if (!mEditTextForList.getText().equals(mListName) &&
                mEditTextForList.getText().length() > 0) {
            DatabaseReference activeListRef = Constants.FIREBASE_LOCATION_ACTIVE_LISTS.child(mListId);
            HashMap<String, Object> listUpdateProperties = new HashMap<>();
            listUpdateProperties.put(Constants.KEY_LIST_NAME, mEditTextForList.getText().toString());
            HashMap<String, Object> dateMap = new HashMap<>();
            dateMap.put(Constants.KEY_DATE, ServerValue.TIMESTAMP);
            listUpdateProperties.put(Constants.KEY_TIMESTAMP_LAST_CHANGED, dateMap);
            activeListRef.updateChildren(listUpdateProperties);
        }
    }
}

