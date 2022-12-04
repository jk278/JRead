/*
    随机文章模块
*/
package com.JRead.ui;

import static java.lang.String.valueOf;
import static com.JRead.SPUtils.get;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.JRead.SPUtils;
import com.JRead.Set;
import com.JRead.TextUtils;
import com.JRead.CommonUtils;
import com.JRead.databinding.FgtHomeBinding;

public class HomeFragment extends Fragment {

    private FgtHomeBinding binding;
    private HomeViewModel model;
    private TextView text;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //                             !!!!!!!!!!!!!!!!
        model = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        binding = FgtHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        text = binding.tvArticle;
        //model.getArticle().observe(getViewLifecycleOwner(), this::setViewText);
        setViewText(SPUtils.get1(Set.init(), ""));

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {
        super.onViewCreated(view,saveInstanceState);
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            "click", getViewLifecycleOwner(), (requestKey, result) -> {
            boolean click = result.getBoolean("click");
            if (click) {
                onClickTitle();
                Log.i("home fragment","click is execute");
            }
        });

        setUiClick();

        initSwipeRefresh(); // 下滑刷新

        // 设置侧边栏，fab
        model.select(Set.FAB_HOME); // ? 跟editor fragment 顺序不同，不能换。
        new Handler().post(()->model.select(Set.SET_SLIDER_HOME));

        model.getSelected().observe(getViewLifecycleOwner(), integer -> {
            if (integer.equals(Set.SCROLL_HOME_UP)) {
                new Handler().post(() -> binding.scrollView.smoothScrollTo(0,0));
            }
            if (integer.equals(Set.SET_ARTICLE)) {
                model.getArticle().observe(getViewLifecycleOwner(), this::setViewText);
            }
        });

    }

    // 设置界面点击响应
    private void setUiClick() {
        btnSetOnClick("font_size",   18,1,1,5);
        btnSetOnClick("line_space",   0,8,2,4);
        btnSetOnClick("lb_size",      1,3,3,4);
        btnSetOnClick("letter_space", 0,0.1f,4,4);
        btnSetOnClick("margin_horizontal",0,1,5,4);
        btnSetOnClick6();
    }
    private void btnSetOnClick(String key, float init, float multi, int type, int amount) {
        for(int i = 0; i< amount; i++){
            requireActivity().getSupportFragmentManager().setFragmentResultListener(key+i,
                    getViewLifecycleOwner(), TextUtils.listener(text,key, i, init , multi, type, false));
        }
    }
    private void btnSetOnClick6() {
        for(int i = 0; i< 3; i++) {
            requireActivity().getSupportFragmentManager().setFragmentResultListener("theme" + i,
                    getViewLifecycleOwner(), TextUtils.listener6(requireActivity(), "theme", i));
        }
    }

    public void onClickTitle() {
        int index = SPUtils.get1(Set.SOURCE_INDEX,-1); // 源索引
        int init = - ( index +1 )*50; // 初始值
        Log.d("home click init---",valueOf(init));
        Log.d("home click get1(-1)---", SPUtils.get1(-1,""));

        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0x11) {
                    String resultMsg = (String) msg.obj;
                    setViewText( resultMsg );
                    if(SPUtils.get("margin_horizontal", 0) == 0) TextUtils.renewMargin(text);
                    String s = SPUtils.get1(init, ""); // 初始赋值要判空
                    if ( CommonUtils.mismatchStrings(s.substring(0,s.indexOf("-split-")),Set.titleStrings) ){
                        int i = SPUtils.get1(index, 1);
                        if (i == Set.MAX_AMOUNT+1) i -= Set.MAX_AMOUNT;
                        SPUtils.put1(index, i+1); // setSlider 与下一次相同
                        Log.d("home click i---",valueOf(i));
                        SPUtils.put1(i+init, SPUtils.get1(init, ""));
                        new Handler().post(() -> model.select(new Integer[]{
                                Set.SET_SLIDER_HOME, Set.SCROLL_SLIDER_UP
                        }));
                    }
                    SPUtils.put1(init, resultMsg);
                }
            }
        };

        //联网获取文章
        new Thread(() -> {
            String website = SPUtils.get1( index+Set.WEBSITE_FROM+1, "");
            Log.i("home click website---",website);
            String rule = SPUtils.get1( index +Set.RULE_FROM+1, "");
            String string = CommonUtils.getTextFromWebsite(website,rule,true,false); // 若 false 则标题为空，会误判成初次加载

            Message tempMsg = handler.obtainMessage();
            tempMsg.what = 0x11;
            tempMsg.obj = string;
            handler.sendMessage(tempMsg);
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // 初始化下滑刷新
    private void initSwipeRefresh() {
        SwipeRefreshLayout swipeRefresh= binding.swipeLayout;
        swipeRefresh.setProgressViewOffset(false,0, 250);
        swipeRefresh.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            onClickTitle();
            swipeRefresh.setRefreshing(false);
        },300));
    }

    // 设置文本
    private void setViewText( String str) {
        //恢复缓存
        try {
            String[] strings = str.split("-split-");
            TextUtils.setArticleUI(text,strings[1],true);
            model.setTitle(strings[0]); // setArticle 已经包括 title
        }catch (Exception e) {
            Log.e("HomeFragment setViewText wrong:",e.toString());
            Log.e("此时，string 为---",str);
        }

        binding.scrollView.post(() -> binding.scrollView.smoothScrollTo(0,0));
    }

}