package com.example.gandh.inclass09a;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ocpsoft.pretty.time.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


/**
 * Created by gandh on 4/5/2017.
 */

public class Adaptor_recycler extends RecyclerView.Adapter {
    int viewtype;
    ArrayList<Trip> triplist = new ArrayList<>();
    Context context;
    fromadaptor intf;
    View v;
    boolean discover_b;



    Adaptor_recycler(Context context, ArrayList<Trip> five_day_fcast, fromadaptor intf, boolean discover_b){
        this.triplist = five_day_fcast;
            this.intf = intf;
        this.context = context;
        this.discover_b = discover_b;

    }

    interface fromadaptor{

        void trip_adder(String unique);
        void trip_delete(String unique);
        void chat_open(String unique);
        void add_ppl_chat(String unique);
        void frnds_to_add_list (Trip trip);
    }

    class View_holder extends RecyclerView.ViewHolder{
        TextView place, title;
        ImageView im1,im2;
        View v;
        StorageReference fs;
        Spinner sp;
        int view_type;
        public View_holder(View itemView) throws ParseException {
            super(itemView);
            v = itemView;
            place = (TextView) v.findViewById(R.id.textView2);
            title = (TextView) v.findViewById(R.id.textView);
            im1 = (ImageView) v.findViewById(R.id.imageView5);
            im2 = (ImageView) v.findViewById(R.id.imageView13);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

             v = inflater.inflate(R.layout.adaptor_trip, parent, false);

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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
           final View_holder view_holder = (View_holder) holder;
            view_holder.place.setText(triplist.get(position).place);
        view_holder.title.setText(triplist.get(position).title);
        view_holder.fs =   FirebaseStorage.getInstance().getReferenceFromUrl(triplist.get(position).image_url);
        if(discover_b)
            view_holder.im2.setVisibility(View.INVISIBLE);

        Glide.with(context )
                .using(new FirebaseImageLoader())
                .load(view_holder.fs)
                .into(view_holder.im1);
        if(discover_b){
            view_holder.v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    intf.trip_adder(triplist.get(position).unique);
                    return false;
                }
            });}
            else{
            view_holder.v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intf.chat_open(triplist.get(position).unique);
                }
            });
        view_holder.v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                        intf.trip_delete(triplist.get(position).unique);
                        return false;
            }
        });
        }
        view_holder.im2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

             intf.frnds_to_add_list(triplist.get(position));

            }
        });

    }

    @Override
    public int getItemCount() {
        return triplist.size();
    }
}
