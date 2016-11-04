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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lets user edit list item name for all copies of the current list
 */
public class EditListItemNameDialogFragment extends EditListDialogFragment {

    private String mListId;
    private String mItemId;
    private String mItemName;

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static EditListItemNameDialogFragment newInstance(ShoppingList shoppingList, String itemName,
                                                             String listId, String itemId) {
        EditListItemNameDialogFragment editListItemNameDialogFragment = new EditListItemNameDialogFragment();
        Bundle bundle = EditListDialogFragment.newInstanceHelper(shoppingList, R.layout.dialog_edit_item);
        bundle.putString(Constants.KEY_LIST_ID, listId);
        bundle.putString(Constants.KEY_ITEM_ID, itemId);
        bundle.putString(Constants.KEY_ITEM_NAME, itemName);
        editListItemNameDialogFragment.setArguments(bundle);

        return editListItemNameDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListId = getArguments().getString(Constants.KEY_LIST_ID);
        mItemId = getArguments().getString(Constants.KEY_ITEM_ID);
        mItemName = getArguments().getString(Constants.KEY_ITEM_NAME);
    }


    @Override

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         */

        Dialog dialog = super.createDialogHelper(R.string.positive_button_edit_item);
        helpSetDefaultValueEditText(mItemName);

        return dialog;
    }

    /**
     * Change selected list item name to the editText input if it is not empty
     */
    protected void doListEdit() {
        String editText = mEditTextForList.getText().toString();
        if (!editText.equals(mItemName) && !editText.equals("")) {

            DatabaseReference listTimeStampRef = Constants.FIREBASE_LOCATION_ACTIVE_LISTS.child(mListId)
                    .child(Constants.KEY_TIMESTAMP_LAST_CHANGED);
            DatabaseReference itemNameRef = Constants.FIREBASE_LOCATION_ACTIVE_ITEMS.child(mListId)
                    .child(mItemId).child(Constants.KEY_ITEM_NAME);
            String firebaseUrl = Constants.FIREBASE_URL;
            String timeStampUrl = listTimeStampRef.toString().replaceAll(firebaseUrl, "");
            String itemNameUrl = itemNameRef.toString().replaceAll(firebaseUrl, "");

            //Log.v("doListEdit: ", timeStampUrl);
            //Log.v("doListEdit: ", itemNameUrl);


            HashMap<String, Object> dateLastChanged = new HashMap<>();
            dateLastChanged.put(Constants.KEY_DATE, ServerValue.TIMESTAMP);

            HashMap<String, Object> updateMap = new HashMap<>();
            updateMap.put(timeStampUrl, dateLastChanged);
            updateMap.put(itemNameUrl, mEditTextForList.getText().toString());

            FirebaseDatabase.getInstance().getReference().updateChildren(updateMap);
        }
    }
}
