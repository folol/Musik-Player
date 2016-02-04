package com;

import android.content.Context;
import android.widget.MediaController;

/**
 * Created by sky on 4/30/2015.
 */
public class MusicController extends MediaController {              //MediaController contains controls like play/pause/rewind etc(it is the widget that open when u click a songfragment_adapter).

    public MusicController(Context c){
        super(c);
    }

    public void hide(){}                                           //remove the controller from screen
}
