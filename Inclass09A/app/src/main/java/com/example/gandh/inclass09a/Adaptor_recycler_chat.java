package com.example.gandh.inclass09a;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ocpsoft.pretty.time.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by gandh on 4/5/2017.
 */

public class Adaptor_recycler_chat extends RecyclerView.Adapter {

    Map<String,Chat> chatlist = new HashMap<>();
    ArrayList<Chat> chat_list ;
    Context context;
    Chat chat1;
    fromadaptor intf;
    View v;
    String uid;

    boolean discover_b;
    PrettyTime pt;
    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Adaptor_recycler_chat(Context context, Map<String,Chat> five_day_fcast, fromadaptor intf, boolean discover_b,String uid){
        this.chatlist = five_day_fcast;
            this.intf = intf;
        this.context = context;
        this.discover_b = discover_b;
        chat_list = new ArrayList<>(five_day_fcast.values());
        this.uid = uid;
    }

    interface fromadaptor{
        void chat_delete_personal(String unique);

    }

    class View_holder extends RecyclerView.ViewHolder{

        View v;
        TextView t1,t2,t3;
        ImageView iv,delete;
        StorageReference fs;
        CardView card;
        public View_holder(View itemView) throws ParseException {
            super(itemView);
            v = itemView;
            card = (CardView) v.findViewById(R.id.card);
            t1 = (TextView) v.findViewById(R.id.textView);
            t2 = (TextView) v.findViewById(R.id.textView4);
            t3 =(TextView) v.findViewById(R.id.textView2);
            iv = (ImageView) v.findViewById(R.id.imageView8);
            delete = (ImageView) v.findViewById(R.id.imageView10);
            iv.setVisibility(View.INVISIBLE);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

             v = inflater.inflate(R.layout.adaptor_chat, parent, false);

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
            View_holder view_holder = (View_holder) holder;
         chat1 = chat_list.get(position);
        if(chat1.post_uid.equals(uid))
        {
            RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) view_holder.card.getLayoutParams();
            rl.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rl.addRule(RelativeLayout.ALIGN_PARENT_END);

        }
        if(chat1.image)
        {
            view_holder.iv.setVisibility(View.VISIBLE);
            view_holder.fs = FirebaseStorage.getInstance().getReferenceFromUrl(chat1.image_url);
            Glide.with(context )
                    .using(new FirebaseImageLoader())
                    .load(view_holder.fs)
                    .into(view_holder.iv);
        }
         pt = new PrettyTime();
        view_holder.t1.setText("posted by:"+chat1.post_username);
        try {
            view_holder.t3.setText(pt.format(sd.parse(chat1.time)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        view_holder.t2.setText(chat1.message);
        view_holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chat1 = chat_list.get(position);
                intf.chat_delete_personal(chat1.unique);
            }
        });
        chat1 = new Chat();
    }

    @Override
    public int getItemCount() {
        return chatlist.size();
    }
}
