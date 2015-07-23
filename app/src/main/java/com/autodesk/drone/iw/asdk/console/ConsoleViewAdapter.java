package com.autodesk.drone.iw.asdk.console;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.autodesk.drone.iw.asdk.R;

import java.util.Vector;

/**
 * Created by DJia on 2/15/15.
 */
public class ConsoleViewAdapter extends BaseAdapter {
    private Vector<ConsoleItem> messageList = new Vector<ConsoleItem>();
    private Context context;
    private LayoutInflater mInflater;

    public ConsoleViewAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    public void addMessageItem(String message) {
        ConsoleItem consoleItem = new ConsoleItem(message);

        if (messageList.size() > 50) {
            messageList.remove(0);
        }
        messageList.add(consoleItem);

        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int i) {
        return messageList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View returnView = view;
        if (returnView == null) {
            returnView = mInflater.inflate(R.layout.console_view_item, null);
        }

        if (returnView == null) {
            return returnView;
        }

        ConsoleItem consoleItem = messageList.get(i);

        TextView timeView = (TextView) returnView.findViewById(R.id.consoleMsgTime);
        timeView.setText(consoleItem.getTime());


        TextView messageView = (TextView) returnView.findViewById(R.id.consoleMsgText);
        messageView.setText(consoleItem.getMessage());

        return returnView;
    }


}
