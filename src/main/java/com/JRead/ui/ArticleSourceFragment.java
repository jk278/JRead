/*
    来源管理
*/
package com.JRead.ui;

import static com.JRead.SPUtils.get;
import static com.JRead.SPUtils.put;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;


import com.JRead.CommonUtils;
import com.JRead.SPUtils;
import com.JRead.Set;
import com.JRead.databinding.FgtSourceArticleBinding;
import com.JRead.databinding.PopSampleTextBinding;
import com.JRead.databinding.PopSourceEnsureBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import com.JRead.R;

public class ArticleSourceFragment extends Fragment {

    private FgtSourceArticleBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FgtSourceArticleBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        DrawerLayout drawer = requireActivity().findViewById(R.id.drawer_layout);
        drawer.setScrimColor(Color.TRANSPARENT); // 下层不变暗
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {
        super.onViewCreated(view,saveInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.push_in,R.anim.push_out);
                transaction.hide(Objects.requireNonNull(requireActivity().getSupportFragmentManager().findFragmentByTag("source")))
                        .show(Objects.requireNonNull(requireActivity().getSupportFragmentManager().findFragmentByTag("appbar")));
                transaction.commit();
            }
        });
        // 返回按钮
        binding.back.setOnClickListener(view1 -> {
            requireActivity().onBackPressed();
            CommonUtils.hideKeyboard(requireActivity());
        });
        // 添加来源
        binding.addManual.setOnClickListener(view2 -> {
            CommonUtils.hideKeyboard(requireActivity());

            onSourceProcessing(); // 包括内容预览
        });
        // html 复选框
        binding.checkboxHtml.setChecked(SPUtils.get("checkbox_html",false));
        binding.checkboxHtml.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b) SPUtils.put("html_checkbox",true);
            else SPUtils.put("html_checkbox",false);
        });
        // info 提示按钮
        binding.info.setOnClickListener(view12 -> {
            CommonUtils.hideKeyboard(requireActivity());
            PopSampleTextBinding textBinding = PopSampleTextBinding.inflate(getLayoutInflater() ,binding.getRoot(),false);
            textBinding.sampleText.setAutoLinkMask(Linkify.ALL);
            textBinding.sampleText.setText(R.string.info);
            textBinding.sampleText.setMovementMethod(LinkMovementMethod.getInstance());
            PopupWindow popupWindow = new PopupWindow(textBinding.getRoot(),900,ViewGroup.LayoutParams.WRAP_CONTENT,true);
            CommonUtils.setPopupWindow(popupWindow,textBinding.getRoot(), requireActivity().getWindow());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void onSourceProcessing() {
        String name=binding.edit1.getText().toString();
        String website=binding.edit2.getText().toString();
        String rule=binding.edit3.getText().toString();

        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0x11) {
                    String[] resultMsg = (String[]) msg.obj;
                    if(resultMsg[1].equals("1")) {
                        // 内容预览
                        PopSourceEnsureBinding ensureBinding = PopSourceEnsureBinding.inflate(getLayoutInflater(),binding.getRoot(),false);

                        String[] strings = resultMsg[0].split("-split-");
                        String string = "标题：\n"+strings[0]+strings[1];
                        ensureBinding.sourceEnsureText.setText(string);
                        PopupWindow popupWindow = new PopupWindow(ensureBinding.getRoot(),ViewGroup.LayoutParams.MATCH_PARENT,1500,true);
                        CommonUtils.setPopupWindow(popupWindow,ensureBinding.getRoot(), requireActivity().getWindow());
                        // 内容预览---点击事件
                        ensureBinding.cancel.setOnClickListener(view21 -> Objects.requireNonNull(popupWindow).dismiss());
                        ensureBinding.ensure.setOnClickListener(view22 -> {
                            int amount = SPUtils.get1(Set.SOURCE_AMOUNT,1);
                            SPUtils.put1(Set.SOURCE_AMOUNT,amount+1);
                            SPUtils.put1( Set.ID_FORM -amount, -amount-1);
                            SPUtils.put1( Set.NAME_FROM -amount, name);
                            SPUtils.put1( Set.WEBSITE_FROM -amount, website);
                            SPUtils.put1( Set.RULE_FROM -amount, rule);
                            HomeViewModel model = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
                            model.select(Set.SET_SLIDER_END);
                            Objects.requireNonNull(popupWindow).dismiss();
                            Snackbar.make(binding.getRoot(), "\u3000\u3000导入成功", Snackbar.LENGTH_SHORT)
                                    .setAction("Action",null).show();
                        });
                    }else Snackbar.make(binding.getRoot(), "\u3000\u3000解析规则格式错误", Snackbar.LENGTH_SHORT)
                            .setAction("Action",null).show();
                }
            }
        };
        //联网获取文章
        new Thread(() -> {
            String str="1";
            String strings = CommonUtils.getTextFromWebsite(website,rule,false, SPUtils.get("html_checkbox",false));
            if(strings.equals("")) str = "0";
            Message tempMsg = handler.obtainMessage();
            tempMsg.what = 0x11;
            tempMsg.obj = new String[]{strings,str};
            handler.sendMessage(tempMsg);
        }).start();
    }

}