package mws.apps.greektexts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import mws.apps.greektexts.R;




public class GreekTextView extends TextView {

    public GreekTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public GreekTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GreekTextView(Context context) {
        super(context);
        init();
    }

    public void init() {

        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "IFAOGrecMWS41.ttf");
        setTypeface(tf ,1);

    }
    }
