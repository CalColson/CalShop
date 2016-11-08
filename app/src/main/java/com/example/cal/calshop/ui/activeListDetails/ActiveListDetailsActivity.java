package com.example.cal.calshop.ui.activeListDetails;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.cal.calshop.R;
import com.example.cal.calshop.model.Item;
import com.example.cal.calshop.model.ShoppingList;
import com.example.cal.calshop.model.User;
import com.example.cal.calshop.ui.BaseActivity;
import com.example.cal.calshop.utils.Constants;
import com.example.cal.calshop.utils.Utils;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private String mListOwner;
    private ShoppingList mShoppingList;
    private FirebaseListAdapter<Item> mAdapter;
    private ValueEventListener mActiveListRefListener;

    private Button mShoppingButton;

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

        mActiveListRefListener = mActiveListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mShoppingList = dataSnapshot.getValue(ShoppingList.class);

                /* Calling invalidateOptionsMenu causes onCreateOptionsMenu to be called */
                invalidateOptionsMenu();
                if (mShoppingList != null) {
                    mListOwner = mShoppingList.getOwner();
                    setTitle(mShoppingList.getListName());
                } else {
                    finish();
                    return;
                }
                initializeScreen();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
        if (mListOwner.equals(Utils.getCurrentUserEmail(this))) {
            remove.setVisible(true);
            edit.setVisible(true);
        } else {
            remove.setVisible(false);
            edit.setVisible(false);
        }

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

        HashMap<String, User> usersShoppingMap = mShoppingList.getUsersShopping();
        if (usersShoppingMap == null) usersShoppingMap = new HashMap<String, User>();
        String shoppingUsers = getShoppingUsersString(usersShoppingMap);

        if (usersShoppingMap.containsKey(Utils.encodeEmail(Utils.getCurrentUserEmail(this))) ) {
            mShoppingList.setShopping(true);
        }
        else mShoppingList.setShopping(false);
        Constants.FIREBASE_LOCATION_ACTIVE_LISTS.child(mListId)
                .child(Constants.KEY_LIST_IS_SHOPPING).setValue(mShoppingList.isShopping());

        mShoppingButton = (Button) findViewById(R.id.button_shopping);
        if (!mShoppingList.isShopping()) {
            mShoppingButton.setText(R.string.button_start_shopping);
            mShoppingButton.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.primary_dark));
        } else {
            mShoppingButton.setText(R.string.button_stop_shopping);
            mShoppingButton.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.dark_grey));
        }
        TextView shoppingUsersTV = (TextView) findViewById(R.id.text_view_people_shopping);
        shoppingUsersTV.setText(shoppingUsers);


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
                TextView boughtByTV = (TextView) v.findViewById(R.id.text_view_bought_by);
                ImageButton removeButton = (ImageButton) v.findViewById(R.id.button_remove_item);

                itemName.setText(model.getItemName());
                if (model.getIsBought()) {
                    itemName.setPaintFlags(itemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    ownerName.setText(model.getOwner());
                    boughtByTV.setVisibility(View.VISIBLE);
                    removeButton.setVisibility(View.INVISIBLE);
                } else {
                    itemName.setPaintFlags(itemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    ownerName.setText("");
                    boughtByTV.setVisibility(View.INVISIBLE);
                    removeButton.setVisibility(View.VISIBLE);
                }

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
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mShoppingList.isShopping()) {
                    String itemId = mAdapter.getRef(i).getKey();
                    DatabaseReference itemRef = Constants.FIREBASE_LOCATION_ACTIVE_ITEMS.child(mListId)
                            .child(itemId);

                    TextView boughtByUserTV = (TextView) view.findViewById(R.id.text_view_bought_by_user);
                    // item has not been bought
                    if (boughtByUserTV.getText().length() == 0) {
                        itemRef.child(Constants.KEY_ITEM_BOUGHT_STATUS).setValue(true);
                    }
                    // item has been bought
                    else {
                        itemRef.child(Constants.KEY_ITEM_BOUGHT_STATUS).setValue(false);
                    }
                }
            }
        });

        /* Inflate the footer, set root layout to null*/
        View footer = getLayoutInflater().inflate(R.layout.footer_empty, null);
        mListView.addFooterView(footer);
    }

    private String getShoppingUsersString(HashMap<String, User> usersShoppingMap) {
        int numUsersShopping = usersShoppingMap.size();
        String shoppingUsers = "default";
        if (!mShoppingList.isShopping()) {

            if (numUsersShopping == 0) shoppingUsers = "";
            else if (numUsersShopping == 1) {
                for (User user : usersShoppingMap.values()) {
                    shoppingUsers = getString(R.string.text_other_is_shopping, user.getName());
                }
            }
            else if (numUsersShopping == 2) {
                String[] arr = new String[2];
                int i = 0;
                for (User user : usersShoppingMap.values()) {
                    arr[i] = user.getName();
                    i++;
                }
                shoppingUsers = getString(R.string.text_other_and_other_are_shopping, arr[0], arr[1]);
            }
            else {
                String firstNameInList = "default";
                for (User user : usersShoppingMap.values()) {
                    firstNameInList = user.getName();
                    break;
                }
                shoppingUsers = getString(R.string.text_other_and_number_are_shopping, firstNameInList,
                        numUsersShopping - 1);
            }
        } else {
            if (numUsersShopping == 1) shoppingUsers = getString(R.string.text_you_are_shopping);
            else if (numUsersShopping == 2) {
                String otherNameInList = "default";
                for (User user : usersShoppingMap.values()) {
                    if (!user.getName().equals(Utils.getCurrentUserName(this))) {
                        Log.v(LOG_TAG, "currentUserName is: " + Utils.getCurrentUserName(this));
                        Log.v(LOG_TAG, "otherName is: " + user.getName());
                        otherNameInList = user.getName();
                    }
                }
                shoppingUsers = getString(R.string.text_you_and_other_are_shopping, otherNameInList);
            }
            else {
                shoppingUsers = getString(R.string.text_you_and_number_are_shopping, numUsersShopping - 1);
            }
        }

        return shoppingUsers;
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
        boolean isShopping = mShoppingList.isShopping();
        mShoppingList.setShopping(!isShopping);
        isShopping = !isShopping;

        if (mShoppingList.getUsersShopping() == null) {
            mShoppingList.setUsersShopping(new HashMap<String, User>());
        }

        String userEmail = Utils.getCurrentUserEmail(this);
        String encodedEmail = Utils.encodeEmail(userEmail);
        if (isShopping) {
            mShoppingButton.setText(R.string.button_stop_shopping);
            mShoppingButton.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.dark_grey));

            String userName = Utils.getCurrentUserName(this);
            mShoppingList.getUsersShopping().put(encodedEmail, new User(userEmail, userName));
        } else {
            mShoppingButton.setText(R.string.button_start_shopping);
            mShoppingButton.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.primary_dark));

            mShoppingList.getUsersShopping().remove(encodedEmail);
        }
        DatabaseReference listRef = Constants.FIREBASE_LOCATION_ACTIVE_LISTS.child(mListId);

        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put(Constants.KEY_LIST_USERS_SHOPPING, mShoppingList.getUsersShopping());
        updateMap.put(Constants.KEY_LIST_IS_SHOPPING, mShoppingList.isShopping());
        listRef.updateChildren(updateMap);

        String shoppingUsers = getShoppingUsersString(mShoppingList.getUsersShopping());

        TextView shoppingUsersTV = (TextView) findViewById(R.id.text_view_people_shopping);
        shoppingUsersTV.setText(shoppingUsers);
    }
}
