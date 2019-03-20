package org.arxing.menuview;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({MenuView.MODE_SLIDE, MenuView.MODE_PUSH})
@Retention(RetentionPolicy.SOURCE)
public @interface ActiveMode {
}
