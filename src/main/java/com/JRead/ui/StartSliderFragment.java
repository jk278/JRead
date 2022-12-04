/*
    左侧边栏
*/
package com.JRead.ui;

import static java.lang.String.valueOf;
import static com.JRead.CommonUtils.getEditorIdList;
import static com.JRead.CommonUtils.getEditorNameList;
import static com.JRead.CommonUtils.getTitleList;
import static com.JRead.SPUtils.get;
import static com.JRead.SPUtils.put;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.JRead.SPUtils;
import com.JRead.Set;
import com.JRead.StartSliderAdapter;
import com.JRead.AlphaUtils;
import com.JRead.CommonUtils;
import com.JRead.EndSliderAdapter;
import com.JRead.R;
import com.JRead.databinding.FgtSliderStartBinding;
import com.JRead.databinding.PopSliderStartMoreBinding;

public class StartSliderFragment extends Fragment {

    private FgtSliderStartBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FgtSliderStartBinding.inflate(inflater, container, false);

        onHeaderImageStart();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HomeViewModel model = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        model.getSelected().observe(getViewLifecycleOwner(), integer -> {
            if(integer.equals(Set.SET_SLIDER_HOME)) {
                if(canSetSlider()) setSlider(requireActivity(), getTitleList());
                else        setSlider(requireActivity(),new ArrayList<>());
                binding.collapsingToolbar.setTitle("最近文章");
            }
            if(integer.equals(Set.SET_SLIDER_EDITOR)) {
                setSliderEditor(requireActivity());
                binding.collapsingToolbar.setTitle("我的笔记");
                Log.e("setSliderEditor","execute!");
            }
            if(integer.equals(Set.SCROLL_SLIDER_UP)) new Handler().post(() ->
                binding.sliderScroll.fullScroll(View.FOCUS_UP));
        });

        TextView textMore = binding.moreStartText;
        textMore.setOnClickListener(view1 -> {
            ObjectAnimator.ofFloat(textMore,"textSize",20,17).setDuration(200).start();
            binding.moreStart.performClick();
        });
        binding.moreStart.setOnClickListener(view2 -> {
            PopSliderStartMoreBinding moreBinding = PopSliderStartMoreBinding.inflate(getLayoutInflater() ,binding.getRoot(),false);
            moreBinding.checkboxAllArticle.setChecked(SPUtils.get("checkbox_all_article",false));
            moreBinding.checkboxAllArticle.setOnCheckedChangeListener((compoundButton, b) -> {
                if(b) {
                    SPUtils.put1(Set.SHOWED_AMOUNT,50);
                    if (canSetSlider()) setSlider(requireActivity(), getTitleList());
                    SPUtils.put("checkbox_all_article",true);
                }
                else SPUtils.put("checkbox_all_article",false);

            });
            moreBinding.checkboxHeaderStart.setChecked(SPUtils.get("checkbox_header_start",false));
            moreBinding.checkboxHeaderStart.setOnCheckedChangeListener((compoundButton, b) -> {
                if(b) {
                    SPUtils.putC("checkbox_header_start",true); // 同步传值
                    onHeaderImageStart();
                }
                else {
                    binding.startHeaderImage.setImageDrawable(null);
                    SPUtils.put("checkbox_header_start",false);
                }
            });
            popupWindowMore(moreBinding.getRoot());

        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // 设置侧边栏
    private void setSlider(Context context, List<String> list) {
        RecyclerView rv = binding.rvView;
        rv.setLayoutManager(new LinearLayoutManager(context));
        StartSliderAdapter adapter=new StartSliderAdapter(context, list);
        rv.setAdapter(adapter);

        Log.i("start slider list",list.toString());

        adapter.setOnItemClickListener((view, position) -> {
            int i = SPUtils.get1( SPUtils.get1(Set.SOURCE_INDEX,-1), 2);
            Log.d("startSlider setOnClickListener i---",valueOf(i));
            int j ;
            if ( i - position > 1 ) j= i -position -1;
            else j = i - position -1 + SPUtils.get1(Set.SHOWED_AMOUNT,10);
            Log.d("startSlider setOnClickListener j---",valueOf(j));

            HomeViewModel model = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
            model.setArticle(SPUtils.get1(j+Set.init(),""));
            model.select(new Integer[]{
                    Set.SCROLL_HOME_UP,Set.DRAWER_START_CLOSE
            });
        });
    }

    // 设置侧边栏
    private void setSliderEditor(Context context) {
        List<Integer> mList = getEditorIdList();
        RecyclerView rv = binding.rvView;
        rv.setLayoutManager(new LinearLayoutManager(context));

        if (mList!=null) {
            EndSliderAdapter adapter=new EndSliderAdapter(context, getEditorNameList(mList));
            rv.setAdapter(adapter);
            // 点击事件
            int[] id = mList.stream().mapToInt(x->x).toArray(); // id 数组
            adapter.setOnItemClickListener((view, position) -> {
                HomeViewModel model = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
                SPUtils.put1(Set.EDITOR_INDEX,id[position]); // 把点击的项放进缓存 ！！！
                //int index = get(valueOf(Set.CACHE_INDEX_INDEX),-1); // 源索引
                Log.e("setSliderEditor setOnItemClickListener id---", String.valueOf(id[position]));
                model.select(new Integer[]{
                        Set.SAVE_EDITOR,Set.SET_TEXT_EDITOR,Set.SCROLL_HOME_UP,Set.DRAWER_START_CLOSE
                });
            });
        } else {
            EndSliderAdapter adapter=new EndSliderAdapter(context, new ArrayList<>());
            rv.setAdapter(adapter);
        }
    }

    private boolean canSetSlider() {
        int init = -( SPUtils.get1(Set.SOURCE_INDEX,-1) +1) *Set.MAX_AMOUNT;
        Log.i("start slider","receive set slider invitation");
        return !SPUtils.get1(init+1, "").equals("");
    }

    private void onHeaderImageStart() {
        if ( SPUtils.get("checkbox_header_start", false) ) {
            if ( timeToRenewImage() ) {
                onHeaderImageGetting();
            } else {
                loadImageFromCache();
            } Log.i("start slider onHeaderImageStart","触发，缓存为true");
        }
        else Log.i("start slider onHeaderImageStart","触发，缓存为false");
    }
    private boolean timeToRenewImage() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        if (day != SPUtils.get("day",0)) return true;
        else return SPUtils.get("hour_24", 0) < 12;
    }

    private void onHeaderImageGetting() {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0x11) {
                    Bitmap resultMsg = (Bitmap) msg.obj;
                    if (resultMsg!=null) {
                        binding.startHeaderImage.setImageBitmap(resultMsg);
                        SPUtils.put("day",Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                        SPUtils.put("hour_24",Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                        SPUtils.put("image_header_start",CommonUtils.bitmapToString(resultMsg));
                        Log.i("start header image renew---","correct!");
                    }
                    else {
                        loadImageFromCache();
                        Log.i("start header image renew---","failed!");
                    }
                }
            }
        };

        new Thread(() -> {
            Document doc = new Document("");
            try {
                doc = Jsoup.connect("https://feedx.fun/rss/bingwallpaper.xml").timeout(2000).get();
            } catch (IOException e) {
                Log.w("start header image failed to connect",e.toString());
                e.printStackTrace();
            }
            String string = doc.select("description").text(); // 转一次 text
            String s = Jsoup.parse(string).select("a").attr("href");
            Log.i("start header image string",string);
            Log.i("start header image s",s);
            Bitmap bitmap = null;
            try {
                bitmap = CommonUtils.getBitmap(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Message tempMsg = handler.obtainMessage();
            tempMsg.what = 0x11;
            tempMsg.obj = bitmap;
            handler.sendMessage(tempMsg);
        }).start();
    }

    private void loadImageFromCache() {
        Bitmap bitmap = CommonUtils.stringToBitmap(SPUtils.get("image_header_start",""));
        binding.startHeaderImage.setImageBitmap(bitmap);
    }

    private void popupWindowMore(View layout) {
        Window window = requireActivity().getWindow();
        PopupWindow popupWindow = new PopupWindow(layout,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,true);
        int alphaDownTime= 2, alphaUpTime= 3;
        float downUnit= 0.005f, upUnit= 0.005f;
        popupWindow.setContentView(layout);
        popupWindow.setAnimationStyle(R.style.popupWindowAnim);
        popupWindow.showAsDropDown(binding.moreStart,0,-380);
        new AlphaUtils(window, true, alphaDownTime, downUnit);
        // dismiss 要在 show 后面，之前弹窗不关闭是模拟器的问题
        popupWindow.setOnDismissListener(() -> new AlphaUtils(window,false, alphaUpTime, upUnit));
    }

}