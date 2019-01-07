package com.example.android.simpleplayer;

import android.content.Context;
import android.widget.MediaController;

public class MusicController extends MediaController {


        public MusicController(Context c){
            super(c);
        }
//stop MusicController class from automatically hiding after three seconds by overriding the hide method.
        public void hide(){}

    }
