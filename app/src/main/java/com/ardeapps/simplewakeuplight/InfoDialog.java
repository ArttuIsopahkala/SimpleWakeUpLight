package com.ardeapps.simplewakeuplight;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class InfoDialog {

    public void showDialog(Activity activity){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_info);

        final Context context =  AppRes.getContext();

        TextView rateText = dialog.findViewById(R.id.rateText);
        rateText.setText(Html.fromHtml("<u>" + context.getString(R.string.info_link_rate) + "</u>"));
        rateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(context.getString(R.string.app_google_play_link)));
                context.startActivity(i);
            }
        });

        TextView moreText = dialog.findViewById(R.id.moreText);
        moreText.setText(Html.fromHtml("<u>" + context.getString(R.string.info_link_more) + "</u>"));
        moreText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(context.getString(R.string.app_developer_link)));
                context.startActivity(i);
            }
        });

        TextView versionText = dialog.findViewById(R.id.versionText);
        versionText.setText(context.getString(R.string.info_version, BuildConfig.VERSION_NAME));

        Button closeButton = dialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }
}
