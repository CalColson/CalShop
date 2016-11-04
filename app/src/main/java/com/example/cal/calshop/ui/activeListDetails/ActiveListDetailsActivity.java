package com.example.cal.calshop.ui.activeListDetails;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.cal.calshop.R;
import com.example.cal.calshop.model.Item;
import com.example.cal.calshop.model.ShoppingList;
import com.example.cal.calshop.ui.BaseActivity;
import com.example.cal.calshop.utils.Constants;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.zip.Inflater;

/**
 * Represents the details screen for the selected shopping list
 */
public class ActiveListDetailsActivity extends BaseActivity {
    private static final String LOG_TAG = ActiveListDetailsActivity.class.getSimpleName();
    private DatabaseReference mActiveListRef;

    private ListView mListView;
    private String mListId;
    private ShoppingList mShoppingList;
    private FirebaseListAdapter<Item> mAdapter;
    private ValueEventListener mActiveListRefListener;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_list_details);

        mListId = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (mListId == null) {
            finish();
            return;
        }

        mActiveListRef = Constants.FIREBASE_LOCATION_ACTIVE_LISTS.child(mListId);

        initializeScreen();

        mActiveListRefListener = mActiveListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mShoppingList = dataSnapshot.getValue(ShoppingList.class);

                /* Calling invalidateOptionsMenu causes onCreateOptionsMenu to be called */
                invalidateOptionsMenu();
                if (mShoppingList != null) setTitle(mShoppingList.getListName());
                else {
                    finish();
                    return;
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /* Show edit list item name dialog on listView item long click event */
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                /* Check that the view is not the empty footer item */
                if (view.getId() != R.id.list_view_footer_empty) {
                    String itemId = mAdapter.getRef(position).getKey();
                    String itemName = ((TextView) view.
                            findViewById(R.id.text_view_active_list_item_name)).getText().toString();
                    showEditListItemNameDialog(itemId, itemName);
                }
                return true;
            }
        });

        final DatabaseReference itemsRef = Constants.FIREBASE_LOCATION_ACTIVE_ITEMS.child(mListId);
        mAdapter = new FirebaseListAdapter<Item>(this, Item.class,
                R.layout.single_active_list_item, itemsRef) {
            @Override
            protected void populateView(View v, Item model, final int position) {
                TextView itemName = (TextView) v.findViewById(R.id.text_view_active_list_item_name);
                TextView ownerName = (TextView) v.findViewById(R.id.text_view_bought_by_user);
                ImageButton removeButton = (ImageButton) v.findViewById(R.id.button_remove_item);

                itemName.setText(model.getItemName());
                ownerName.setText(model.getOwner());
                removeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity, R.style.CustomTheme_Dialog)
                                .setTitle(mActivity.getString(R.string.remove_item_option))
                                .setMessage(mActivity.getString(R.string.dialog_message_are_you_sure_remove_item))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        removeItem(position);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                /* Dismiss the dialog */
                                        dialog.dismiss();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert);

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });

            }

            private void removeItem(int position) {
                String itemId = mAdapter.getRef(position).getKey();
                Constants.FIREBASE_LOCATION_ACTIVE_ITEMS.child(mListId).child(itemId).removeValue();

                DatabaseReference listRef = Constants.FIREBASE_LOCATION_ACTIVE_LISTS.child(mListId);

                HashMap<String, Object> dateLastChanged = new HashMap<>();
                dateLastChanged.put(Constants.KEY_DATE, ServerValue.TIMESTAMP);
                listRef.child(Constants.KEY_TIMESTAMP_LAST_CHANGED).setValue(dateLastChanged);
            }
        };
        mListView.setAdapter(mAdapter);

    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_list_details, menu);

        /**
         * Get menu items
         */
        MenuItem remove = menu.findItem(R.id.action_remove_list);
        MenuItem edit = menu.findItem(R.id.action_edit_list_name);
        MenuItem share = menu.findItem(R.id.action_share_list);
        MenuItem archive = menu.findItem(R.id.action_archive);

        /* Only the edit and remove options are implemented */
        remove.setVisible(true);
        edit.setVisible(true);
        share.setVisible(false);
        archive.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /**
         * Show edit list dialog when the edit action is selected
         */
        if (id == R.id.action_edit_list_name) {
            showEditListNameDialog();
            return true;
        }

        /**
         * removeList() when the remove action is selected
         */
        if (id == R.id.action_remove_list) {
            removeList();
            return true;
        }

        /**
         * Eventually we'll add this
         */
        if (id == R.id.action_share_list) {
            return true;
        }

        /**
         * archiveList() when the archive action is selected
         */
        if (id == R.id.action_archive) {
            archiveList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Cleanup when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
        mActiveListRef.removeEventListener(mActiveListRefListener);
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    private void initializeScreen() {

        mListView = (ListView) findViewById(R.id.list_view_shopping_list_items);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        /* Common toolbar setup */
        setSupportActionBar(toolbar);
        /* Add back button to the action bar */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /* Inflate the footer, set root layout to null*/
        View footer = getLayoutInflater().inflate(R.layout.footer_empty, null);
        mListView.addFooterView(footer);
    }


    /**
     * Archive current list when user selects "Archive" menu item
     */
    public void archiveList() {
    }


    /**
     * Start AddItemsFromMealActivity to add meal ingredients into the shopping list
     * when the user taps on "add meal" fab
     */
    public void addMeal(View view) {
    }

    /**
     * Remove current shopping list and its items from all nodes
     */
    public void removeList() {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = RemoveListDialogFragment.newInstance(mShoppingList, mListId);
        dialog.show(getFragmentManager(), "RemoveListDialogFragment");
    }

    /**
     * Show the add list item dialog when user taps "Add list item" fab
     */
    public void showAddListItemDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = AddListItemDialogFragment.newInstance(mShoppingList, mListId);
        dialog.show(getFragmentManager(), "AddListItemDialogFragment");
    }

    /**
     * Show edit list name dialog when user selects "Edit list name" menu item
     */
    public void showEditListNameDialog() {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = EditListNameDialogFragment.newInstance(mShoppingList, mListId);
        dialog.show(this.getFragmentManager(), "EditListNameDialogFragment");
    }

    /**
     * Show the edit list item name dialog after longClick on the particular item
     */
    public void showEditListItemNameDialog(String itemId, String itemName) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = EditListItemNameDialogFragment.newInstance(mShoppingList, itemName, mListId, itemId);
        dialog.show(this.getFragmentManager(), "EditListItemNameDialogFragment");
    }

    /**
     * This method is called when user taps "Start/Stop shopping" button
     */
    public void toggleShopping(View view) {

    }
}
