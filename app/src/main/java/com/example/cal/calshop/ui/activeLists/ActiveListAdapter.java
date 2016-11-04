package com.example.cal.calshop.ui.activeLists;


import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.example.cal.calshop.R;
import com.example.cal.calshop.model.ShoppingList;
import com.example.cal.calshop.utils.Constants;
import com.example.cal.calshop.utils.Utils;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;

import java.util.Date;

/**
 * Populates the list_view_active_lists inside ShoppingListsFragment
 */
public class ActiveListAdapter extends FirebaseListAdapter<ShoppingList> {

    public ActiveListAdapter(Activity activity, Class<ShoppingList> modelClass, int modelLayout, DatabaseReference ref) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
    }

    @Override
    protected void populateView(View v, ShoppingList model, int position) {
        ((TextView) v.findViewById(R.id.text_view_list_name)).setText(model.getListName());
        ((TextView) v.findViewById(R.id.text_view_created_by_user)).setText(model.getOwner());
        long dateLastChanged = (long) model.getDateLastChanged().get(Constants.KEY_DATE);
        Date date = new Date(dateLastChanged);
        String dateString = Utils.SIMPLE_DATE_FORMAT.format(date);
        ((TextView) v.findViewById(R.id.text_view_edit_time)).setText(dateString);
    }
}
