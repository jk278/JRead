/*
    设置UI模块
*/
package com.JRead.ui;

import static com.JRead.CommonUtils.setPopupWindow;
import static com.JRead.SPUtils.get;
import static com.JRead.SPUtils.put;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.JRead.SPUtils;
import com.JRead.Set;
import com.JRead.databinding.FgtPopUiBinding;

public class UiPopupFragment extends Fragment {

    private FgtPopUiBinding binding;
    private PopupWindow popupWindow;

    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FgtPopUiBinding.inflate(inflater, container, false);
        popupWindow = new PopupWindow(binding.getRoot(),900,1300,true);
        setPopupWindow(popupWindow,binding.getRoot(),requireActivity().getWindow());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        uiSetOnClickListener();
        HomeViewModel model = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        model.getSelected().observe(getViewLifecycleOwner(), integer -> {
            if (integer.equals(Set.POPUP_UI_DISMISS)) new Handler().post(() ->
                    popupWindow.dismiss());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // 设置 UI 的点击监听
    private void uiSetOnClickListener() {
        // 规定：button 的 id 与 sharedPreferences 的 key 必须除数字外相等！
        btnSetOnClickListener( binding.fontSize,"font_size",5,1);
        btnSetOnClickListener( binding.lineSpace,"line_space",4,2);
        btnSetOnClickListener( binding.lbSize,"lb_size",4,2);
        btnSetOnClickListener( binding.letterSpace,"letter_space",4,1);
        btnSetOnClickListener( binding.marginHorizontal,"margin_horizontal",4,1);
        btnSetOnClickListener( binding.theme,"theme",3,0);
    }
    private void btnSetOnClickListener ( SeekBar seekBar, String key, int amount,int init) {
        seekBar.setProgress(SPUtils.get(key+"_seekbar",init));
        for(int i=0; i< amount; i++){
            seekBar.setOnSeekBarChangeListener(listener(key));
        }
    }
    private SeekBar.OnSeekBarChangeListener listener(String key) {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Bundle result = new Bundle();
                result.putBoolean(key+i, true);
                SPUtils.put(key+"_seekbar",i);
                requireActivity().getSupportFragmentManager().setFragmentResult(key+i, result);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }

}
