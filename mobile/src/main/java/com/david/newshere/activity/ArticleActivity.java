package com.david.newshere.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.david.newshere.R;
import com.david.newshere.utils.SystemBarTintManager;

/**
 * Created by davidhodge on 10/20/14.
 */
public class ArticleActivity extends ActionBarActivity {

    Context mContext;
    ActionBar actionbar;
    String articleTitle;
    String articleDate;
    String articleContent;
    String articleLink;
    String articleAuth;

    TextView articleTitleView;
    TextView articleDateView;
    TextView articleContentView;
    TextView articleAuthView;
    LinearLayout titleHolder;

    public static final String VIEW_NAME_HEADER_TITLE = "detail:header:title";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21) {
            getWindow().setExitTransition(new Fade());
        }

        setContentView(R.layout.rss_article_view);
        mContext = this;
        brandGlowEffect(mContext, mContext.getResources().getColor(R.color.main_color));

        if(Build.VERSION.SDK_INT == 19){
            setTranslucentNav(true);
            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(ArticleActivity.this);
            systemBarTintManager.setStatusBarTintEnabled(true);
            systemBarTintManager.setStatusBarTintColor(getResources().getColor(R.color.main_color_dark));
            systemBarTintManager.setNavigationBarTintEnabled(true);
            systemBarTintManager.setNavigationBarTintColor(getResources().getColor(android.R.color.transparent));
        }

        actionbar = getSupportActionBar();
        actionbar.setDisplayShowTitleEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);

        articleTitleView = (TextView) findViewById(R.id.article_title);
        articleDateView = (TextView) findViewById(R.id.article_date);
        articleContentView = (TextView) findViewById(R.id.article_content);
        articleAuthView = (TextView) findViewById(R.id.article_auth);
        titleHolder = (LinearLayout) findViewById(R.id.title_holder);

        Bundle extras = getIntent().getExtras();
        articleTitle = extras.getString("article_title");
        articleDate = extras.getString("article_date");
        articleContent = extras.getString("article_content");
        articleLink = extras.getString("article_link");
        articleAuth = extras.getString("article_auth");

        actionbar.setTitle(articleDate);

        articleTitleView.setText(articleTitle);
        articleDateView.setText(articleDate);
        articleDateView.setVisibility(View.GONE);
        articleAuthView.setText(articleAuth);

        try{
            articleContentView.setText(stripHtml(articleContent));
        }catch (NullPointerException e){
            e.printStackTrace();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleLink));
            startActivity(browserIntent);
            this.finish();
        }

    }

    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 15);
    }

    private String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.web_view:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleLink));
                startActivity(browserIntent);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();

    }

    static void brandGlowEffect(Context context, int brandColor) {
        //glow
        try {
            int glowDrawableId = context.getResources().getIdentifier("overscroll_glow", "drawable", "android");
            Drawable androidGlow = context.getResources().getDrawable(glowDrawableId);
            androidGlow.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
            //edge
            int edgeDrawableId = context.getResources().getIdentifier("overscroll_edge", "drawable", "android");
            Drawable androidEdge = context.getResources().getDrawable(edgeDrawableId);
            androidEdge.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
        }catch (Exception e){
            Log.e("parkfans", e.toString());
        }
    }

    @TargetApi(19)
    private void setTranslucentNav(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        final int bits1 = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        if (on) {
            winParams.flags |= bits;
            winParams.flags |= bits1;
        } else {
            winParams.flags &= ~bits;
            winParams.flags &= ~bits1;
        }
        win.setAttributes(winParams);
    }
}
