package com.example.android.ribbit;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.ParseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private static final int TAKE_PIC = 0;
    private static final int TAKE_VID = 1;
    private static final int PICK_PIC = 2;
    private static final int PICK_VID = 3;

    private static final int MEDIATYPE_PIC = 4;
    private static final int MEDIATYPE_VID = 5;

    private Uri mediaUri;

    protected DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case 0:
                    Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mediaUri = getOutputMediaFileUri(MEDIATYPE_PIC);
                    takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
                    startActivityForResult(takePicIntent, TAKE_PIC);
                    break;
                case 1:
                    Intent takeVidIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mediaUri = getOutputMediaFileUri(MEDIATYPE_PIC);
                    takeVidIntent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
                    takeVidIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                    takeVidIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                    startActivityForResult(takeVidIntent, TAKE_VID);
                    break;
                case 2:
                    Intent pickPic = new Intent(Intent.ACTION_GET_CONTENT);
                    pickPic.setType("image/*");
                    startActivityForResult(pickPic, PICK_VID);
                    break;
                case 3:
                    Intent pickVid = new Intent(Intent.ACTION_GET_CONTENT);
                    pickVid.setType("video/*");
                    startActivityForResult(pickVid, PICK_VID);
                    break;
            }
        }

        private Uri getOutputMediaFileUri(int mediatype) {
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "Ribbit");
                // This location works best if you want the created images to be shared
                // between applications and persist after your app has been uninstalled.

                // Create the storage directory if it does not exist
                if (! mediaStorageDir.exists()){
                    if (! mediaStorageDir.mkdirs()){
                        Log.d("Ribbit", "failed to create directory");
                        return null;
                    }
                }

                // Create a media file name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File mediaFile;
                if (mediatype == MEDIATYPE_PIC){
                    mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                            "IMG_"+ timeStamp + ".jpg");
                } else if(mediatype == MEDIATYPE_VID) {
                    mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                            "VID_"+ timeStamp + ".mp4");
                } else {
                    return null;
                }

                return Uri.fromFile(mediaFile);
            }
            else {
                return null;
            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(resultCode == PICK_PIC || resultCode == PICK_VID){
                mediaUri = data.getData();
                if (requestCode == PICK_VID) {
                    // make sure the file is less than 10 MB
                    int fileSize = 0;
                    InputStream inputStream = null;

                    try {
                        inputStream = getContentResolver().openInputStream(mediaUri);
                        fileSize = inputStream.available();
                    }
                    catch (FileNotFoundException e) {
                        Toast.makeText(this, "File not found", Toast.LENGTH_LONG).show();
                        return;
                    }
                    catch (IOException e) {
                        Toast.makeText(this, "Error openning the file", Toast.LENGTH_LONG).show();
                        return;
                    }
                    finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) { /* Intentionally blank */ }
                    }

                    if (fileSize >= 1024 * 1024 * 10) {
                        Toast.makeText(this, "File too large, try file under 10Mb", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            else{
                Intent storeIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                storeIntent.setData(mediaUri);
                sendBroadcast(storeIntent);
            }
        }
        else if(resultCode != RESULT_CANCELED){
            Log.i("Store pic","Error storing pic");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
        } else {
            Log.i(TAG, currentUser.getUsername());
        }

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this,
                getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            ParseUser.logOut();
            navigateToLogin();
        }
        else if (id == R.id.action_edit_friends) {
            Intent intent = new Intent(this, EditFriendsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.action_camera){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(R.array.camera_options, dialogListener);
            Dialog dialog = builder.create();
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }
}
