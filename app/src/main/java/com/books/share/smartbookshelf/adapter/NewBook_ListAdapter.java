package com.books.share.smartbookshelf.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.books.share.smartbookshelf.R;
import com.books.share.smartbookshelf.lib.trans.api.object.Book;
import com.books.share.smartbookshelf.lib.trans.api.object.books.VolumeInfo;
import com.books.share.smartbookshelf.ui.AddBookActivity;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by limjuhyun on 26/07/2017.
 */
public class NewBook_ListAdapter extends RecyclerView.Adapter<NewBook_ListAdapter.RecyclerViewHolders> {

    private Context context;
    private ArrayList<Book> itemList;
    private String pos;
    private Gson gson;

    //생성자
    public NewBook_ListAdapter(Context context, ArrayList<Book> itemList, String pos) {
        this.context = context;
        this.itemList = itemList;
        this.pos = pos;
        gson = new Gson();
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_card_view, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolders holder, int position) {
        VolumeInfo volumeInfo = itemList.get(position).getVolumeInfo();

        holder.tvTitle.setText(volumeInfo.getTitle());


        try {
            holder.ivPublisher.setText(android.text.TextUtils.join(",", volumeInfo.getAuthors()));
        } catch (Exception e) {
        }

        holder.ivDescription.setText(volumeInfo.getDescription());
        try {
            Picasso.with(context).load(itemList.get(position).getVolumeInfo().getImageLinks().get("thumbnail")).into(holder.ivPhoto);
            Log.d("img", itemList.get(position).getVolumeInfo().getImageLinks().get("thumbnail"));
        } catch (Exception e) {
            holder.ivPhoto.setImageResource(R.mipmap.no_cover_thumb);
        }

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    //뷰홀더 클래스 정의 - 별도 파일로 하거나 어답터 안에서 정의해 줄 수 있다.
    //여기에서 반복적으로 사용되는 각종 뷰들을 정의해 준다.
    public class RecyclerViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tvTitle;
        public ImageView ivPhoto;
        public TextView ivPublisher;
        public TextView ivDescription;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            tvTitle = (TextView) itemView.findViewById(R.id.book_card_view_name);
            ivPhoto = (ImageView) itemView.findViewById(R.id.book_card_view_img);
            ivPublisher = (TextView) itemView.findViewById(R.id.book_card_view_publisher);
            ivDescription = (TextView) itemView.findViewById(R.id.book_card_view_desc);

            itemView.setOnClickListener(this);

        }

        //클릭 이벤트 정의
        @Override
        public void onClick(View view) {
            Toast.makeText(view.getContext(), "Clicked Card Position = " + getLayoutPosition(), Toast.LENGTH_SHORT).show();
            Intent moveIntent = new Intent(context, AddBookActivity.class);
            moveIntent.putExtra("book", itemList.get(getLayoutPosition()));
            moveIntent.putExtra("pos", pos);
            context.startActivity(moveIntent);
        }
    }

}
