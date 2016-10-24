package com.example.android.cola;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by 민경태 on 2016-10-24.
 * Modify by 민경태 on 2016-10-24.
 * 클래스 생성 : FriendListAdapter
 * FriendListActivity에서 쓰일 Adapter
 *
 * FriendListItem을 받아 리스트 정보를 받음
 * Modify by 민경태 on 2016-10-24
 *
 */

public class FriendListAdapter extends BaseAdapter {

    private Context context;
    LayoutInflater inflater;

    private ArrayList<FriendListItem> FriendList = new ArrayList<FriendListItem>() ;

    public FriendListAdapter(Context context) {
        setContext(context);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setContext(Context context) {
        this.context = context;
    }


    @Override

    public int getCount() {
        return FriendList.size();
    }

    @Override
    public Object getItem(int i) {
        return FriendList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {

        final int pos = i;
        final Context context = parent.getContext();
        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.adapter_friendlist, parent, false);
        }

        //view를 구성하는 것을 채워줌
        TextView profileName = (TextView) view.findViewById(R.id.prf_name);
        TextView profileMsg = (TextView) view.findViewById(R.id.prf_msg);
        ImageView profileImg = (ImageView) view.findViewById(R.id.prf_img);

        FriendListItem fli = FriendList.get(pos);
        profileName.setText(fli.getName());
        profileMsg.setText(fli.getMsg());
        profileImg.setImageResource(fli.getRes());

        return view;
    }
    public void addItem(String name, String msg,int res){
        FriendListItem fli = new FriendListItem();
        fli.setName(name);
        fli.setMsg(msg);
        fli.setRes(res);

        FriendList.add(fli);
    }
}
