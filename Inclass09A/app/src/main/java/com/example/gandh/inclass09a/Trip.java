package com.example.gandh.inclass09a;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gandh on 4/12/2017.
 */

public class Trip {

    String title, place, image_url, owner_uid,unique;
    Map<String,String> members = new HashMap<>();
    Map<String,Chat> chat_list = new HashMap<>();
    boolean closed;
    Map<String,String> trip_closed_userlist = new HashMap<>();
    Map<String,String> trip_place_list = new HashMap<>();

    public Trip() {
    }

    boolean notnull()
    {
        if(title!=null && place !=null && image_url!=null&& owner_uid!=null&& members!=null)
        {
            return  true;
        }
        return  false;
    }
}
