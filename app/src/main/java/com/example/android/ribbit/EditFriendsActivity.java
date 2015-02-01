package com.example.android.ribbit;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class EditFriendsActivity extends ListActivity {

    private List<ParseUser> users;
    private ParseUser mCurrentUser;
    private ParseRelation<ParseUser> friendsRelation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_friends);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        friendsRelation = mCurrentUser.getRelation("friendsRelation");

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByAscending("username");
        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if(e == null){
                    users = parseUsers;
                    String[] usernames = new String[parseUsers.size()];
                    int i =0;
                    for(ParseUser user : parseUsers){
                        usernames[i] = user.getUsername();
                        i++;

                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditFriendsActivity.this,
                            android.R.layout.simple_list_item_checked, usernames);
                    setListAdapter(adapter);
                    addCheckMarks();
                }
                else{
                    Log.i(EditFriendsActivity.class.getSimpleName(), "Parse query error");
                }
            }
        });
    }

    private void addCheckMarks() {
        friendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if(e == null){
                    for(int i = 0; i < users.size(); i++){
                        ParseUser user = users.get(i);
                        for(ParseUser user2 : parseUsers){
                            if(user2.getObjectId().equals(user.getObjectId())){
                                getListView().setItemChecked(i, true);
                            }
                        }
                    }
                }
                else{
                    Log.i(EditFriendsActivity.class.getSimpleName(), "AddCheckmarks error");
                }
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if(getListView().isItemChecked(position)){
            friendsRelation.add(users.get(position));
        }
        else{
           friendsRelation.remove(users.get(position));
        }
        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Log.i(EditFriendsActivity.class.getSimpleName(), "saving new friend error");
                }
            }
        });

    }
}
