package com.kparks.ribbit;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by kevin on 9/15/15.
 */
public class MessageAdapter extends ArrayAdapter<ParseObject> {
    protected Context mContext;
    protected List<ParseObject> mMessages;


    public MessageAdapter(Context context, List<ParseObject> objects) {
        super(context, R.layout.message_item, objects);
        mContext = context;
        mMessages = objects;
    }

    private static class ViewHolder {
        ImageView iconImageView;
        TextView nameLabel;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.messageIcon);
            holder.nameLabel = (TextView) convertView.findViewById(R.id.senderLabel);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        ParseObject message = mMessages.get(position);
        if(message.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_IMAGE)) {
            if(holder.iconImageView == null) {
                Log.e("Test", "Null holder iconImageView");
            }
            holder.iconImageView.setImageResource(R.mipmap.ic_action_picture);
        } else {
            holder.iconImageView.setImageResource(R.mipmap.ic_action_play_over_video);
        }
        holder.nameLabel.setText(message.getString(ParseConstants.KEY_SENDER_NAME));

        return convertView;
    }
}
