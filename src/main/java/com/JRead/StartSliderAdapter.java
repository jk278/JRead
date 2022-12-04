package com.JRead;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StartSliderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public Context mContext;
    public List<String> str;

    public StartSliderAdapter(Context mContext, List<String> str) {
        this.mContext = mContext; //构造器
        this.str = str;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleItem; // onBindViewHolder 设置文本要用

        public ViewHolder(View itemView) {
            super(itemView);
            titleItem = itemView.findViewById(R.id.item_slider_tv);
        }
    }

    OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public interface  OnItemClickListener {
        void onItemClick(View view, int position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View item = LayoutInflater.from(mContext) // 从主环境扩充视图
                .inflate(R.layout.rv_item_slider, parent, false);

        item.setOnClickListener(view -> listener.onItemClick(view,(int)view.getTag() ));
        // 类型为1时，设置tag
        return new ViewHolder(item); // 创建新holder
    }

    @Override //对于一个holder，上下是连续执行的
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof  ViewHolder){ //单独的holder，放单独的内容
            String data = str.get(position);
            ((ViewHolder) holder).titleItem.setText(data); // 通过 ViewHolder 设置文本

            holder.itemView.setTag(position);
        }
    }

    @Override
    public int getItemCount() { //获取表长
        return str.size();
    }
}