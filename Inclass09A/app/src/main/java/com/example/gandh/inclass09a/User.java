package com.example.gandh.inclass09a;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gandh on 4/12/2017.
 */

public class User  {
    public User() {
    }

    String first_name, last_name,image_url,uuid,email;
    Boolean gender = false, image= false;

    public User(String first_name, String last_number, String email) {
        this.first_name = first_name;
        this.last_name = last_number;
        this.email = email;

    }
    Map<String, Integer> relationship = new HashMap<>();


    Map<String,Object> list ()
    {
        Map<String, Object> mapper = new HashMap<>();
        mapper.put("first_name",first_name);
        mapper.put("last_name",last_name);
        mapper.put("gender",gender);
        mapper.put("image",image);
        mapper.put("uuid",uuid);
        mapper.put("relationship",relationship);


        return  mapper;
    }
}
