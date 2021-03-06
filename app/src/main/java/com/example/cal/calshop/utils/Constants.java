package com.example.cal.calshop.utils;

import com.example.cal.calshop.BuildConfig;
import com.example.cal.calshop.ui.activeLists.AddListDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Constants class store most important strings and paths of the app
 */
public final class Constants {

    public final static String KEY_LISTS = "activeLists";
    public final static String KEY_LIST_NAME = "listName";
    public final static String KEY_LIST_IS_SHOPPING = "shopping";
    public static final String KEY_LIST_USERS_SHOPPING = "usersShopping";
    public final static String KEY_TIMESTAMP_LAST_CHANGED = "dateLastChanged";
    public final static String KEY_DATE = "date";
    public final static String KEY_LIST_ID = "listId";

    public final static String KEY_ITEMS = "shoppingListItems";
    public final static String KEY_ITEM_NAME = "itemName";
    public final static String KEY_ITEM_BOUGHT_STATUS = "isBought";
    //public final static String KEY_ITEM_BOUGHT_TRUE = "true";
    //public final static String KEY_ITEM_BOUGHT_FALSE = "false";
    public final static String KEY_ITEM_ID = "itemId";

    public final static String KEY_USERS = "appUsers";
    public final static String DEFAULT_OWNER = "Calvin";

    /**
     * Constants for Firebase URL
     */
    public static final String FIREBASE_URL = BuildConfig.UNIQUE_FIREBASE_ROOT_URL;

    /**
     * Constants related to locations in Firebase, such as the name of the node
     * where active lists are stored (ie "activeLists")
     */
    public static DatabaseReference FIREBASE_LOCATION_ACTIVE_LISTS = FirebaseDatabase.getInstance().getReference()
            .child(KEY_LISTS);
    public static DatabaseReference FIREBASE_LOCATION_ACTIVE_ITEMS = FirebaseDatabase.getInstance().getReference()
            .child(KEY_ITEMS);
    public static DatabaseReference FIREBASE_LOCATION_ACTIVE_USERS = FirebaseDatabase.getInstance().getReference()
            .child(KEY_USERS);
    public static final String FIREBASE_URL_ACTIVE_LIST = FIREBASE_URL + "activeList/";



    /**
     * Constants for Firebase object properties
     */




    /**
     * Constants for bundles, extras and shared preferences keys
     */
    public static final String KEY_LAYOUT_RESOURCE = "LAYOUT_RESOURCE";

    public static final String PREF_PROVIDER_PASSWORD = "passwordLogin";
    public static final String PREF_PROVIDER_GOOGLE = "googleLogin";

    //Shared preferences keys
    public static final String KEY_PREF_USERNAME = "userName";
    public static final String KEY_PREF_ENCODED_EMAIL = "emailPref";
    public static final String KEY_PREF_PROVIDER = "providerPref";


}
