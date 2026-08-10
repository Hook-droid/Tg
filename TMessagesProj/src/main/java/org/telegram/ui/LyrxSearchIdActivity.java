package org.telegram.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;

public class LyrxSearchIdActivity extends BaseFragment {

    private EditText idInput;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("Open Profile By ID");
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

        FrameLayout infoCard = new FrameLayout(context);
        GradientDrawable infoBg = new GradientDrawable();
        infoBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        infoBg.setCornerRadius(AndroidUtilities.dp(18));
        infoBg.setStroke(AndroidUtilities.dp(1), 0x18FFFFFF);
        infoCard.setBackground(infoBg);

        LinearLayout infoCol = new LinearLayout(context);
        infoCol.setOrientation(LinearLayout.VERTICAL);
        infoCol.setPadding(AndroidUtilities.dp(18), AndroidUtilities.dp(18), AndroidUtilities.dp(18), AndroidUtilities.dp(18));

        TextView tools = new TextView(context);
        tools.setText("LYRXGRAM TOOLS");
        tools.setTextColor(0xFF3390EC);
        tools.setTextSize(13);
        tools.setTypeface(AndroidUtilities.bold());
        infoCol.addView(tools);

        TextView infoTitle = new TextView(context);
        infoTitle.setText("Open LyrxGram Chat By User ID");
        infoTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        infoTitle.setTextSize(20);
        infoTitle.setTypeface(AndroidUtilities.bold());
        LinearLayout.LayoutParams itParams = LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
        itParams.topMargin = AndroidUtilities.dp(8);
        infoCol.addView(infoTitle, itParams);

        TextView infoDesc = new TextView(context);
        infoDesc.setText("Enter A Numeric User ID. Opening Will Work If That User Is Already Available In The Client Cache: Dialogs, Contacts, Or Forwarded Messages.");
        infoDesc.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        infoDesc.setTextSize(14);
        infoDesc.setLineSpacing(AndroidUtilities.dp(3), 1f);
        LinearLayout.LayoutParams idParams = LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
        idParams.topMargin = AndroidUtilities.dp(12);
        infoCol.addView(infoDesc, idParams);

        infoCard.addView(infoCol);
        root.addView(infoCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        FrameLayout inputCard = new FrameLayout(context);
        GradientDrawable inCardBg = new GradientDrawable();
        inCardBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        inCardBg.setCornerRadius(AndroidUtilities.dp(18));
        inCardBg.setStroke(AndroidUtilities.dp(1), 0x18FFFFFF);
        inputCard.setBackground(inCardBg);

        LinearLayout inCol = new LinearLayout(context);
        inCol.setOrientation(LinearLayout.VERTICAL);
        inCol.setPadding(AndroidUtilities.dp(18), AndroidUtilities.dp(18), AndroidUtilities.dp(18), AndroidUtilities.dp(18));

        TextView inTitle = new TextView(context);
        inTitle.setText("LyrxGram User ID");
        inTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        inTitle.setTextSize(17);
        inTitle.setTypeface(AndroidUtilities.bold());
        inCol.addView(inTitle);

        TextView inHint = new TextView(context);
        inHint.setText("Only Numeric IDs Are Supported.");
        inHint.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        inHint.setTextSize(14);
        LinearLayout.LayoutParams ihParams = LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
        ihParams.topMargin = AndroidUtilities.dp(4);
        inCol.addView(inHint, ihParams);

        FrameLayout inputBox = new FrameLayout(context);
        GradientDrawable ibBg = new GradientDrawable();
        ibBg.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
        ibBg.setCornerRadius(AndroidUtilities.dp(16));
        inputBox.setBackground(ibBg);
        idInput = new EditText(context);
        idInput.setHint("Example: 8760170705");
        idInput.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        idInput.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        idInput.setTextSize(16);
        idInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        idInput.setBackgroundColor(Color.TRANSPARENT);
        inputBox.addView(idInput, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT, 16, 0, 16, 0));
        LinearLayout.LayoutParams ibParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 54);
        ibParams.topMargin = AndroidUtilities.dp(16);
        inCol.addView(inputBox, ibParams);

        FrameLayout openBtn = new FrameLayout(context);
        GradientDrawable obBg = new GradientDrawable();
        obBg.setColor(0xFF3390EC);
        obBg.setCornerRadius(AndroidUtilities.dp(16));
        openBtn.setBackground(obBg);
        TextView obText = new TextView(context);
        obText.setText("Open Chat");
        obText.setTextColor(0xFFFFFFFF);
        obText.setTextSize(16);
        obText.setTypeface(AndroidUtilities.bold());
        obText.setGravity(Gravity.CENTER);
        openBtn.addView(obText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        LinearLayout.LayoutParams obParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 52);
        obParams.topMargin = AndroidUtilities.dp(16);
        inCol.addView(openBtn, obParams);

        openBtn.setOnClickListener(v -> openById());

        inputCard.addView(inCol);
        LinearLayout.LayoutParams inCardParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        inCardParams.topMargin = AndroidUtilities.dp(16);
        root.addView(inputCard, inCardParams);

        TextView powered = new TextView(context);
        powered.setText("Powered By LyrxGram");
        powered.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        powered.setTextSize(13);
        powered.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams pParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        pParams.topMargin = AndroidUtilities.dp(20);
        root.addView(powered, pParams);

        scroll.addView(root, new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        fragmentView = scroll;
        return fragmentView;
    }

    private void openById() {
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
        AndroidUtilities.hideKeyboard(idInput);
        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(id);
        if (user != null) {
            Bundle args = new Bundle();
            args.putLong("user_id", id);
            presentFragment(new ProfileActivity(args));
        } else {
            TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(id);
            if (chat != null) {
                Bundle args = new Bundle();
                args.putLong("chat_id", id);
                presentFragment(new ProfileActivity(args));
            } else {
                BulletinFactory.of(this).createErrorBulletin("User not found in cache").show();
            }
        }
    }
}
