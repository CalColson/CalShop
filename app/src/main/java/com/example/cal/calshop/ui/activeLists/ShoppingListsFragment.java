package com.example.cal.calshop.ui.activeLists;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.cal.calshop.R;
import com.example.cal.calshop.model.ShoppingList;
import com.example.cal.calshop.ui.activeListDetails.ActiveListDetailsActivity;
import com.example.cal.calshop.utils.Constants;
import com.example.cal.calshop.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;


/**
 * A simple {@link Fragment} subclass that shows a list of all shopping lists a user can see.
 * Use the {@link ShoppingListsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingListsFragment extends Fragment {
    private final String LOG_TAG = ShoppingListsFragment.class.getSimpleName();
    private ListView mListView;
    private TextView mTextViewListName;
    private TextView mTextViewOwnerName;
    private TextView mTextViewDate;

    public ShoppingListsFragment() {
        /* Required empty public constructor */
    }

    /**
     * Create fragment and pass bundle with data as it's arguments
     * Right now there are not arguments...but eventually there will be.
     */
    public static ShoppingListsFragment newInstance() {
        ShoppingListsFragment fragment = new ShoppingListsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /**
         * Initalize UI elements
         */
        View rootView = inflater.inflate(R.layout.fragment_shopping_lists, container, false);
        initializeScreen(rootView);

        final DatabaseReference listNameRef = Constants.FIREBASE_LOCATION_ACTIVE_LISTS;
        listNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ShoppingList sList = dataSnapshot.child("-KVb4U0YCjJAkBxGG1JA").getValue(ShoppingList.class);
                String listName = sList.getListName();
                String owner = sList.getOwner();
                long dateLastChanged = (long) sList.getDateLastChanged().get(Constants.KEY_DATE);
                Date date = new Date(dateLastChanged);
                String dateString = Utils.SIMPLE_DATE_FORMAT.format(date);

                mTextViewListName.setText(listName);
                mTextViewOwnerName.setText(owner);
                mTextViewDate.setText(dateString);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mTextViewListName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActiveListDetailsActivity.class);
//                TextView textView = (TextView) view;
//                String title = textView.getText().toString();
//                intent.putExtra(Intent.EXTRA_TEXT, title);
                startActivity(intent);
            }
        });

        /**
         * Set interactive bits, such as click events and adapters
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * Link layout elements from XML
     */
    private void initializeScreen(View rootView) {
        mListView = (ListView) rootView.findViewById(R.id.list_view_active_lists);
        mTextViewListName = (TextView) rootView.findViewById(R.id.text_view_list_name);
        mTextViewOwnerName = (TextView) rootView.findViewById(R.id.text_view_created_by_user);
        mTextViewDate = (TextView) rootView.findViewById(R.id.text_view_edit_time);
    }
}
