package com.example.android.cola;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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

    FriendListItem item = new FriendListItem();

    public FriendListAdapter(Context context) {
        setContext(context);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setContext(Context context) {
        this.context = context;
    }


    @Override

    public int getCount() {
        return item.getSize();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        View view = inflater.inflate(R.layout.adapter_friendlist, viewGroup, false);
        //view를 구성하는 것을 채워줌
        TextView profileName = (TextView) view.findViewById(R.id.prf_name);
        TextView profileMsg = (TextView) view.findViewById(R.id.prf_msg);
        ImageView profileImg = (ImageView) view.findViewById(R.id.prf_img);

        profileName.setText(item.getName(i));
        profileMsg.setText(item.getMsg(i));
        profileImg.setImageResource(item.imgResource(i));

        return view;
    }
}
