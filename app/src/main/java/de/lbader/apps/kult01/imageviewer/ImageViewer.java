package de.lbader.apps.kult01.imageviewer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

import de.lbader.apps.kult01.R;

public class ImageViewer extends FragmentActivity {

    private FixedViewPager mPager;
    private PagerAdapter mPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        ArrayList<String> urls = getIntent().getExtras().getStringArrayList("urls");
        int position = getIntent().getExtras().getInt("position");

        mPager = (FixedViewPager) findViewById(R.id.gallery_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), urls);
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(position, false);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<String> urls;

        public ScreenSlidePagerAdapter(FragmentManager fm, ArrayList<String> urls) {
            super(fm);
            this.urls = urls;
        }

        @Override
        public Fragment getItem(int position) {
            return ImageFragment.newInstance(urls.get(position), getString(R.string.app_name));
        }

        @Override
        public int getCount() {
            return this.urls.size();
        }
    }

}
