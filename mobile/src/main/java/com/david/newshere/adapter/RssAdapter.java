package com.david.newshere.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.david.newshere.activity.ArticleActivity;
import com.david.newshere.R;
import com.pkmmte.pkrss.Article;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by davidhodge on 10/20/14.
 */
public class RssAdapter extends BaseAdapter {

    private List<Article> rssItems;
//    private ViewHolder holder;
    private LayoutInflater mInflater;
    private Context context;
    private Activity activity;

    public RssAdapter(Context c, List<Article> mRssItems, Activity activity) {
        context = c;
        mInflater = LayoutInflater.from(c);
        rssItems = mRssItems;
        this.activity = activity;
    }

    public void setData(List<Article> mRssItems) {
        rssItems = mRssItems;
    }

    public int getCount() {
        return rssItems.size();
    }

    public Article getItem(int position) {
        return rssItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

//    class ViewHolder {
//
//        TextView mTitle;
//        TextView mPubDate;
//        TextView mContent;
//        TextView mAuthor;
//        TextView mDesc;
//        String mLink;
//        ImageView mImage;
//    }

    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = mInflater.inflate(R.layout.rss_item, null);

//            view.setTag(holder);
        }

        final Article mItem = rssItems.get(position);

        final TextView mTitle = (TextView) view.findViewById(R.id.rss_title_text);
        TextView mAuthor = (TextView) view.findViewById(R.id.rss_desc_text);
        TextView mPubDate = (TextView) view.findViewById(R.id.rss_pub_text);
        ImageView mImage = (ImageView) view.findViewById(R.id.article_image);

        Picasso.with(context).load(mItem.getImage()).into(mImage);

        long time = mItem.getDate();
        final Date date = new Date(time);
        final SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd k:mma");
        format.setTimeZone(TimeZone.getDefault());

        mImage.setVisibility(View.GONE);
        mTitle.setText(mItem.getTitle());
        mAuthor.setText(mItem.getAuthor());
        mPubDate.setText(format.format(date));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewArticle = new Intent(context, ArticleActivity.class);
                viewArticle.putExtra("article_title", mItem.getTitle());
                viewArticle.putExtra("article_author", mItem.getAuthor());
                viewArticle.putExtra("article_date", format.format(date));
                viewArticle.putExtra("article_content", mItem.getDescription());
                viewArticle.putExtra("article_link", mItem.getSource());
                if(Build.VERSION.SDK_INT >= 21) {
                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation(activity, mTitle, "robot");
                    context.startActivity(viewArticle, options.toBundle());
                }else{
                    context.startActivity(viewArticle);
                }
            }
        });

        return view;
    }

    private static String removeLastChar(String str) {
        return str.substring(0,str.length()-15);
    }
}
