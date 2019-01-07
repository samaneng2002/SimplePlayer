 package com.example.android.simpleplayer;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.widget.MediaController.MediaPlayerControl;
 public class MainActivity extends AppCompatActivity implements MediaPlayerControl {


    private ArrayList<Song> songList;
    private ListView songView;
     private MusicService musicSrv;
     private Intent playIntent;
     private boolean musicBound=false;
     private MusicController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setController();

//        controller = (MusicController) findViewById(R.id.controller);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

// MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
// app-defined int constant

                return;
            }}

        //Instantiate the list as shown below:
        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();

        getSongList();
        //let's sort the data so that the songs are presented alphabetically:
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        //create a new instance of the Adapter class and set it on the ListView:
        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);

        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
//                musicSrv.setSong(Integer.parseInt(songList.get(i).toString()));

                musicSrv.playSong();

            }
        });



    }

    //we will have to bind to the Service class. The above instance variables represent the Service class and Intent, as well as a flag to keep track of whether the Activity class is bound to the Service class or not
     //connect to the service
     private ServiceConnection musicConnection = new ServiceConnection(){

         @Override
         public void onServiceConnected(ComponentName name, IBinder service) {
             MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
             //get service
             musicSrv = binder.getService();
             //pass list
             musicSrv.setList(songList);
             musicBound = true;
         }

         @Override
         public void onServiceDisconnected(ComponentName name) {
             musicBound = false;
         }
     };


     //We will want to start the Service instance when the Activity instance starts, so override the onStart method:
     @Override
     protected void onStart() {
         super.onStart();
         if(playIntent==null){
             playIntent = new Intent(this, MusicService.class);
             bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
             startService(playIntent);
         }
     }


     //helper method to retrieve the audio file information:
     public void getSongList() {
         //retrieve song info by first retrieve the column indexes for the data items that we are interested in for each song, then we use these to create a new Song object and add it to the list, before continuing to loop through the results.
         // create a ContentResolver instance, retrieve the URI for external music files, and create a Cursor instance using the ContentResolver instance to query the music files
         ContentResolver musicResolver = getContentResolver();
         Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
         Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

         //Now we can iterate over the results, first checking that we have valid data:
         if(musicCursor!=null && musicCursor.moveToFirst()){
             //get columns
             int titleColumn = musicCursor.getColumnIndex
                     (android.provider.MediaStore.Audio.Media.TITLE);
             int idColumn = musicCursor.getColumnIndex
                     (android.provider.MediaStore.Audio.Media._ID);
             int artistColumn = musicCursor.getColumnIndex
                     (android.provider.MediaStore.Audio.Media.ARTIST);
             //add songs to list
             do {
                 long thisId = musicCursor.getLong(idColumn);
                 String thisTitle = musicCursor.getString(titleColumn);
                 String thisArtist = musicCursor.getString(artistColumn);
                 songList.add(new Song(thisId, thisTitle, thisArtist));
             }
             while (musicCursor.moveToNext());
         }
     }

     //we added an onClick attribute to the layout for each item in the song list
//     public void songPicked(View view){
//         Toast.makeText(getBaseContext(),"Playing", Toast.LENGTH_SHORT).show();
//         musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
//         musicSrv.playSong();
//     }


     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the Options Menu we specified in XML
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         //menu item selected
         switch (item.getItemId()) {
             case R.id.action_shuffle:
                 //shuffle
                 musicSrv.setShuffle();
                 break;
             case R.id.action_end:
                 stopService(playIntent);
                 musicSrv=null;
                 System.exit(0);
                 break;
         }
         return super.onOptionsItemSelected(item);
     }

     private void setController(){
         //set the controller up
         controller = new MusicController(this);
         controller.setMediaPlayer(this);
         controller.setAnchorView(findViewById(R.id.song_list));
         controller.setEnabled(true);
        //determine what will happen when the user presses the previous/next buttons
         controller.setPrevNextListeners(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 playNext();
             }
         }, new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 playPrev();
             }
         });


//         controller.show(900000000);
//         controller.show();

     }

     //play next
     private void playNext(){
         musicSrv.playNext();
         controller.show(0);
     }

     //play previous
     private void playPrev(){
         musicSrv.playPrev();
         controller.show(0);
     }

     @Override
     protected void onDestroy() {
         stopService(playIntent);
         musicSrv=null;
         super.onDestroy();
     }

     @Override
     public void start() {
         musicSrv.go();

     }

     @Override
     public void pause() {
         musicSrv.pausePlayer();


     }

     @Override
     public int getDuration() {
         return 0;
     }

     @Override
     public int getCurrentPosition() {

         if(musicSrv!=null && musicBound && musicSrv.isPng())
         return musicSrv.getDur();
         else return 0;
     }

     @Override
     public void seekTo(int pos) {
         musicSrv.seek(pos);
     }

     @Override
     public boolean isPlaying() {
         if(musicSrv!=null && musicBound)
         return musicSrv.isPng();
         return false;
     }

     @Override
     public int getBufferPercentage() {
         if(musicSrv!=null && musicBound && musicSrv.isPng())
         return musicSrv.getPosn();
         else return 0;     }

     @Override
     public boolean canPause() {
         return true;
     }

     @Override
     public boolean canSeekBackward() {
         return true;
     }

     @Override
     public boolean canSeekForward() {
         return true;
     }

     @Override
     public int getAudioSessionId() {
         return 0;
     }
 }
