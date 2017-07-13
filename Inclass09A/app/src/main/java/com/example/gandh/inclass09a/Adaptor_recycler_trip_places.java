package com.example.gandh.inclass09a;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ocpsoft.pretty.time.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by gandh on 4/5/2017.
 */

public class Adaptor_recycler_trip_places extends RecyclerView.Adapter {

    Map<String,String> trip_placelist = new LinkedHashMap<>();
    Context context;
    Trip trip;
    fromadaptor intf;
    View v;
    String[] latlong = new String[2],temp_color = new String[2];
    String uid,uri;
    int clicked =0;
    DatabaseReference mDatabase;

    boolean discover_b;
    PrettyTime pt;
    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Adaptor_recycler_trip_places(Context context, Map<String,String> five_day_fcast, fromadaptor intf){
        this.trip_placelist = five_day_fcast;
        this.intf = intf;
        this.context = context;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("inclass09");
    }

    interface fromadaptor{
        void edit_place(String title);
        void delete_place(String title);
        void change_position_place(String uri);
        void do_nothing();
    }

    class View_holder extends RecyclerView.ViewHolder{

        View v;
        TextView t1;
        ImageView iv,edit;

        CardView card;
        public View_holder(View itemView) throws ParseException {
            super(itemView);
            v = itemView;
            t1 = (TextView) v.findViewById(R.id.textView5);
            iv = (ImageView) v.findViewById(R.id.imageView17);
            edit = (ImageView) v.findViewById(R.id.imageView20);
            card = (CardView) v.findViewById(R.id.c1);
            iv.setVisibility(View.INVISIBLE);
            edit.setVisibility(View.INVISIBLE);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        v = inflater.inflate(R.layout.activity_adaptor_recycler_trip_places, parent, false);

        View_holder holder = null;
        try {
            holder = new View_holder(v);


        } catch (ParseException e) {
            e.printStackTrace();
        }
        return holder;
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,final int position) {
        final View_holder view_holder = (View_holder) holder;
        view_holder.t1.setText((String) trip_placelist.keySet().toArray()[position]);

        view_holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(view_holder.iv.isEnabled()){
                    view_holder.v.setBackgroundColor(0xffffffff);
                    view_holder.iv.setVisibility(View.INVISIBLE);
                    view_holder.edit.setVisibility(View.INVISIBLE);
                    view_holder.iv.setEnabled(false);
                    intf.do_nothing();
                }
                else
                {
                    view_holder.v.setBackgroundColor(0xffffffff);
                    view_holder.iv.setEnabled(true);
                    view_holder.iv.setVisibility(View.VISIBLE);
                    view_holder.edit.setVisibility(View.VISIBLE);
                    intf.do_nothing();
                }
            }
        });
        view_holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intf.edit_place((String) trip_placelist.keySet().toArray()[position]);
            }
        });
        view_holder.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intf.delete_place((String) trip_placelist.keySet().toArray()[position]);
            }
        });

//        view_holder.v.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                clicked++;
//                if(clicked==1)
//                {
//
//                    latlong = ((String) trip_placelist.values().toArray()[position]).replace("lat/lng: (", "").replace(")", "").split(",");
//                    view_holder.v.setBackgroundColor(0xFF00FF00);
//                    uri = null;
//                    temp_color[0] = (String) trip_placelist.keySet().toArray()[position];
//                }
//
//                else if (clicked==2)
//                {
//                    String[] latlong2 = ((String) trip_placelist.values().toArray()[position]).replace("lat/lng: (", "").replace(")", "").split(",");
//                    uri = "http://maps.google.com/maps?saddr=" + latlong[0] + "," + latlong[1] + "&daddr=" + latlong2[0] + "," +latlong2[1];
//                    latlong = null;
//                    clicked=0;
//                    view_holder.v.setBackgroundColor(0xFF00FF00);
//                    temp_color[1] = (String) trip_placelist.keySet().toArray()[position];
//                    intf.change_position_place(uri);
//                }
//                return false;
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return trip_placelist.size();
    }
}
