package de.lbader.apps.kult01;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import de.lbader.apps.kult01.imageviewer.ImageViewer;

/**
 * Created by lbader on 3/5/16.
 */
public class WebAppInterface {
    Context mContext;

    WebAppInterface(Context c) {
        mContext = c;
    }

    @JavascriptInterface
    public void loadGallery(String images, int position, String title) {
        String[] ids = images.split(";");
        ArrayList<String> urls = new ArrayList<>();
        for (String id : ids) {
            urls.add("http://kult01.de/gallery_system/system/" + id + "/large.JPG");
        }
        Intent intent = new Intent(mContext, ImageViewer.class);
        Bundle b = new Bundle();
        b.putInt("position", position);
        b.putStringArrayList("urls", urls);
        b.putString("title", title);
        intent.putExtras(b);
        mContext.startActivity(intent);
    }

    @JavascriptInterface
    public void shareNews(String title, String text, String created, String id) {
        String toShare = title.trim() + "\n" + text.trim() + "\n\n" + "http://kult01.de/news_" + id + ".aspx";
        Intent send = new Intent();
        send.setAction(Intent.ACTION_SEND);
        send.putExtra(Intent.EXTRA_TEXT, toShare);
        send.setType("text/plain");
        mContext.startActivity(send);
    }

    @JavascriptInterface
    public void showImage(String src, String title) {
        if (title.equals("")) {
            title = mContext.getString(R.string.app_name);
        }
        ArrayList<String> urls = new ArrayList<>();
        urls.add(src);
        Intent intent = new Intent(mContext, ImageViewer.class);
        Bundle b = new Bundle();
        b.putInt("position", 0);
        b.putStringArrayList("urls", urls);
        b.putString("title", title);
        intent.putExtras(b);
        mContext.startActivity(intent);
    }

    @JavascriptInterface
    public void scrollOnTop(boolean onTop) {
        final Boolean activate = onTop;
        ((MainActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity)mContext).swipeContainer.setEnabled(activate);
            }
        });
    }
}
