package com.alisteners.pengaduan.presentation.operator.dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.alisteners.pengaduan.R;
import com.alisteners.pengaduan.data.remote.config.ApiConfig;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONException;

public class DashboardOperatorFragment extends Fragment {

    private static final String TAG = "DashboardOperator";
    private TabLayout tabLayout;
    private RequestQueue requestQueue;
    private final Handler badgePollingHandler = new Handler(Looper.getMainLooper());
    private Runnable badgePollingRunnable;
    private static final long BADGE_POLLING_INTERVAL = 30000; // Cek setiap 30 detik

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_operator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbarOperator);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        tabLayout = view.findViewById(R.id.tabLayoutOperator);
        ViewPager2 viewPager = view.findViewById(R.id.viewPagerOperator);
        requestQueue = Volley.newRequestQueue(requireContext());

        OperatorPagerAdapter pagerAdapter = new OperatorPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Baru");
                    break;
                case 1:
                    tab.setText("Diproses");
                    break;
                case 2:
                    tab.setText("Selesai");
                    break;
            }
        }).attach();

        setupPollingBadge();
    }

    private void setupPollingBadge() {
        badgePollingRunnable = () -> {
            fetchNewReportCount();
            badgePollingHandler.postDelayed(badgePollingRunnable, BADGE_POLLING_INTERVAL);
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        badgePollingHandler.post(badgePollingRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        badgePollingHandler.removeCallbacks(badgePollingRunnable);
    }

    private void fetchNewReportCount() {
        String url = ApiConfig.BASE_URL + "get_new_report_count.php";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if ("success".equals(response.getString("status"))) {
                            int count = response.getInt("jumlah_baru");
                            updateBadge(count);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error: " + error.toString());
                }
        );
        requestQueue.add(request);
    }

    private void updateBadge(int count) {
        TabLayout.Tab tabBaru = tabLayout.getTabAt(0); // Ambil tab "Baru" (posisi 0)
        if (tabBaru != null) {
            if (count > 0) {
                BadgeDrawable badge = tabBaru.getOrCreateBadge();
                badge.setNumber(count);

                // Mengatur posisi badge agar tidak menutupi teks
                badge.setBadgeGravity(BadgeDrawable.TOP_END);
                badge.setHorizontalOffset(10); // Geser ke kanan (eksperimen dengan nilai ini)
                badge.setVerticalOffset(10);   // Geser ke bawah (eksperimen dengan nilai ini)

            } else {
                tabBaru.removeBadge();
            }
        }
    }
}