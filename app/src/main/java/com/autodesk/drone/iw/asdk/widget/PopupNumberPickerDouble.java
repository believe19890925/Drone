package com.autodesk.drone.iw.asdk.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.autodesk.drone.iw.asdk.CameraProtocolDemoActivity.pickerValueChangeListener;
import com.autodesk.drone.iw.asdk.R;
import com.autodesk.drone.iw.asdk.util.DensityUtil;
import com.autodesk.drone.iw.asdk.widget.wheel.AbstractWheelTextAdapter;
import com.autodesk.drone.iw.asdk.widget.wheel.OnWheelClickedListener;
import com.autodesk.drone.iw.asdk.widget.wheel.OnWheelScrollListener;
import com.autodesk.drone.iw.asdk.widget.wheel.WheelView;

import java.util.List;
import java.util.Map;

public class PopupNumberPickerDouble extends PopupWindow {


    private List<String> item_texts1 = null;  //第一个picker
    Map<Integer, List<Integer>> item_image1;

    private List<String> item_texts2 = null;//第二个picker
    Map<Integer, List<Integer>> item_image2;

    private Context mContext; // 上下文
    String[] strItemValue1;
    String[] strItemValue2;

    pickerValueChangeListener valueChangeListen;
    WheelView Wheelpicker1;
    WheelView Wheelpicker2;

    //int picker1_num = 0;
    //int picker2_num = 1;

    int picker_currentPos1 = 0;
    int picker_currentPos2 = 0;

    int jpeg_seq_interval = 0;

    public PopupNumberPickerDouble(Context context) {
        super(context);
    }

    public PopupNumberPickerDouble(Context context,
                                   List<String> item_strings1, Map<Integer, List<Integer>> ImagesMap1,  ///第一个picker 的数据
                                   List<String> item_strings2, Map<Integer, List<Integer>> ImagesMap2,     //第二个picker 的数据;
                                   pickerValueChangeListener itemClickEvent, int w, int h, int pos) {
        super(context);

        //pos = 1;
        item_texts1 = item_strings1;
        item_image1 = ImagesMap1;
        item_texts2 = item_strings2;
        item_image2 = ImagesMap2;

        this.mContext = context;
        valueChangeListen = itemClickEvent;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.numpicker_continue_shot, null);
        this.setContentView(view);
        this.setWidth(DensityUtil.dip2px(context, w));
        this.setHeight(DensityUtil.dip2px(context, h));
        this.setFocusable(true);//
        // this.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.pickerbackground_singl));

        ColorDrawable dw = new ColorDrawable(-00000);
        this.setBackgroundDrawable(dw);
        strItemValue1 = new String[item_texts1.size()];
        for (int i = 0; i < item_texts1.size(); i++) {
            strItemValue1[i] = item_texts1.get(i);

        }

        strItemValue2 = new String[item_texts2.size()];
        for (int i = 0; i < item_texts2.size(); i++) {
            strItemValue2[i] = item_texts2.get(i);

        }


        //第一个picker
        Wheelpicker1 = (WheelView) view.findViewById(R.id.id_numberPicker1);
        Wheelpicker1.addScrollingListener(onWheelScrollListener1);
        Wheelpicker1.addClickingListener(onWheelClickedListener1);
        Wheelpicker1.setViewAdapter(new TypeTextAdapter(context, Wheelpicker1, strItemValue1));
        //放到setViewAdapter 之后
        Wheelpicker1.setCurrentItem(pos);


        //第二个picker
        Wheelpicker2 = (WheelView) view.findViewById(R.id.id_numberPicker2);
        Wheelpicker2.addScrollingListener(onWheelScrollListener2);
        Wheelpicker2.addClickingListener(onWheelClickedListener2);
        Wheelpicker2.setViewAdapter(new TypeTextAdapter(context, Wheelpicker2, strItemValue2));
        //放到setViewAdapter 之后
        Wheelpicker2.setCurrentItem(pos);

        ImageButton select_button = (ImageButton) view.findViewById(R.id.id_select_imageButton1);
        select_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                valueChangeListen.onValueChange(picker_currentPos1, picker_currentPos2);

            }
        });

    }


    class TypeTextAdapter extends AbstractWheelTextAdapter {

        String[] strItemValue;

        protected TypeTextAdapter(Context context, WheelView wheelView, String[] strItemValue2) {
            // super(context, R.layout.type_layout, R.id.type_name, isSelected);
            super(context, R.layout.numpicker_type_layout, R.id.type_name);
            strItemValue = strItemValue2;
        }

        public int getItemsCount() {
            return strItemValue.length;
        }

        @Override
        protected CharSequence getItemText(int index) {
            // TODO Auto-generated method stub
            return strItemValue[index].toString();
        }

        @Override
        public View getItem(int index, View convertView, ViewGroup parent) {

            View view = super.getItem(index, convertView, parent);
            TextView tv = (TextView) view.findViewById(R.id.type_name);

			/*
            if(picker1_pos == index)
			{
				ImageView image = (ImageView) view.findViewById(R.id.flag);
				image.setVisibility(View.VISIBLE);
				image.setImageResource(R.drawable.batery_0);
	
			}
			else
			{
				ImageView image = (ImageView) view.findViewById(R.id.flag);
				image.setVisibility(View.INVISIBLE);
			}
	
	*/

            //tv.setText("fff");
            return view;
        }
    }

    ///第二个picker adapter ；
    class TypeImageAdapter extends AbstractWheelTextAdapter {

        List<Integer> imagelist;

        protected TypeImageAdapter(Context context, WheelView wheelView, List<Integer> imagelis) {
            // super(context, R.layout.type_layout, R.id.type_name, isSelected);
            super(context, R.layout.numpicker_type_layout, R.id.type_name);
            imagelist = imagelis;
        }

        public int getItemsCount() {
            return imagelist.size();
        }

        @Override
        protected CharSequence getItemText(int index) {
            // TODO Auto-generated method stub
            return "";
        }

        @Override
        public View getItem(int index, View convertView, ViewGroup parent) {

            View view = super.getItem(index, convertView, parent);
            TextView tv = (TextView) view.findViewById(R.id.type_name);
            tv.setVisibility(View.GONE);
            tv.setText("fff 0");
            ImageView image = (ImageView) view.findViewById(R.id.flag);
            image.setVisibility(View.VISIBLE);
            image.setImageResource(imagelist.get(index));
            tv.setText("fff 1");
            return view;
        }
    }


    class ItemAdapter extends AbstractWheelTextAdapter {

        List<Integer> imagelist;

        protected ItemAdapter(Context context, WheelView wheelView, List<Integer> imagelis) {
            // super(context, R.layout.type_layout, R.id.type_name, isSelected);
            super(context, R.layout.numpicker_type_layout, R.id.type_name);
            imagelist = imagelis;
        }

        public int getItemsCount() {
            return imagelist.size();
        }

        @Override
        protected CharSequence getItemText(int index) {
            // TODO Auto-generated method stub
            return "";
        }

        @Override
        public View getItem(int index, View convertView, ViewGroup parent) {

            View view = super.getItem(index, convertView, parent);
            TextView tv = (TextView) view.findViewById(R.id.type_name);
            tv.setVisibility(View.GONE);
            tv.setText("fff 0");
            ImageView image = (ImageView) view.findViewById(R.id.flag);
            image.setVisibility(View.VISIBLE);
            image.setImageResource(imagelist.get(index));
            tv.setText("fff 1");
            return view;
        }
    }

    OnWheelScrollListener onWheelScrollListener1 = new OnWheelScrollListener() {
        public void onScrollingStarted(WheelView wheel) {
            //select_type = type[wheelView.getCurrentItem()];
        }

        public void onScrollingFinished(WheelView wheel) {
            int pos = wheel.getCurrentItem();
            picker_currentPos1 = pos;
            //String select = strItemValue[pos];
            //Log.e("", "OnWheelScrollListener 当前选择了===" + select);
            //Wheelpicker2.setViewAdapter(new TypeImageAdapter(context, Wheelpicker2,item_image2.get(pos)));

        }
    };

    OnWheelClickedListener onWheelClickedListener1 = new OnWheelClickedListener() {

        public void onItemClicked(WheelView wheel, int itemIndex) {
            picker_currentPos1 = itemIndex;

            wheel.setCurrentItem(itemIndex);
            //String select = strItemValue[itemIndex];
            //	Log.e("", "OnWheelClickedListener 当前选择了" + select);
        }
    };

    OnWheelScrollListener onWheelScrollListener2 = new OnWheelScrollListener() {
        public void onScrollingStarted(WheelView wheel) {
            //select_type = type[wheelView.getCurrentItem()];
        }

        public void onScrollingFinished(WheelView wheel) {
            int pos = wheel.getCurrentItem();
            picker_currentPos2 = pos;

            //typeTextAdapter = new TypeTextAdapter(context, wheel,strItemValue1);
            //wheel.setViewAdapter(typeTextAdapter);
            //Log.e("", "OnWheelScrollListener 当前选择了===" + pos);


        }
    };

    OnWheelClickedListener onWheelClickedListener2 = new OnWheelClickedListener() {

        public void onItemClicked(WheelView wheel, int itemIndex) {
            wheel.setCurrentItem(itemIndex);
            picker_currentPos2 = itemIndex;
            //Log.e("", "OnWheelClickedListener pos" + itemIndex);;

        }
    };

}
