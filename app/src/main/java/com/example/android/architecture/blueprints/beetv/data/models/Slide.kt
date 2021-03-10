package com.example.android.architecture.blueprints.beetv.data.models

data class Slide (
        val image :String
){
    companion object{
        fun mocks() = mutableListOf<Slide>(
//                Slide("https://i.ytimg.com/vi/0n7llXgSn4U/maxresdefault.jpg"),
                Slide("https://www.danverslibrary.org/readthis/wp-content/uploads/2017/06/wonderwoman-1024x637.jpg"),
                Slide("https://www.danverslibrary.org/readthis/wp-content/uploads/2017/06/wonderwoman-1024x637.jpg"),
                Slide("https://www.danverslibrary.org/readthis/wp-content/uploads/2017/06/wonderwoman-1024x637.jpg")
//                Slide("https://townsquare.media/site/295/files/2019/09/GNRmovies.jpg"),
//                Slide("https://i.ytimg.com/vi/f1gWbvvLDwE/maxresdefault.jpg")
        )
    }
}