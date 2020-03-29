# PhotoGallery
BigNerdRanch: Android Programming study, chapter 24: Retrofit, ViewModel, Repository pattern, RecyclerView

to use Retreofit we will need to:
1. [make an API interface to connect to web api](#api-nterface)
2. [build the Retrofit instance](#build-retrofit)
    - provide base URL
    - deserialize response from web (convert ResponseBody to other type)
3. [ombine from both step above, we will get a `Call` object](#call-response-convert)
    - this object is the object to run the request to web
4. [then we will get Response object](#call-response-convert) (we will have to create this Response object class)
5. [from response object we can convert to the object we want to use](#call-response-convert) (LiveData / String / whatever)

### Api Interface

```kotlin
interface FlickrApi {

    @GET(
        "services/rest/?method=flickr.interestingness.getList" +
                "&api_key=660c3f34e0bc88113bedc20b2960abc2" +
                "&format=json" +
                "&nojsoncallback=1" +
                "&extras=url_s"
    )
    fun fetchPhotos(): Call<FlickrResponse>
}
```

### Build Retrofit

```kotlin
class FlickrFetchr {

    private val flickrApi: FlickrApi

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        flickrApi = retrofit.create(FlickrApi::class.java)
    }
}
```

### Call, Response, Convert

Below is the `json` response we get from web api, and we need to moel these into response and model
```js
{
  // we model "photos" as FlickrResponse, that contains PhotoResponse
  "photos": {
              "page": 1,
              "pages": 5,
              "perpage": 100,
              "total": 500,
              
              // model "photo" as PhotoResponse, that contains array of galleryItem
              "photo": [
                          // this is individual galleryItem
                          { 
                            "id": "49708647573",
                            "owner": "57761648@N02",
                            "secret": "66fb4ec927",
                            "server": "65535",
                            "farm": 66,
                            "title": "mother...",
                            "ispublic": 1,
                            "isfriend": 0,
                            "isfamily": 0,
                            "url_s": "https://live.staticflickr.com/65535/49708647573_66fb4ec927_m.jpg",
                            "height_s": 240,
                            "width_s": 240
                          },
                          ...
                       ]
            }
}
```

This would be the outer object we get from web api (json above, `photos` object)
```kotlin
class FlickrResponse {
    lateinit var photos: PhotoResponse
}
```

Another object inside outer object above, actually named `photo` array, but we rename it to `galleryItems`
```kotlin
class PhotoResponse {
    @SerializedName("photo")
    lateinit var galleryItems: List<GalleryItem>
}
```

This would be individual photo item
```kotlin
data class GalleryItem(
    var title: String = "",
    var id: String = "",
    @SerializedName("url_s") var url: String
)
```

Run the Call object and convert to LiveData
```kotlin
class FlickrFetchr {

    private val flickrApi: FlickrApi

    init {
        ...
    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        val flickrRequest: Call<FlickrResponse> = flickrApi.fetchPhotos()

        flickrRequest.enqueue(object : Callback<FlickrResponse> {
            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: Failed to fetch photos", t)
            }

            override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
                val flickrResponse: FlickrResponse? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?: mutableListOf()
                
                // this is to take only item with url and omit item without url
                galleryItems = galleryItems.filterNot { 
                    it.url.isBlank()
                }
                
                responseLiveData.value = galleryItems
                Log.d(TAG, "onResponse: ResponseLiveData received $responseLiveData")
            }
        })

        return responseLiveData
    }
}
```
