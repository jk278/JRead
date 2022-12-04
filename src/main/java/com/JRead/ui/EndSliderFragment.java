/*
    右侧边栏
*/
package com.JRead.ui;

import static java.lang.String.valueOf;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import com.JRead.CommonUtils;
import com.JRead.EndSliderAdapter;
import com.JRead.SPUtils;
import com.JRead.Set;
import com.JRead.R;
import com.JRead.databinding.FgtSliderEndBinding;

public class EndSliderFragment extends Fragment {

    private FgtSliderEndBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FgtSliderEndBinding.inflate(inflater, container, false);

        setSlider(requireActivity());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HomeViewModel model =
                new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        model.getSelected().observe(getViewLifecycleOwner(), integer -> {
            if(integer.equals(Set.SET_SLIDER_END)) setSlider(requireActivity());
            if(integer.equals(Set.SCROLL_SLIDER_UP)) new Handler().post(() ->
                    binding.sliderScroll.fullScroll(View.FOCUS_UP));
        });

        TextView textCreate = binding.textSourceCreate;
        textCreate.setOnClickListener(view1 -> {
            ObjectAnimator.ofFloat(textCreate,"textSize",20,17).setDuration(200).start();
            binding.articleSource.performClick();
        });
        binding.articleSource.setOnClickListener(view2 -> {
            FragmentManager manager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            Fragment fragment = manager.findFragmentByTag("source");
            transaction.setCustomAnimations(R.anim.push_in,R.anim.push_out);
            transaction.hide(Objects.requireNonNull(requireActivity().getSupportFragmentManager().findFragmentByTag("appbar")));
            if(fragment==null) {
                // fragment container view 改成 fragment 之后，加到 appbar main 中就不显示了。
                transaction.add(R.id.drawer_layout,new ArticleSourceFragment(),"source");
                // great!!! transaction.setReorderingAllowed(true).addToBackStack("");
            }
            else {
                transaction.show(fragment);
            }
            transaction.commit();
            model.select(Set.DRAWER_END_CLOSE);
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // 设置侧边栏
    private void setSlider(Context context) {
        List<Integer> mList = CommonUtils.getSourceIdList();
        RecyclerView rv = binding.rvView;
        rv.setLayoutManager(new LinearLayoutManager(context));
        EndSliderAdapter adapter=new EndSliderAdapter(context, CommonUtils.getSourceNameList(mList));
        rv.setAdapter(adapter);

        int[] id = mList.stream().mapToInt(x->x).toArray();
        adapter.setOnItemClickListener((view, position) -> {
            HomeViewModel model = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
            SPUtils.put1(Set.SOURCE_INDEX,id[position]);
            //int index = get(valueOf(Set.CACHE_INDEX_INDEX),-1); // 源索引
            int init = (-1 - id[position]) * Set.MAX_AMOUNT;
            Log.i("end slider init",valueOf(init));
            if(SPUtils.get1(init, "").equals("")) {
                SPUtils.put1(init,Set.titleStrings[0]+"-split-...");  // 待定
                Bundle result = new Bundle();
                result.putBoolean("click", true);
                Log.i("end slider init 为空，点击刷新",valueOf(init));
                requireActivity().getSupportFragmentManager().setFragmentResult("click", result);
            } else model.setArticle(SPUtils.get1(init,Set.titleStrings[0]+"-split-..."));

            model.select(new Integer[]{
                    Set.SCROLL_HOME_UP,Set.DRAWER_END_CLOSE,Set.SET_SLIDER_HOME
            });
        });
    }

}