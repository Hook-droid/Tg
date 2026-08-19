package org.telegram.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;

public class LyrxMapPickerActivity extends BaseFragment {

    private MapView mapView;
    private TextView coordsView;

    private final boolean viewOnly;
    private final double viewLat;
    private final double viewLon;
    private final String viewTitle;

    public LyrxMapPickerActivity() {
        this.viewOnly = false;
        this.viewLat = 0;
        this.viewLon = 0;
        this.viewTitle = null;
    }

    public LyrxMapPickerActivity(double lat, double lon, String title) {
        this.viewOnly = true;
        this.viewLat = lat;
        this.viewLon = lon;
        this.viewTitle = title;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(viewOnly ? (viewTitle == null || viewTitle.length() == 0 ? "Location" : viewTitle) : "Pick A Location");
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
            }
        });

        try {
            Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
            Configuration.getInstance().setUserAgentValue(ApplicationLoader.applicationContext.getPackageName());
            Configuration.getInstance().setOsmdroidBasePath(new java.io.File(context.getCacheDir(), "osmdroid"));
            Configuration.getInstance().setOsmdroidTileCache(new java.io.File(context.getCacheDir(), "osmdroid/tiles"));
        } catch (Throwable ignore) {
        }

        FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        mapView = new MapView(context);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setHorizontalMapRepetitionEnabled(false);
        mapView.setVerticalMapRepetitionEnabled(false);
        mapView.getZoomController().setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER);

        double startLat;
        double startLon;
        if (viewOnly) {
            startLat = viewLat;
            startLon = viewLon;
            mapView.getController().setZoom(15.0);
        } else {
            startLat = SharedConfig.lyrxFakeLat != 0f ? SharedConfig.lyrxFakeLat : 41.0082;
            startLon = SharedConfig.lyrxFakeLon != 0f ? SharedConfig.lyrxFakeLon : 28.9784;
            mapView.getController().setZoom(SharedConfig.lyrxFakeLat != 0f ? 14.0 : 5.0);
        }
        mapView.getController().setCenter(new GeoPoint(startLat, startLon));

        root.addView(mapView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP, 0, 0, 0, viewOnly ? 0 : 96));

        ImageView pin = new ImageView(context);
        pin.setImageResource(R.drawable.map_pin2);
        pin.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (viewOnly) {
            org.osmdroid.views.overlay.Marker marker = new org.osmdroid.views.overlay.Marker(mapView);
            marker.setPosition(new GeoPoint(startLat, startLon));
            marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM);
            marker.setIcon(context.getResources().getDrawable(R.drawable.map_pin2));
            mapView.getOverlays().add(marker);
            pin.setVisibility(View.GONE);
        } else {
            root.addView(pin, LayoutHelper.createFrame(28, 40, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0, 0, 136));
        }

        if (viewOnly) {
            fragmentView = root;
            return fragmentView;
        }

        FrameLayout bottom = new FrameLayout(context);
        GradientDrawable bottomBg = new GradientDrawable();
        bottomBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        bottomBg.setCornerRadii(new float[]{AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), 0, 0, 0, 0});
        bottom.setBackground(bottomBg);
        root.addView(bottom, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 96, Gravity.BOTTOM));

        coordsView = new TextView(context);
        coordsView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        coordsView.setTextSize(13);
        coordsView.setGravity(Gravity.CENTER);
        bottom.addView(coordsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 16, 12, 16, 0));

        TextView saveButton = new TextView(context);
        saveButton.setText("Set A Fake Live Location");
        saveButton.setTextColor(0xFFFFFFFF);
        saveButton.setTextSize(15);
        saveButton.setTypeface(AndroidUtilities.bold());
        saveButton.setGravity(Gravity.CENTER);
        GradientDrawable saveBg = new GradientDrawable();
        saveBg.setColor(0xFF4CD964);
        saveBg.setCornerRadius(AndroidUtilities.dp(10));
        saveButton.setBackground(saveBg);
        saveButton.setOnClickListener(v -> savePoint());
        bottom.addView(saveButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 16, 0, 16, 8));

        mapView.addMapListener(new org.osmdroid.events.MapListener() {
            @Override
            public boolean onScroll(org.osmdroid.events.ScrollEvent event) {
                updateCoords();
                return false;
            }

            @Override
            public boolean onZoom(org.osmdroid.events.ZoomEvent event) {
                updateCoords();
                return false;
            }
        });
        updateCoords();

        fragmentView = root;
        return fragmentView;
    }

    private void updateCoords() {
        if (mapView == null || coordsView == null) {
            return;
        }
        try {
            org.osmdroid.api.IGeoPoint center = mapView.getMapCenter();
            coordsView.setText(String.format(java.util.Locale.US, "%.5f, %.5f", center.getLatitude(), center.getLongitude()));
        } catch (Throwable ignore) {
        }
    }

    private void savePoint() {
        if (mapView == null) {
            return;
        }
        try {
            org.osmdroid.api.IGeoPoint center = mapView.getMapCenter();
            SharedConfig.lyrxFakeLat = (float) center.getLatitude();
            SharedConfig.lyrxFakeLon = (float) center.getLongitude();
            MessagesController.getGlobalMainSettings().edit()
                    .putFloat("lyrxFakeLat", SharedConfig.lyrxFakeLat)
                    .putFloat("lyrxFakeLon", SharedConfig.lyrxFakeLon)
                    .apply();
            try {
                BulletinFactory.global().createSimpleBulletin(R.raw.chats_infotip, "Fake Location Successfully Set").show();
            } catch (Throwable ignore) {
            }
        } catch (Throwable ignore) {
        }
        finishFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (mapView != null) {
            try {
                mapView.onDetach();
            } catch (Throwable ignore) {
            }
            mapView = null;
        }
    }
}
