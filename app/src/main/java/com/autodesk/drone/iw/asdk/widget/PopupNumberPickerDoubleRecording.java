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

public class PopupNumberPickerDoubleRecording extends PopupWindow {

    private List<String> item_texts1 = null;  //第一个picker
    Map<Integer, List<Integer>> item_image1;
    private List<String> item_texts2 = null;//第二个picker
    Map<Integer, List<Integer>> item_image2;

    private Context mContext; // 上下文
    String[] strItemValue;
    String[] strItemValue2;
    TypeTextAdapter typeTextAdapter;

    pickerValueChangeListener valueChangeListen;
    WheelView Wheelpicker1;
    WheelView Wheelpicker2;

    int picker1_pos = 0;
    int picker2_pos = 0;

    public PopupNumberPickerDoubleRecording(Context context) {
        super(context);
    }

    public PopupNumberPickerDoubleRecording(Context context,
                                            List<String> item_strings1, Map<Integer, List<Integer>> ImagesMap1,  ///第一个picker 的数据
                                            List<String> item_strings2, Map<Integer, List<Integer>> ImagesMap2,     //第二个picker 的数据;
                                            pickerValueChangeListener itemClickEvent, int w, int h, int pos) {
        super(context);

        item_texts1 = item_strings1;
        item_texts2 = item_strings2;
        item_image1 = ImagesMap1;
        item_image2 = ImagesMap2;

        this.mContext = context;
        valueChangeListen = itemClickEvent;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.numpicker_double, null);
        this.setContentView(view);
        this.setWidth(DensityUtil.dip2px(context, w));
        this.setHeight(DensityUtil.dip2px(context, h));
        this.setFocusable(true);//
        // this.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.pickerbackground_singl));

        ColorDrawable dw = new ColorDrawable(-00000);
        this.setBackgroundDrawable(dw);

        strItemValue = new String[item_texts1.size()];
        for (int i = 0; i < item_texts1.size(); i++) {
            strItemValue[i] = item_texts1.get(i);
        }

        strItemValue2 = new String[item_texts2.size()];
        for (int i = 0; i < item_texts2.size(); i++) {
            strItemValue2[i] = item_texts2.get(i);

        }

        //第一个picker
        Wheelpicker1 = (WheelView) view.findViewById(R.id.id_numberPicker1);
        Wheelpicker1.addScrollingListener(onWheelScrollListener1);
        Wheelpicker1.addClickingListener(onWheelClickedListener1);

        Wheelpicker1.setViewAdapter(new TypeTextAdapter(context, Wheelpicker1, strItemValue));
        //放到setViewAdapter 之后
        Wheelpicker1.setCurrentItem(pos);


        //第二个picker
        Wheelpicker2 = (WheelView) view.findViewById(R.id.id_numberPicker2);
        Wheelpicker2.addScrollingListener(onWheelScrollListener2);
        Wheelpicker2.addClickingListener(onWheelClickedListener2);
        Wheelpicker2.setViewAdapter(new TypeTextAdapter(context, Wheelpicker2, strItemValue2));
        //Wheelpicker2.setViewAdapter(new TypeImageAdapter(context, Wheelpicker2,item_image2.get(pos)));
        //放到setViewAdapter 之后
        Wheelpicker2.setCurrentItem(pos);

        ImageButton select_button = (ImageButton) view.findViewById(R.id.id_select_imageButton1);
        select_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                valueChangeListen.onValueChange(picker1_pos, picker2_pos);

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
            ImageView image = (ImageView) view.findViewById(R.id.flag);
            image.setVisibility(View.VISIBLE);
            image.setImageResource(imagelist.get(index));
            return view;
        }
    }


    OnWheelScrollListener onWheelScrollListener1 = new OnWheelScrollListener() {
        public void onScrollingStarted(WheelView wheel) {
            //select_type = type[wheelView.getCurrentItem()];
        }

        public void onScrollingFinished(WheelView wheel) {
            int pos = wheel.getCurrentItem();
            picker1_pos = pos;
            //Log.e("MypickerDouble", "OnWheelScrollListener1 当前选择了===" + pos);
//			Wheelpicker2.setViewAdapter(new TypeImageAdapter(mContext, Wheelpicker2,item_image2.get(pos)));
//			if(currentPos1 == picker1_pos)
//			{
//
//				Wheelpicker2.setCurrentItem(currentPos2);
//			}else
//			{
//
//				Wheelpicker2.setCurrentItem(0);
//				picker2_pos = 0;
//			}

        }
    };

    OnWheelClickedListener onWheelClickedListener1 = new OnWheelClickedListener() {

        public void onItemClicked(WheelView wheel, int itemIndex) {
            picker1_pos = itemIndex;
            wheel.setCurrentItem(itemIndex);
            //Log.e("", "OnWheelClickedListener1 当前选择了" + itemIndex);
            //Wheelpicker2.setViewAdapter(new TypeImageAdapter(mContext, Wheelpicker2,item_image2.get(picker1_pos)));
        }
    };

    OnWheelScrollListener onWheelScrollListener2 = new OnWheelScrollListener() {
        public void onScrollingStarted(WheelView wheel) {
            //select_type = type[wheelView.getCurrentItem()];
        }

        public void onScrollingFinished(WheelView wheel) {
            int pos = wheel.getCurrentItem();
            picker2_pos = pos;
            //Log.e("", "OnWheelScrollListener2 当前选择了===" + pos);


        }
    };

    OnWheelClickedListener onWheelClickedListener2 = new OnWheelClickedListener() {

        public void onItemClicked(WheelView wheel, int itemIndex) {
            wheel.setCurrentItem(itemIndex);
            picker2_pos = itemIndex;
            //Log.e("", "OnWheelClickedListener2 pos" + itemIndex);;

        }
    };

}
