package com.abood.photogallary;


import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;


public class PhotoGallaryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView mPhotoRecyclerView;
    private ArrayList<GalleryItem> mItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    private int CurrentPage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        CurrentPage = 1;
        new FetchItemsTask().execute(CurrentPage++);

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);

        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {

                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {

                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        photoHolder.bindDrawable(drawable);

                    }
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_photo_gallary, container, false);

        mPhotoRecyclerView = v.findViewById(R.id.Photo_gallary_recycler_view);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        } else {
            mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 5));
        }


        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(!recyclerView.canScrollVertically(1)) {
                    new FetchItemsTask().execute(CurrentPage++);
                }
            }
        });


        setupAdapter();

        return v;

    }


    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private ArrayList<GalleryItem> mGalleryItems;

        public PhotoAdapter(ArrayList<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallary_holder, viewGroup, false);
            return new PhotoHolder(view);

        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {

            GalleryItem galleryItem = mGalleryItems.get(position);
            Drawable placeholder = getResources().getDrawable(R.drawable.ic_image_24dp);
            photoHolder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(photoHolder, galleryItem.getgUrl());

        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }


    private class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView mItemImageView;;

        public PhotoHolder(View itemView) {
            super(itemView);

            mItemImageView = itemView.findViewById(R.id.item_image_view);
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }


    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }


    private class FetchItemsTask extends AsyncTask<Integer,Void,ArrayList<GalleryItem>> {

        @Override
        protected ArrayList<GalleryItem> doInBackground(Integer... pageNumber) {

            return new FlickrFetchr().fetchItems(pageNumber[0]);

        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }

    }

    public static PhotoGallaryFragment newInstance() {
        return new PhotoGallaryFragment();
    }

}
