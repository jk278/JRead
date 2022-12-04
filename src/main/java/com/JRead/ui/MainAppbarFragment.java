/*
    appbar;标题、actionbar、fab
*/
package com.JRead.ui;

import static com.JRead.CommonUtils.closeFab;
import static com.JRead.CommonUtils.fabChangeImage;
import static com.JRead.CommonUtils.hideKeyboard;
import static com.JRead.CommonUtils.setPopupWindow;
import static com.JRead.CommonUtils.openFab;

import android.graphics.Color;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.JRead.SPUtils;
import com.JRead.Set;
import com.JRead.databinding.FgtAppbarMainBinding;
import com.JRead.databinding.PopSampleTextBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import com.JRead.CommonUtils;
import com.JRead.R;

public class MainAppbarFragment extends Fragment {


    private FgtAppbarMainBinding binding;
    private HomeViewModel model;

    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("AppbarFragment onCreateView","START");
        binding = FgtAppbarMainBinding.inflate(inflater, container, false);
        model = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        initToolbar();

        model.getTitle().observe(getViewLifecycleOwner(), binding.appbar.textTitle::setText);
        Log.d("AppbarFragment onCreateView","END");

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("ViewLifecycleOwner","MainAppbarFragment, onViewCreated");
        TextView title = binding.appbar.textTitle;
        title.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putBoolean("click", true);
            requireActivity().getSupportFragmentManager().setFragmentResult("click", result);
        });
        // 初始赋值，标题同步 commit ( 返回boolean )
        firstGetValue(view);
        // 自动刷新，防止刚存title就刷新。
        autoRefresh(title);
        // fab 点击动画、监听
        initFabMulti();
        // menu 声明 in fragment
        setHasOptionsMenu(true);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // 初次运行缓存赋值
    private void firstGetValue(View view) { // view 显示 snack bar
        if(SPUtils.get1(0, "").equals("")) { // 初始值判断，此处同步传值
            Snackbar snackbar = Snackbar.make(view, "\u3000点击标题或下滑更新内容", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("确定", click -> snackbar.dismiss()).setActionTextColor(Color.CYAN).show();
            SPUtils.put1(Set.NAME_FROM, "每日一文");
            SPUtils.put1(Set.WEBSITE_FROM, "https://meiriyiwen.com/");
            SPUtils.put1(Set.RULE_FROM, "p;.article_author,[style];h1;;.article_author;");
            SPUtils.put1(Set.ID_FORM, Set.INDEX_FROM);
            SPUtils.put1C(0, Set.titleStrings[0] + "-split-"); // 可能不需要,还是要的！
            SPUtils.put1(Set.NAME_FROM - 1, "读书网");
            SPUtils.put1(Set.WEBSITE_FROM - 1, "https://m.dushu.com/meiwen/random/"); // :containsOwn(读书导航),:contains(随机美文),:contains(关闭)
            SPUtils.put1(Set.RULE_FROM - 1, "p;:has(a),:containsOwn(读书导航);h1;;.border-right;");
            SPUtils.put1(Set.ID_FORM - 1, Set.INDEX_FROM - 1);
            Log.i("appbar fragment", "初始赋缓存值");
            // not(:has())
        }
    }
    private void autoRefresh(TextView title) {
        String s = SPUtils.get1(0, ""); // 自动刷新，防止刚存title就刷新。
        if( !CommonUtils.mismatchStrings(s.substring(0,s.indexOf("-split-")),Set.titleStrings) ) {
            title.performClick(); // 如果没存完，click title 会存空值进缓存
            Log.i("appbar fragment", "延时刷新");
        } else {
            Log.i("appbar fragment", "未自动刷新");
            Log.i("此时，get1(0)---", SPUtils.get1(0, ""));
            Log.i("此时，第一个如果的判断值---", String.valueOf(!CommonUtils.mismatchStrings(SPUtils.get1(0, ""),Set.titleStrings)));
            // 改成 get text 之后会等 title ( model ) 设值再刷新
        }
    }

    private void initToolbar() {
        // toolbar
        Toolbar toolbar = binding.appbar.toolbar;
        //-----------------------------------直接写在下面要两个括号！！！！
        AppCompatActivity activity= (AppCompatActivity)requireActivity();
        toolbar.setTitle(""); // get1 support actionbar 之前，xml 中无效
        activity.setSupportActionBar(toolbar);
        Objects.requireNonNull(activity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        //activity.getSupportActionBar().setDisplayShowTitleEnabled(false); // 无标题
        toolbar.setNavigationOnClickListener(v1 -> {
            model.select(Set.DRAWER_START_OPEN);
            hideKeyboard(requireActivity());
        });

        // 此处一坑：从fragmentContainerView改为fragment 之后，
        // drawerToggle 一在 appbar main fragment 中错误！
        /*/ drawer toggle
        DrawerLayout drawer = requireActivity().findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle= new ActionBarDrawerToggle(requireActivity(), drawer, R.string.drawer_open, R.string.drawer_close);
        drawerToggle.syncState();
        drawer.addDrawerListener(drawerToggle);
         */
    }

    /*
    ！！！！！之前切换错误，是因为 onViewCreated 中有个空 lambda !
    实际可以在子 fragment 中放置 navHost navView, activity 中操作 navControl.
    fragment 中 initNavigationSelected, 不设也有默认切换效果！

    private void initNavigationSelected() {
        binding.navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id==R.id.nav_home) {
                Log.e("Bottom item selected---","home");
                return true;
            } else if (id==R.id.nav_editor) {
                binding.appbar.textTitle.setText("");
                Log.e("Bottom item selected---","editor");
                return true;
            }
            return false;
        });
    }

     */

    // 创造 actionBar 的 menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.main, menu);
    }

    // 设置 actionBar menu 点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.set_ui) {
            showPopupUi();
            hideKeyboard(requireActivity());
            return true;
        } else if(id == R.id.about_us) {
            showAboutUs();
            hideKeyboard(requireActivity());
            return true;
        } else if(id == R.id.help) {
            showHelp();
            hideKeyboard(requireActivity());
            return true;
        }
        return  super.onOptionsItemSelected(item);
    }

    // 设置 fab 的点击监听
    private boolean fabOpen = false;
    private void initFabMulti() { // fab 点击动画、监听
        FloatingActionButton fab=binding.fabMulti.fab, fab2=binding.fabMulti.fab2, fab3=binding.fabMulti.fab3
                ,fabItem1 = binding.fabMulti.fabItem1, fabItem2 = binding.fabMulti.fabItem2;
        // 设置 fabMulti
        model.getSelected().observe(getViewLifecycleOwner(),integer -> {
            if (integer.equals(Set.FAB_HOME)) {
                if (fabOpen) {
                    fabChangeImage(fab2,R.drawable.ic_baseline_west_24,fab2.getCompatElevation());
                } else fab2.setImageResource(R.drawable.ic_baseline_west_24);
                fab2.setOnClickListener(view1 -> model.select(Set.DRAWER_END_OPEN));
                fab3.setOnClickListener(view1 -> model.select(Set.SCROLL_HOME_UP));
            } if (integer.equals(Set.FAB_EDITOR)) {
                if (fabOpen) {
                    fabChangeImage(fab2,R.drawable.ic_baseline_save_alt_24,fab2.getCompatElevation());
                } else fab2.setImageResource(R.drawable.ic_baseline_save_alt_24);
                fab2.setOnClickListener(view1 -> {
                    model.select(Set.SAVE_EDITOR);
                    model.select(Set.ADD_NEW);
                });
                fab3.setOnClickListener(view1 -> model.select(Set.SCROLL_EDITOR_UP));
            }
        });

        fab.setOnClickListener(view -> {
            if(!fabOpen){
                fabOpen =true;
                openFab(fab,fab2,fab3,fab.getRotation(),fab2.getTranslationY()
                        ,fab3.getTranslationY(),fab2.getCompatElevation(),fab3.getCompatElevation()
                        ,fabItem1,fabItem2,fabItem1.getTranslationX(),fabItem2.getTranslationX());
            }  else {
                fabOpen = false;
                closeFab(fab,fab2,fab3,fab.getRotation(),fab2.getTranslationY()
                        ,fab3.getTranslationY(),fab2.getCompatElevation(),fab3.getCompatElevation()
                        ,fabItem1,fabItem2,fabItem1.getTranslationX(),fabItem2.getTranslationX());
            }
        });
    }

    // 显示“UI”设置
    public void showPopupUi() {
        requireActivity().getSupportFragmentManager().beginTransaction().add(new UiPopupFragment(),"ui").commit();
    }

    // 显示“关于”界面
    private void showAboutUs() {
        PopSampleTextBinding popBinding = PopSampleTextBinding.inflate(getLayoutInflater(), binding.getRoot(),false);
        popBinding.sampleText.setAutoLinkMask(Linkify.ALL);
        popBinding.sampleText.setMovementMethod(LinkMovementMethod.getInstance());
        popBinding.sampleText.setText(R.string.about_us_details);
        PopupWindow popupWindow = new PopupWindow(popBinding.getRoot(),900,ViewGroup.LayoutParams.WRAP_CONTENT,true);
        setPopupWindow(popupWindow,popBinding.getRoot(), requireActivity().getWindow());
    }

    // 显示“帮助”界面
    private void showHelp() {
        PopSampleTextBinding popBinding = PopSampleTextBinding.inflate(getLayoutInflater(), binding.getRoot(),false);
        popBinding.sampleText.setText(R.string.help_details);
        PopupWindow popupWindow = new PopupWindow(popBinding.getRoot(),900,900,true);
        setPopupWindow(popupWindow,popBinding.getRoot(), requireActivity().getWindow());
    }

}
