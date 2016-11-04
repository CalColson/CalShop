package com.example.cal.calshop.ui.activeListDetails;

import android.app.Dialog;
import android.os.Bundle;

import com.example.cal.calshop.R;
import com.example.cal.calshop.model.Item;
import com.example.cal.calshop.model.ShoppingList;
import com.example.cal.calshop.utils.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

/**
 * Lets user add new list item.
 */
public class AddListItemDialogFragment extends EditListDialogFragment {

    private String mListId;

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static AddListItemDialogFragment newInstance(ShoppingList shoppingList, String listId) {
        AddListItemDialogFragment addListItemDialogFragment = new AddListItemDialogFragment();

        Bundle bundle = newInstanceHelper(shoppingList, R.layout.dialog_add_item);
        bundle.putString(Constants.KEY_LIST_ID, listId);
        addListItemDialogFragment.setArguments(bundle);

        return addListItemDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListId = getArguments().getString(Constants.KEY_LIST_ID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         **/
        return super.createDialogHelper(R.string.positive_button_add_list_item);
    }

    /**
     * Adds new item to the current shopping list
     */
    @Override
    protected void doListEdit() {
        //Location where the item will be pushed under (root > items > id for list)
        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference().child(
                Constants.KEY_ITEMS).child(mListId);
        DatabaseReference listRef = Constants.FIREBASE_LOCATION_ACTIVE_LISTS.child(mListId);

        String owner = Constants.DEFAULT_OWNER;
        Item item = new Item(mEditTextForList.getText().toString(), owner);

        if (!item.getItemName().equals("")) {
            itemRef.push().setValue(item);
            HashMap<String, Object> dateLastChanged = new HashMap<>();
            dateLastChanged.put(Constants.KEY_DATE, ServerValue.TIMESTAMP);
            listRef.child(Constants.KEY_TIMESTAMP_LAST_CHANGED).setValue(dateLastChanged);

        }
    }
}
