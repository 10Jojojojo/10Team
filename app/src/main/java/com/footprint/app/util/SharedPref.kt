package com.footprint.app.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

//object SharedPref {
    const val PREF_KEY = "YOUTUBE_PREF"
    fun setString(context: Context?, key: String?, value: String?) {
        context ?: return

        val sp = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
        sp.edit().putString(key, value).apply()
    }

    fun Context?.getString( key: String?, defaultVal: String): String {
        this ?: return defaultVal

        val sp = this.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
        return sp.getString(key, defaultVal) ?: ""
//    }
//    fun saveCategory(context:Context,items :ArrayList<DataModel>) {
//        val editor =context.getSharedPreferences("spf",Context.MODE_PRIVATE).edit()
//        val gson = GsonBuilder().create()
//        editor.putString("categoryItems", gson.toJson(items))
//        editor.apply()
//    }
//
//    fun getCategory(context:Context): ArrayList<DataModel> {
//        val sharedPreferences =context.getSharedPreferences("spf",Context.MODE_PRIVATE)
//        val savedata = sharedPreferences.getString("categoryItems", "") ?:""
//        val gson = Gson()
//        val type = object : TypeToken<ArrayList<DataModel>>() {}.type
//        return  if (savedata.isNotEmpty()) {
//            gson.fromJson(savedata, type)
//        } else {
//            arrayListOf()
//        }
//    }
}