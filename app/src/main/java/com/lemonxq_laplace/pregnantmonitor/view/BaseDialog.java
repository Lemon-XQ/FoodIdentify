package com.lemonxq_laplace.pregnantmonitor.view;

import android.app.Dialog;
import android.content.Context;

/**
 * @author: Lemon-XQ
 * @date: 2018/4/7
 */

public class BaseDialog extends Dialog {
    private int res;

    public BaseDialog(Context context, int theme, int res) {
        super(context, theme);

        setContentView(res);
        this.res = res;
        setCanceledOnTouchOutside(false);
    }
}
