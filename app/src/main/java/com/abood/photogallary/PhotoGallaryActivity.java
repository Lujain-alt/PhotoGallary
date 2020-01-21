package com.abood.photogallary;

import androidx.fragment.app.Fragment;

public class PhotoGallaryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {

        return PhotoGallaryFragment.newInstance();

    }

}
