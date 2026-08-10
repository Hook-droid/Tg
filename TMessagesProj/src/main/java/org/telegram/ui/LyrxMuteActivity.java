package org.telegram.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;

public class LyrxMuteActivity extends BaseFragment {

    private EditText idInput;
    private LinearLayout listContainer;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("Mute");
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        android.widget.ScrollView scroll = new android.widget.ScrollView(context);
        scroll.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(24));

        FrameLayout inputCard = new FrameLayout(context);
        GradientDrawable inBg = new GradientDrawable();
        inBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        inBg.setCornerRadius(AndroidUtilities.dp(16));
        inputCard.setBackground(inBg);

        idInput = new EditText(context);
        idInput.setHint("Enter ID");
        idInput.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        idInput.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        idInput.setTextSize(16);
        idInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        idInput.setBackgroundColor(Color.TRANSPARENT);
        inputCard.addView(idInput, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT, 16, 0, 16, 0));
        root.addView(inputCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 54));

        FrameLayout activeBtn = new FrameLayout(context);
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(0xFF3390EC);
        btnBg.setCornerRadius(AndroidUtilities.dp(16));
        activeBtn.setBackground(btnBg);
        TextView activeText = new TextView(context);
        activeText.setText("Active");
        activeText.setTextColor(0xFFFFFFFF);
        activeText.setTextSize(16);
        activeText.setTypeface(AndroidUtilities.bold());
        activeText.setGravity(Gravity.CENTER);
        activeBtn.addView(activeText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        LinearLayout.LayoutParams abParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50);
        abParams.topMargin = AndroidUtilities.dp(12);
        root.addView(activeBtn, abParams);

        activeBtn.setOnClickListener(v -> {
            String txt = idInput.getText().toString().trim();
            if (txt.length() == 0) {
                return;
            }
            long id;
            try {
                id = Long.parseLong(txt);
            } catch (Exception e) {
                BulletinFactory.of(this).createErrorBulletin("Invalid ID").show();
                return;
            }
            SharedConfig.lyrxMuteEnabled = true;
            SharedConfig.lyrxAddMute(id);
            idInput.setText("");
            AndroidUtilities.hideKeyboard(idInput);
            rebuildList();
            BulletinFactory.of(this).createSimpleBulletin(R.raw.chats_infotip, "Added to mute list").show();
        });

        TextView listHeader = new TextView(context);
        listHeader.setText("Blacklist");
        listHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        listHeader.setTextSize(14);
        listHeader.setTypeface(AndroidUtilities.bold());
        LinearLayout.LayoutParams lhParams = LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
        lhParams.topMargin = AndroidUtilities.dp(20);
        lhParams.leftMargin = AndroidUtilities.dp(6);
        lhParams.bottomMargin = AndroidUtilities.dp(8);
        root.addView(listHeader, lhParams);

        listContainer = new LinearLayout(context);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(listContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        rebuildList();

        scroll.addView(root, new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        fragmentView = scroll;
        return fragmentView;
    }

    private void rebuildList() {
        if (listContainer == null) {
            return;
        }
        listContainer.removeAllViews();
        Context context = listContainer.getContext();
        if (SharedConfig.lyrxMuteList.isEmpty()) {
            TextView empty = new TextView(context);
            empty.setText("No muted users");
            empty.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            empty.setTextSize(14);
            empty.setPadding(AndroidUtilities.dp(6), AndroidUtilities.dp(10), 0, 0);
            listContainer.addView(empty);
            return;
        }
        java.util.ArrayList<Long> ids = new java.util.ArrayList<>(SharedConfig.lyrxMuteList);
        for (int i = 0; i < ids.size(); i++) {
            final long id = ids.get(i);
            FrameLayout row = new FrameLayout(context);
            GradientDrawable rowBg = new GradientDrawable();
            rowBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            rowBg.setCornerRadius(AndroidUtilities.dp(14));
            row.setBackground(rowBg);

            LinearLayout textCol = new LinearLayout(context);
            textCol.setOrientation(LinearLayout.VERTICAL);

            String name = "User " + id;
            TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(id);
            if (user != null) {
                String un = UserObject.getUserName(user);
                if (un != null && un.trim().length() > 0) {
                    name = un;
                }
            }

            TextView nameView = new TextView(context);
            nameView.setText(name);
            nameView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            nameView.setTextSize(16);
            nameView.setTypeface(AndroidUtilities.bold());

            TextView idView = new TextView(context);
            idView.setText(String.valueOf(id));
            idView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            idView.setTextSize(13);
            idView.setAlpha(0.6f);

            textCol.addView(nameView);
            textCol.addView(idView);
            row.addView(textCol, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 16, 10, 56, 10));

            ImageView removeBtn = new ImageView(context);
            removeBtn.setImageResource(R.drawable.msg_close);
            removeBtn.setColorFilter(0xFFFF3B30);
            removeBtn.setOnClickListener(v -> {
                SharedConfig.lyrxRemoveMute(id);
                if (SharedConfig.lyrxMuteList.isEmpty()) {
                    SharedConfig.lyrxMuteEnabled = false;
                    SharedConfig.lyrxSaveMuteList();
                }
                rebuildList();
            });
            row.addView(removeBtn, LayoutHelper.createFrame(20, 20, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 16, 0));

            LinearLayout.LayoutParams rowParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
            rowParams.topMargin = AndroidUtilities.dp(8);
            row.setMinimumHeight(AndroidUtilities.dp(60));
            listContainer.addView(row, rowParams);
        }
    }
}
