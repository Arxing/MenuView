package org.arxing.menuview;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({MenuView.ORIENTATION_LEFT, MenuView.ORIENTATION_TOP, MenuView.ORIENTATION_RIGHT, MenuView.ORIENTATION_BOTTOM})
@Retention(RetentionPolicy.SOURCE)
public @interface Orientation {
}
