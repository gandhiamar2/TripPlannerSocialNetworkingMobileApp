package com.example.gandh.inclass09a;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.util.ArrayList;


/**
 * Created by gandh on 4/5/2017.
 */

public class Adaptor_recycler_friends extends RecyclerView.Adapter {
    int viewtype;
    ArrayList<User> uselist = new ArrayList<>();
    Context context;
    fromadaptor intf;
    View v;
    User current_user,user1;



    Adaptor_recycler_friends(Context context, ArrayList<User> five_day_fcast, fromadaptor intf, User current_user){
        this.uselist = five_day_fcast;
            this.intf = intf;
        this.context = context;
        this.current_user = current_user;

    }

    interface fromadaptor{

        void add_friend(User user1);
        void remove_friend(User user1);
        void respond_friend(User user1);


    }

    class View_holder extends RecyclerView.ViewHolder{
        TextView firstname, lastname, gender, status;
        ImageView im1,im2,im3;
        View v;
        StorageReference fs;
        int view_type;
        public View_holder(View itemView) throws ParseException {
            super(itemView);
            v = itemView;
            lastname = (TextView) v.findViewById(R.id.textView2);
            firstname = (TextView) v.findViewById(R.id.textView);
            gender = (TextView) v.findViewById(R.id.textView3);
            status =(TextView) v.findViewById(R.id.relation);
            im1 = (ImageView) v.findViewById(R.id.imageView5);
            im2 = (ImageView) v.findViewById(R.id.imageView14);
            im3 = (ImageView) v.findViewById(R.id.imageView2);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

             v = inflater.inflate(R.layout.adaptor_friend, parent, false);

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
         user1 = uselist.get(position);
            view_holder.firstname.setText(user1.first_name);
        view_holder.lastname.setText(user1.last_name);
        view_holder.im3.setVisibility(View.INVISIBLE);
        view_holder.im2.setVisibility(View.INVISIBLE);
        if(user1.image_url!=null){
        view_holder.fs =   FirebaseStorage.getInstance().getReferenceFromUrl(user1.image_url);
        Glide.with(context )
                .using(new FirebaseImageLoader())
                .load(view_holder.fs)
                .into(view_holder.im1);}
        view_holder.gender.setText(user1.gender?"Female":"Male");

        if (current_user.relationship.containsKey(user1.uuid)) {
            switch(current_user.relationship.get(user1.uuid))
            {
                case 0:
                    view_holder.status.setText("Friends!");
                    view_holder.status.setTextColor(Color.parseColor("#79FE33"));
                    view_holder.im2.setVisibility(View.VISIBLE);
                    view_holder.im2.setImageResource(R.drawable.unfriend);
                    view_holder.im2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            intf.remove_friend(uselist.get(position));
                        }
                    });
                    break;
                case 1:
                    view_holder.status.setText("Already Requested!");
                    view_holder.status.setTextColor(Color.parseColor("#0f4d8d"));
                    view_holder.im3.setVisibility(View.VISIBLE);
                    view_holder.im3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            intf.remove_friend(uselist.get(position));
                        }
                    });
                    break;
                case -1:
                    view_holder.status.setText("Respond to Request!");
                    view_holder.status.setTextColor(Color.parseColor("#e65c46"));
                    view_holder.im2.setImageResource(R.drawable.addfrnd);
                    view_holder.im3.setVisibility(View.VISIBLE);
                    view_holder.im2.setVisibility(View.VISIBLE);
                    view_holder.im2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            intf.respond_friend(uselist.get(position));
                        }
                    });
                    view_holder.im3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            intf.remove_friend(uselist.get(position));
                        }
                    });
                    break;
            }

        }
        else
        {
            view_holder.status.setText("Send Request!");
            view_holder.im2.setImageResource(R.drawable.addfrnd);
            view_holder.status.setTextColor(Color.parseColor("#d7e2e8"));
            view_holder.im2.setVisibility(View.VISIBLE);
            view_holder.im2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intf.add_friend(uselist.get(position));
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return uselist.size();
    }
}
