package com.JRead.ui;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.JRead.Set;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> article, title;
    private final MutableLiveData<Integer> selected = new MutableLiveData<>();

    public HomeViewModel() {
        article = new MutableLiveData<>();
        title = new MutableLiveData<>();
        // 进应用恢复数据
        //article.setValue(get1(init(), "")); // 第一次这个值为空，所以取一个，让它刷新
        Log.i("home view model","初始从缓存取值");
    }

    public MutableLiveData<String> getArticle() { return article; }
    public LiveData<String> getTitle() { return title; }

    public void setTitle(String str) { title.setValue(str); }

    public void setArticle(String str) {
        select(Set.SET_ARTICLE);
        article.setValue(str);
    }

    public void select(Integer integer) { selected.setValue(integer); }
    public LiveData<Integer> getSelected() { return selected; }

    public void select(Integer[] integers) {
        for (Integer integer : integers) {
            selected.setValue(integer);
        }
    }

}