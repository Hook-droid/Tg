package org.telegram.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private final java.util.ArrayList<GeoPoint> routePoints = new java.util.ArrayList<>();
    private TextView addPointButton;
    private TextView saveButton;

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

        root.addView(mapView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP, 0, 0, 0, viewOnly ? 0 : 140));

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
            root.addView(pin, LayoutHelper.createFrame(28, 40, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0, 0, 180));
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
        root.addView(bottom, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 140, Gravity.BOTTOM));

        coordsView = new TextView(context);
        coordsView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        coordsView.setTextSize(13);
        coordsView.setGravity(Gravity.CENTER);
        bottom.addView(coordsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 16, 12, 16, 0));

        addPointButton = new TextView(context);
        addPointButton.setTextColor(0xFFFFFFFF);
        addPointButton.setTextSize(15);
        addPointButton.setTypeface(AndroidUtilities.bold());
        addPointButton.setGravity(Gravity.CENTER);
        GradientDrawable addBg = new GradientDrawable();
        addBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueButton));
        addBg.setCornerRadius(AndroidUtilities.dp(10));
        addPointButton.setBackground(addBg);
        addPointButton.setOnClickListener(v -> addRoutePoint());
        addPointButton.setOnLongClickListener(v -> {
            clearRoute();
            return true;
        });
        bottom.addView(addPointButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 40, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 16, 34, 16, 0));
        updateAddButton();

        saveButton = new TextView(context);
        saveButton.setText("Set A Fake Live Location");
        saveButton.setTextColor(0xFFFFFFFF);
        saveButton.setTextSize(15);
        saveButton.setTypeface(AndroidUtilities.bold());
        saveButton.setGravity(Gravity.CENTER);
        GradientDrawable saveBg = new GradientDrawable();
        saveBg.setColor(0xFF4CD964);
        saveBg.setCornerRadius(AndroidUtilities.dp(10));
        saveButton.setBackground(saveBg);
        saveButton.setOnClickListener(v -> {
            if (routePoints.size() >= 2) {
                showSpeedDialog();
            } else {
                savePoint();
            }
        });
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

    private void updateAddButton() {
        if (addPointButton == null) {
            return;
        }
        if (routePoints.isEmpty()) {
            addPointButton.setText("+  Add Route Point");
        } else {
            addPointButton.setText("+  Add Route Point  (" + routePoints.size() + ")");
        }
        if (saveButton != null) {
            saveButton.setText(routePoints.size() >= 2 ? "Start Fake Route" : "Set A Fake Live Location");
        }
    }

    private void addRoutePoint() {
        if (mapView == null) {
            return;
        }
        org.osmdroid.api.IGeoPoint center = mapView.getMapCenter();
        GeoPoint point = new GeoPoint(center.getLatitude(), center.getLongitude());
        routePoints.add(point);

        org.osmdroid.views.overlay.Marker marker = new org.osmdroid.views.overlay.Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER);
        marker.setTextIcon(String.valueOf(routePoints.size()));
        mapView.getOverlays().add(marker);
        mapView.invalidate();
        updateAddButton();
    }

    private void clearRoute() {
        if (mapView == null) {
            return;
        }
        routePoints.clear();
        mapView.getOverlays().clear();
        mapView.invalidate();
        updateAddButton();
    }

    private void showSpeedDialog() {
        if (getParentActivity() == null) {
            return;
        }
        final int[] selected = {0};
        final String[] labels = {"Walking", "Driving", "Flying"};
        final int[] icons = {R.drawable.lyrx_ic_walk, R.drawable.lyrx_ic_car, R.drawable.lyrx_ic_plane};
        final String[] defaults = {"5", "60", "800"};
        final android.widget.EditText[] inputs = new android.widget.EditText[3];
        final android.widget.FrameLayout[] rows = new android.widget.FrameLayout[3];

        LinearLayout box = new LinearLayout(getParentActivity());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(4), AndroidUtilities.dp(12), 0);

        for (int i = 0; i < 3; i++) {
            final int index = i;
            android.widget.FrameLayout row = new android.widget.FrameLayout(getParentActivity());
            rows[i] = row;

            ImageView rowIcon = new ImageView(getParentActivity());
            rowIcon.setImageResource(icons[i]);
            rowIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            rowIcon.setColorFilter(new android.graphics.PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextGray2), android.graphics.PorterDuff.Mode.SRC_IN));
            row.addView(rowIcon, LayoutHelper.createFrame(24, 24, Gravity.LEFT | Gravity.CENTER_VERTICAL, 12, 0, 0, 0));

            TextView label = new TextView(getParentActivity());
            label.setText(labels[i]);
            label.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            label.setTextSize(16);
            row.addView(label, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 48, 0, 0, 0));

            TextView unit = new TextView(getParentActivity());
            unit.setText("km/h");
            unit.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
            unit.setTextSize(14);
            row.addView(unit, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 12, 0));

            android.widget.EditText input = new android.widget.EditText(getParentActivity());
            input.setText(defaults[i]);
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            input.setTextColor(Theme.getColor(Theme.key_dialogTextBlue2));
            input.setTextSize(16);
            input.setGravity(Gravity.RIGHT);
            input.setBackgroundDrawable(null);
            input.setSingleLine(true);
            inputs[i] = input;
            row.addView(input, LayoutHelper.createFrame(70, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 52, 0));

            row.setOnClickListener(v -> {
                selected[0] = index;
                for (int a = 0; a < 3; a++) {
                    GradientDrawable bg = new GradientDrawable();
                    bg.setCornerRadius(AndroidUtilities.dp(10));
                    bg.setColor(a == index ? Theme.getColor(Theme.key_listSelector) : 0x00000000);
                    rows[a].setBackground(bg);
                }
            });
            box.addView(row, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 52));
        }
        rows[0].performClick();

        org.telegram.ui.ActionBar.AlertDialog.Builder builder = new org.telegram.ui.ActionBar.AlertDialog.Builder(getParentActivity());
        builder.setTitle("Movement Speed");
        builder.setView(box);
        builder.setNegativeButton(org.telegram.messenger.LocaleController.getString(R.string.Cancel), null);
        builder.setPositiveButton("Start", (dialog, which) -> {
            float speed = 5f;
            try {
                speed = Float.parseFloat(inputs[selected[0]].getText().toString().trim());
            } catch (Exception ignore) {
            }
            if (speed <= 0f) {
                speed = 5f;
            }
            saveRoute(speed);
        });
        builder.show();
    }

    private void saveRoute(float speedKmh) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < routePoints.size(); i++) {
            if (i > 0) {
                sb.append(";");
            }
            sb.append(String.format(java.util.Locale.US, "%.6f,%.6f", routePoints.get(i).getLatitude(), routePoints.get(i).getLongitude()));
        }
        SharedConfig.lyrxFakeRoute = sb.toString();
        SharedConfig.lyrxFakeSpeed = speedKmh;
        SharedConfig.lyrxFakeRouteStart = System.currentTimeMillis();
        SharedConfig.lyrxFakeLat = (float) routePoints.get(0).getLatitude();
        SharedConfig.lyrxFakeLon = (float) routePoints.get(0).getLongitude();
        MessagesController.getGlobalMainSettings().edit()
                .putString("lyrxFakeRoute", SharedConfig.lyrxFakeRoute)
                .putFloat("lyrxFakeSpeed", SharedConfig.lyrxFakeSpeed)
                .putLong("lyrxFakeRouteStart", SharedConfig.lyrxFakeRouteStart)
                .putFloat("lyrxFakeLat", SharedConfig.lyrxFakeLat)
                .putFloat("lyrxFakeLon", SharedConfig.lyrxFakeLon)
                .apply();
        try {
            BulletinFactory.global().createSimpleBulletin(R.raw.chats_infotip, "Fake Route Started").show();
        } catch (Throwable ignore) {
        }
        finishFragment();
    }

    private void savePoint() {
        if (mapView == null) {
            return;
        }
        try {
            org.osmdroid.api.IGeoPoint center = mapView.getMapCenter();
            SharedConfig.lyrxFakeLat = (float) center.getLatitude();
            SharedConfig.lyrxFakeLon = (float) center.getLongitude();
            SharedConfig.lyrxFakeRoute = "";
            SharedConfig.lyrxFakeSpeed = 0f;
            MessagesController.getGlobalMainSettings().edit()
                    .putFloat("lyrxFakeLat", SharedConfig.lyrxFakeLat)
                    .putFloat("lyrxFakeLon", SharedConfig.lyrxFakeLon)
                    .putString("lyrxFakeRoute", "")
                    .putFloat("lyrxFakeSpeed", 0f)
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
