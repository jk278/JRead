/*
    笔记模块
*/
package com.JRead.ui;

import static com.JRead.SPUtils.get;
import static com.JRead.SPUtils.put;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.JRead.CommonUtils;
import com.JRead.HeightProvider;
import com.JRead.NoScrollFocusScrollView;
import com.JRead.SPUtils;
import com.JRead.Set;
import com.JRead.TextUtils;
import com.JRead.R;
import com.JRead.databinding.FgtEditorLocalBinding;

public class EditorFragment extends Fragment {

    private FgtEditorLocalBinding binding;
    private EditText editor;
    private NoScrollFocusScrollView scrollView;
    private HomeViewModel model;
    private int keyboardHeight, id;
    private double beforePressY;
    private final static int EDITOR_BOTTOM_SPACE = 89; // (editor bottom -cursorY) -HEADER_SPACE =572 -483

    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FgtEditorLocalBinding.inflate(inflater, container, false);
        // 初始化赋值
        initValue();
        // 获取根视图
        View root = binding.getRoot();
        // 提前滚动到上次位置。    之前只能用 scroll post
        root.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                scrollView.getViewTreeObserver().removeOnPreDrawListener(this);
                scrollView.setScrollY(SPUtils.get("scroll_y",0));
                return false;
            }
        });
        // 设置正文内容与格式，与笔记标题
        setTextFromCache();
        // 空块高度，scroll view 之后
        binding.blockNull.getLayoutParams().height = keyboardHeight-EDITOR_BOTTOM_SPACE;
        // 禁用右 drawer
        DrawerLayout drawer = requireActivity().findViewById(R.id.drawer_layout);
        //drawer.setScrimColor(Color.TRANSPARENT);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        // 创建视图
        return root;
    }

    /*
    ！！！！！之前切换错误，是因为 onViewCreated 中有个空 lambda !
    实际可以在子 fragment 中放置 navHost navView, activity 中操作 navControl.
    fragment 中 initNavigationSelected, 不设也有默认切换效果！
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("ViewLifecycleOwner","EditorFragment, onViewCreated");
        // 保存笔记标题
        processMainChanged();
        // 文本变化监听
        processTitleChanged();
        // 设置 toolbar 标题
        model.setTitle("笔记");
        // ?? 待定
        // 触摸监听
        scrollView.setOnTouchListener((view13, motionEvent) -> {
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN) {
                beforePressY = motionEvent.getY();
                Log.d("beforePressY",String.valueOf(beforePressY));
            }
            if(motionEvent.getAction()==MotionEvent.ACTION_UP) {
                double nowPressY = motionEvent.getY();
                if (nowPressY-beforePressY > 10) {
                    CommonUtils.hideKeyboard(requireActivity());
                    Log.d("nowPressY",String.valueOf(nowPressY));
                }
            }
            return false;
        });
        // 设置 SoftInputMode
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        // 键盘高度监听
        new HeightProvider(requireActivity()).init().setHeightListener(new HeightProvider.HeightListener() {
            final int SPACE = 220; // 键盘上面留空
            @Override public void onHeightChanged(int height) {
                if (height!=0 && height+SPACE!=keyboardHeight) {
                    keyboardHeight = height+SPACE;
                    try {
                        binding.blockNull.getLayoutParams().height = keyboardHeight-EDITOR_BOTTOM_SPACE;
                    } catch (Exception e) {
                        Log.e("HeightProvider blockNull", e.toString());
                    }
                    SPUtils.put("keyboard_height", keyboardHeight);
                }
            }
        });
        // 初始获焦，隐藏光标
        editor.requestFocus();
        editor.setCursorVisible(false);
        /* 底部空块 点击。隔一段时间就会偏长。
        requireCursorAwayTop = scroll bottom - 键盘高;
         */
        binding.blockNull.setOnClickListener(view1 -> {
            editor.setSelection(editor.getText().toString().length());
            CommonUtils.showKeyboard(requireActivity(),editor);
            CommonUtils.editorAutoScroll(scrollView, editor, keyboardHeight);
            editor.setCursorVisible(true); // 放在最后才有效
            try {
                binding.blockNull.getLayoutParams().height = keyboardHeight-EDITOR_BOTTOM_SPACE;
            } catch (Exception e) {
                Log.e("HeightProvider blockNull", e.toString());
            }
            Log.i("相关尺寸参数如下---",
                    "\nscrollY---:  "+scrollView.getScrollY() +"\ncursorY---:  "+ CommonUtils.getCursorY(editor)
                    +"\neditor bottom---:  "+editor.getBottom() +"\nkeyboardHeight---:  "+keyboardHeight
                    +"\nblock height---:  "+binding.blockNull.getHeight() +"\nscroll bottom---:  "+scrollView.getBottom()
                    +"\nblock bottom---:  "+binding.blockNull.getBottom() +"\nrequireBlockBottom---" +(editor.getBottom()+keyboardHeight)
            );
        });
        // editor 点击
        editor.setOnClickListener(view12 -> {
            try { // 点监听回车行，移动光标。光标没在回车行显示。
                editor.setCursorVisible(true);
                int index = editor.getSelectionStart();
                String str = editor.getText().subSequence(index-2, index+2).toString();
                Matcher matcher = Pattern.compile(".\n\n.").matcher(str);
                if (matcher.find()) {
                    editor.setSelection(index-1);
                }
                Log.d("subSequence", editor.getText().subSequence(index-2,index+2).toString());
            } catch (Exception e) {
                Log.d("监听回车失败---","已到达底部");
            }
            CommonUtils.editorAutoScroll(scrollView, editor, keyboardHeight);
        });
        /*/ 获焦点模拟点击。光标没在回车行显示。
        editor.setOnFocusChangeListener((view14, b) -> {
            if (b) { // 获焦点模拟点击。光标没在回车行显示。
                editor.setCursorVisible(false);
                float [] cursor = getCursorXY(editor); // 下面这个 scrollView 就很玄学
                //scrollView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),MotionEvent.ACTION_DOWN,cursor[0],cursor[1],0));
                scrollView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),MotionEvent.ACTION_UP,cursor[0],cursor[1],0));
            }
        });
         */

        // 设置fab，初始打开也无效。狗日的，这个位置可以了。
        model.select(Set.FAB_EDITOR);
        // 设置侧边栏，放上面，start slider 还没有开始 onViewCreated 生命周期。狗日的，进应用前面的才生效。
        new Handler().post(() -> model.select(Set.SET_SLIDER_EDITOR)); // correct! 进入应用自动化才阻塞。
        // 接收事件
        model.getSelected().observe(getViewLifecycleOwner(), integer -> {
            if (integer.equals(Set.SCROLL_EDITOR_UP)) {
                new Handler().post(() -> scrollView.smoothScrollTo(0,0));
            } if (integer.equals(Set.SAVE_EDITOR)) {
                onSaveTextClick();
            } if (integer.equals(Set.ADD_NEW)) {
                addNew();
            } if (integer.equals(Set.SET_TEXT_EDITOR)) {
                setTextFromCache();
            }
        });

        // 设置界面点击响应
        setUiClick();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    // 初始化赋值
    private void initValue() {
        scrollView = binding.scrollEditor;
        editor = binding.editMain;
        keyboardHeight = SPUtils.get("keyboard_height",600);
        model = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }
    // 文本变化监听
    private void processMainChanged() {
        final int lineBreakSize = SPUtils.get("lb_size", 7);
        editor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void afterTextChanged(Editable editable) {
                TextUtils.shortenBlankSpace(editable,lineBreakSize,true);
                String str = editable.toString();
                if (id==0) {
                    SPUtils.put("text_main",str);
                } else {
                    SPUtils.put1(id,str);
                }
                // 文本变化
                CommonUtils.editorAutoScroll(scrollView, editor, keyboardHeight);
                SPUtils.put("scroll_y",scrollView.getScrollY());
                Log.d("scroll_y scroll---", String.valueOf(scrollView.getScrollY()));
                Log.d("cursor_y scroll---", String.valueOf(CommonUtils.getCursorY(editor)));
            }
        });
    }
    // 保存笔记标题
    private void processTitleChanged() {
        binding.editTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void afterTextChanged(Editable editable) {
                String str = editable.toString();
                if (id==0) {
                    SPUtils.put("text_title",str);
                } else {
                    SPUtils.put1(id- Set.EDITOR_MAX,str);
                }
            }
        });
    }
    // 设置正文内容与格式，与笔记标题
    private void setTextFromCache() {
        id = SPUtils.get1(Set.EDITOR_INDEX,0);
        Log.e("setTextFromCache id----------", String.valueOf(id));
        String textMain, textTitle;
        if ( id==0 ) {
            textMain = SPUtils.get("text_main","");
            textTitle = SPUtils.get("text_title","");
        } else {
            textMain = SPUtils.get1(id,"");
            textTitle = SPUtils.get1(id- Set.EDITOR_MAX,"");
        }
        TextUtils.setArticleUI(editor, textMain,true);
        binding.editTitle.setText(textTitle);
    }
    // 保存笔记点击事件
    private void onSaveTextClick() {
        String text = editor.getText().toString();
        String title = binding.editTitle.getText().toString();
        if ( !text.equals("") || !title.equals("") ) {
            //int id = get1(Set.EDITOR_INDEX,0);
            if (id==0) {
                int amount = SPUtils.get1(Set.EDITOR_AMOUNT,0); // 顺序存，还有非顺序存。
                SPUtils.put1(Set.EDITOR_AMOUNT,amount+1);
                CommonUtils.putCacheTogether(Set.EDITOR_FROM -amount, text,title,true); // 还有存已打开文件。
                Log.e("onSaveTextClick","\namount"+ amount +"title" +title);
            } else {
                CommonUtils.putCacheTogether(id,text,title,false); // 打开项 存
            }
            model.select(Set.SET_SLIDER_EDITOR);
            editor.setCursorVisible(false);
            //Log.e("setSliderEditor","没执行!");
        }
    }
    private void addNew() {
            id=0;
            SPUtils.put1(Set.EDITOR_INDEX,0); // 打开项改为即时缓存 !!!
            editor.setText(""); // 清空显示内容！！！非零不能清空！!!!
            binding.editTitle.setText("");
    }
    // 设置界面点击响应
    private void setUiClick() {
        btnSetOnClick("font_size", 18, 1, 1, 5);
        btnSetOnClick("line_space", 0, 8, 2, 4);
        btnSetOnClick("lb_size", 1, 3, 3, 4);
        btnSetOnClick("letter_space", 0, 0.1f, 4, 4);
        btnSetOnClick("margin_horizontal", 0, 1, 5, 4);
        btnSetOnClick6();
    }
    private void btnSetOnClick(String key, float init, float multi, int type, int amount) {
        for(int i = 0; i< amount; i++){
            requireActivity().getSupportFragmentManager().setFragmentResultListener(key+i,
                    getViewLifecycleOwner(), TextUtils.listener(editor, key, i, init, multi, type, true));
        }
    }
    private void btnSetOnClick6() {
        for(int i = 0; i< 3; i++) {
            requireActivity().getSupportFragmentManager().setFragmentResultListener("theme" + i,
                    getViewLifecycleOwner(), TextUtils.listener6(requireActivity(), "theme", i));
        }
    }

}