// java
package my.edu.utar.grandarchivecompanion.ui.cards;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.text.HtmlCompat;

import android.text.Spanned;
import android.text.Html;

public class ResourceImageGetter implements Html.ImageGetter {
    private final Context context;
    private final int sizePx;

    public ResourceImageGetter(@NonNull Context context, int sizePx) {
        this.context = context.getApplicationContext();
        this.sizePx = sizePx;
    }

    @Override
    public Drawable getDrawable(String source) {
        if (source == null) return null;
        int resId = context.getResources().getIdentifier(source, "drawable", context.getPackageName());
        if (resId == 0) return null;
        Drawable d = AppCompatResources.getDrawable(context, resId);
        if (d == null) return null;
        int s = sizePx > 0 ? sizePx : (d.getIntrinsicHeight() > 0 ? d.getIntrinsicHeight() : 48);
        d.setBounds(0, 0, s, s);
        return d;
    }

    public Spanned fromHtml(String html) {
        return HtmlCompat.fromHtml(html == null ? "" : html, HtmlCompat.FROM_HTML_MODE_LEGACY, this, null);
    }
}
