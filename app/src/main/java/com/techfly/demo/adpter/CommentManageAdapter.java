package com.techfly.demo.adpter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.techfly.demo.R;
import com.techfly.demo.bean.GoodReviewsBean;
import com.techfly.demo.interfaces.ItemMultClickListener;
import com.techfly.demo.selfview.photepicker.PhotoPagerActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * 评价列表
 */
public class CommentManageAdapter extends BaseAdapter {

    private Context mContext;
    private List<GoodReviewsBean.DataEntity.DatasEntity> listData;// = new ArrayList<List<GoodsBean>>();
    private LayoutInflater layoutInflater;
    private ItemMultClickListener mItemClickListener;


    public CommentManageAdapter(Context context, List<GoodReviewsBean.DataEntity.DatasEntity> list) {
        this.layoutInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.listData = list;
    }

    public void addAll(List<GoodReviewsBean.DataEntity.DatasEntity> info) {
        listData.addAll(info);
        notifyDataSetChanged();
    }

    public void setItemClickListener(ItemMultClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void clearAll(){
        listData.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return listData != null ? listData.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup arg2) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_comment_manage_lv, null);
            holder = new ViewHolder();

            holder.item_date_tv = (TextView)convertView.findViewById(R.id.item_date_tv);
            holder.item_ratingbar = (RatingBar)convertView.findViewById(R.id.item_ratingbar);

            holder.item_name_tv = (TextView)convertView.findViewById(R.id.item_name_tv);
            holder.item_phone_tv = (TextView)convertView.findViewById(R.id.item_phone_tv);
            holder.item_order_number_tv = (TextView)convertView.findViewById(R.id.item_order_number_tv);
            holder.item_comment_tv = (TextView)convertView.findViewById(R.id.item_comment_tv);

            holder.item_container_rl = (LinearLayout)convertView.findViewById(R.id.item_container_rl);
            holder.item_container_linear = (LinearLayout)convertView.findViewById(R.id.item_container_linear);

            holder.item_contact_tv = (TextView)convertView.findViewById(R.id.item_contact_tv);
            holder.item_reply_tv = (TextView)convertView.findViewById(R.id.item_reply_tv);

            holder.item_reply_status_tv = (TextView)convertView.findViewById(R.id.item_reply_status_tv);

            holder.item_delete_iv = (ImageView)convertView.findViewById(R.id.item_delete_iv);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String[] date = listData.get(position).getCreate_time().split(" ");
        if(date.length > 1){
            holder.item_date_tv.setText(date[0]);
        }

        holder.item_ratingbar.setRating(Float.parseFloat(listData.get(position).getMark() + ""));

        if(listData.get(position).getCan_review() == 1){
            holder.item_reply_status_tv.setVisibility(View.INVISIBLE);
        }else{
            holder.item_reply_status_tv.setVisibility(View.VISIBLE);
        }

        holder.item_name_tv.setText(listData.get(position).getNickname());
        holder.item_phone_tv.setText(listData.get(position).getMobile());
        holder.item_order_number_tv.setText(listData.get(position).getCode());
        holder.item_comment_tv.setText(listData.get(position).getContent());

        String imags = listData.get(position).getImages();
        imags = imags.replace("[]", "");
        if(TextUtils.isEmpty(imags)){
            holder.item_container_rl.setVisibility(View.GONE);
        }else{
            holder.item_container_rl.setVisibility(View.VISIBLE);
            //图片部分
            holder.item_container_linear.removeAllViews();

            String[] imagesArray = listData.get(position).getImages().split(" ");
            final ArrayList<String> picList = new ArrayList<String>();
            Collections.addAll(picList, imagesArray);

            int indicate = 0;  //当前图片
            for(String imgUrl:picList){
                View view = View.inflate(mContext, R.layout.layout_imageview_review, null);
                ImageView mphote = (ImageView) view.findViewById(R.id.phote_Iv);
                Glide.with(mContext)
                        .load(imgUrl)
                        .thumbnail(0.3f)
                        .placeholder(R.drawable.def_photo)
                        .error(R.drawable.def_photo)
                        .into(mphote);

                final int finalI = indicate;
                mphote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, PhotoPagerActivity.class);
                        intent.putExtra(PhotoPagerActivity.EXTRA_CURRENT_ITEM, finalI);
                        intent.putExtra(PhotoPagerActivity.EXTRA_PHOTOS, picList);
                        intent.putExtra(PhotoPagerActivity.EXTRA_EDITABLE, false);
                        mContext.startActivity(intent);
                    }
                });
                setPicParams(view, true);
                holder.item_container_linear.addView(view);
                indicate++;
            }
        }

        holder.item_delete_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mItemClickListener != null){
                    mItemClickListener.onItemSubViewClick(0, position);
                }
            }
        });

        holder.item_contact_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemSubViewClick(1, position);
                }
            }
        });

        holder.item_reply_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemSubViewClick(2, position);
                }
            }
        });



        return convertView;
    }

    /////////////////缩略图的布局
    private void setPicParams(View view, boolean requirdRightMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (requirdRightMargin) {
            params.rightMargin = (int) mContext.getResources().getDimension(R.dimen.marign_mid);
        }
        view.setLayoutParams(params);
    }

    public static class ViewHolder {
        public TextView item_date_tv;
        public RatingBar item_ratingbar;

        public TextView item_name_tv;
        public TextView item_phone_tv;
        public TextView item_order_number_tv;
        public TextView item_comment_tv;

        public LinearLayout item_container_rl;
        public LinearLayout item_container_linear;

        public TextView item_contact_tv;
        public TextView item_reply_tv;

        public TextView item_reply_status_tv;

        public ImageView item_delete_iv;
    }

}