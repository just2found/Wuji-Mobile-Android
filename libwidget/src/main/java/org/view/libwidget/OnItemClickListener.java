package org.view.libwidget;

import android.view.View;

import org.jetbrains.annotations.NotNull;

public interface OnItemClickListener<T> {
    void OnItemClick(@NotNull T data, int position,@NotNull View view);
}
