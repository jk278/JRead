package com.JRead;

import static com.JRead.SPUtils.get;
import static com.JRead.SPUtils.put;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;

import android.os.Bundle;
import android.util.Log;

import com.JRead.databinding.ActivityMainBinding;
import com.JRead.databinding.FabMultiBinding;
import com.JRead.ui.HomeViewModel;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTheme(SPUtils.get("theme", R.style.Theme_阅1));
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        Log.d("MainActivity onCreate","START before setContentView");
        setContentView(binding.getRoot());

        new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_editor).build();
        NavController navController = Navigation.findNavController(this,R.id.nav_host_fragment);
        // 会覆盖 drawer toggle, 注释掉后 fragment 中的 drawer toggle 也会报错
        CommonUtils.setNavDestination(navController, SPUtils.get("nav_destination",R.id.nav_home));

        initDrawer();

        initNavigationSelected(navController);

        Log.d("MainActivity onCreate","END");
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)|binding.drawerLayout.isDrawerOpen(GravityCompat.END))
            binding.drawerLayout.closeDrawers();
        else super.onBackPressed();
    }

    private void initDrawer() {
        DrawerLayout drawer = binding.drawerLayout;

        ActionBarDrawerToggle drawerToggle= new ActionBarDrawerToggle(this, drawer, R.string.drawer_open, R.string.drawer_close);
        drawerToggle.syncState();
        drawer.addDrawerListener(drawerToggle);

        HomeViewModel model = new ViewModelProvider(this).get(HomeViewModel.class);
        model.getSelected().observe(this, integer -> {
            if(integer.equals(Set.DRAWER_START_OPEN)) drawer.openDrawer(GravityCompat.START);
            if(integer.equals(Set.DRAWER_START_CLOSE)) drawer.closeDrawer(GravityCompat.START);
            if(integer.equals(Set.DRAWER_END_OPEN)) drawer.openDrawer(GravityCompat.END);
            if(integer.equals(Set.DRAWER_END_CLOSE)) drawer.closeDrawer(GravityCompat.END);
        });
    }

    /*
    ！！！！！之前切换错误，是因为 onViewCreated 中有个空 lambda !
    实际可以在子 fragment 中放置 navHost navView, activity 中操作 navControl.
    activity 中 initNavigationSelected, 不设只有默认切换效果。
     */
    private void initNavigationSelected(NavController navController) {

        FabMultiBinding fabMulti = FabMultiBinding.bind(findViewById(R.id.fab_multi));
        fabMulti.fabItem2.setOnClickListener(view -> {
            navController.navigate(R.id.nav_home);
            SPUtils.put("nav_destination",R.id.nav_home);
        });
        fabMulti.fabItem1.setOnClickListener(view -> {
            navController.navigate(R.id.nav_editor);
            SPUtils.put("nav_destination",R.id.nav_editor);
        });
    }

}
