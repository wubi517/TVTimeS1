package com.it_tech613.tvtimes1.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.it_tech613.tvtimes1.R;

public class SelectLoginTypeDlg extends DialogFragment implements View.OnFocusChangeListener, View.OnClickListener {
    private TextView image_user,image_mac;
    private SelectList selectListener;
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_type, container);
        image_user = view.findViewById(R.id.imageView2);
        image_mac = view.findViewById(R.id.imageView4);
        image_user.setOnFocusChangeListener(this);
        image_mac.setOnFocusChangeListener(this);
        image_user.setOnClickListener(this);
        image_mac.setOnClickListener(this);
        image_user.requestFocus();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageView2:
                selectListener.onSelect(1);
                break;
            case R.id.imageView4:
                selectListener.onSelect(2);
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean b) {
        switch (v.getId()){
            case R.id.imageView2:
                if(b){
                    image_user.setScaleX(1.0f);
                    image_user.setScaleY(1.0f);
//                    image_user.setBackgroundResource(R.drawable.border);
                }else {
                    image_user.setScaleX(0.9f);
                    image_user.setScaleY(0.9f);
//                    image_user.setBackgroundResource(R.color.trans_parent);
                }
                break;
            case R.id.imageView4:
                if(b){
                    image_mac.setScaleX(1.0f);
                    image_mac.setScaleY(1.0f);
//                    image_mac.setBackgroundResource(R.drawable.border);
                }else {
                    image_mac.setScaleX(0.9f);
                    image_mac.setScaleY(0.9f);
//                    image_mac.setBackgroundResource(R.color.trans_parent);
                }
                break;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    public interface SelectList{
        void onSelect(int position);
    }
    public void setSelectListener(SelectList selectListener) {
        this.selectListener = selectListener;
    }
}
