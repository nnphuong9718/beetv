package com.example.android.architecture.blueprints.beetv.manager

import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.models.BCategory
import com.example.android.architecture.blueprints.beetv.util.Constants

class CategoryManager {

    private val mCategories = mutableListOf<BCategory>()

    companion object {
        private var mInstance: CategoryManager? = null
        fun getInstance(): CategoryManager {

            if (mInstance == null) {
                mInstance = CategoryManager()
            }
            return mInstance!!
        }
    }

    fun setData(categories: List<BCategory>?) {
        mCategories.clear()
        if (categories != null)
            mCategories.addAll(categories)
    }

    fun getData(type: String): MutableList<BCategory> {
        val list = mutableListOf<BCategory>()
        mCategories.forEach {
            if (type == Constants.TYPE_CATEGORY.TV.type) {
                if (it.type == type && it.parent_id == null) {
                    list.add(it)
                }
            } else {
//                if (it.tags != null)
//                    if (it.tags.contains(type)){
//                        list.add(it)
//                    }


                if (it.group == type) {
                    list.add(it)
                }
            }
        }
        list.sortBy { it -> it.position }
        if (type == Constants.TYPE_CATEGORY.FAVORITE.type || type == Constants.TYPE_CATEGORY.PLAYBACK.type) {
            list.add(BCategory("0", name = BeeTVApplication.context.resources.getString(R.string.all)))
            list.add(BCategory("1", name = BeeTVApplication.context.resources.getString(R.string.movie)))
            list.add(BCategory("2", name = BeeTVApplication.context.resources.getString(R.string.drama)))
            list.add(BCategory("3", name = BeeTVApplication.context.resources.getString(R.string.entertainment)))
            list.add(BCategory("4", name = BeeTVApplication.context.resources.getString(R.string.preview_education)))
            list.add(BCategory("5", name = BeeTVApplication.context.resources.getString(R.string.child)))
        }
        if (type != Constants.TYPE_CATEGORY.TV.type && type != Constants.TYPE_CATEGORY.FAVORITE.type && type != Constants.TYPE_CATEGORY.PLAYBACK.type) {

            list.add(0, BCategory(name = BeeTVApplication.context.resources.getString(R.string.update_order)))
            list.add(1, BCategory(name = BeeTVApplication.context.resources.getString(R.string.popular_order)))
        } else {
            if (type == Constants.TYPE_CATEGORY.TV.type)
                list.add(0, BCategory(name = BeeTVApplication.context.resources.getString(R.string.favorites)))
        }
        return list
    }

    fun getCategoryByParentId(parentId: String): MutableList<BCategory> {
        val list = mutableListOf<BCategory>()
        mCategories.forEach {
            if (it.parent_id == parentId) {
                list.add(it)
            }
        }
        return list
    }

    fun getCategoryByName(name: String): BCategory? {
        var category: BCategory? = null
        mCategories.forEach {
            if (it.name == name) {
                category = it
                return@forEach
            }
        }
        return category
    }

    fun getCategoryByID(id: String): BCategory? {
        var category: BCategory? = null
        mCategories.forEach {
            if (it.id == id) {
                category = it
                return@forEach
            }
        }
        return category
    }

    fun getCategoryIDByType(type: String): MutableList<String> {
        val idList = mutableListOf<String>()
        mCategories.forEach {
            if (it.group == type) {
                idList.add(it.id)
            }
        }
        return idList
    }
}