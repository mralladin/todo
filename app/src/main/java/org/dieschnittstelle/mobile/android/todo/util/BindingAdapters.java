package org.dieschnittstelle.mobile.android.todo.util;

import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BindingAdapters {
    //Für activity overview structured listitem view
    @BindingAdapter("formattedDate")
    public static void setFormattedDate(TextView textView, Long timestamp) {
        if (timestamp != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            String formattedDate = formatter.format(new Date(timestamp));
            textView.setText(formattedDate);
        } else {
            textView.setText(""); // Leeren Text setzen, falls timestamp null ist
        }
    }

    @BindingAdapter("formattedText")
    public static void setFormattedText(TextView textView, String text) {
        if (text != null) {
            // Wenn der Text länger als 16 Zeichen ist, kürze ihn mit "..."
            if (text.length() > 21) {
                text = text.substring(0, 21) + "...";
            }
            textView.setText(text);
        }
    }
}
