package com.drdlee.photogallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.drdlee.photogallery.api.FlickrFetchr
import com.drdlee.photogallery.api.GalleryItem

private const val TAG = "AppDebug"

class PhotoGalleryViewModel: ViewModel() {

    val galleryItemLiveData: LiveData<List<GalleryItem>>

    init {
        galleryItemLiveData = FlickrFetchr().fetchPhotos()
    }
}