package net.sdvn.nascommon.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * 适合平均分布
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    @NonNull
    private Paint mDividerPaint = new Paint();
    private DisplayMetrics mDisplayMetrics;
    private int mSpace;
    private int mEdgeSpace;
    private Context mContext;

    public SpaceItemDecoration(Context context) {
        mContext = context;
        mDisplayMetrics = context.getResources().getDisplayMetrics();
    }

    @NonNull
    public SpaceItemDecoration setSpace(int space) {
        mSpace = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, space, mDisplayMetrics) + 0.5f);
        return this;
    }

    @NonNull
    public SpaceItemDecoration setEdgeSpace(int edgeSpace) {
        mEdgeSpace = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, edgeSpace, mDisplayMetrics) + 0.5f);
        return this;
    }

    @NonNull
    public SpaceItemDecoration setSpaceColor(int spaceColor) {
        mDividerPaint.setColor(spaceColor);
        return this;
    }

    @NonNull
    public SpaceItemDecoration setSpaceColorResId(@ColorRes int spaceColor) {
        int color = mContext.getResources().getColor(spaceColor);
        mDividerPaint.setColor(color);
        return this;
    }


    @Override
    public void getItemOffsets(@NonNull Rect outRect, View view, @NonNull RecyclerView parent, RecyclerView.State state) {
        final RecyclerView.LayoutManager manager = parent.getLayoutManager();
        final int childPosition = parent.getChildAdapterPosition(view);
        final int itemCount = parent.getAdapter().getItemCount();
        if (manager != null) {
            if (manager instanceof GridLayoutManager) {
                setGrid((GridLayoutManager) manager, ((GridLayoutManager) manager).getOrientation(), ((GridLayoutManager) manager).getSpanCount(), outRect, childPosition, itemCount);
            } else if (manager instanceof LinearLayoutManager) {
                setLinear(((LinearLayoutManager) manager).getOrientation(), outRect, childPosition, itemCount);
            }
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, RecyclerView.State state) {
        final RecyclerView.LayoutManager manager = parent.getLayoutManager();
        if (manager != null) {
            if (manager instanceof GridLayoutManager) {

            } else if (manager instanceof LinearLayoutManager) {
                drawLinear(((LinearLayoutManager) manager).getOrientation(), c, parent);
            }
        }
    }

    private void setLinear(int orientation, @NonNull Rect outRect, int childPosition, int itemCount) {
        if (orientation == LinearLayoutManager.VERTICAL) {
            if (childPosition == 0) {
                outRect.set(0, mEdgeSpace, 0, mSpace);
            } else if (childPosition == itemCount - 1) {
                outRect.set(0, 0, 0, mEdgeSpace);
            } else {
                outRect.set(0, 0, 0, mSpace);
            }
        } else if (orientation == LinearLayoutManager.HORIZONTAL) {
            if (childPosition == 0) {
                outRect.set(mEdgeSpace, 0, mSpace, 0);
            } else if (childPosition == itemCount - 1) {
                outRect.set(0, 0, mEdgeSpace, 0);
            } else {
                outRect.set(0, 0, mSpace, 0);
            }
        }
    }

    /**
     * ---------  if(childPosition==0 )
     * |a|b|c|d|
     * ---------
     * |adcd|ab|
     * ---------
     * |aaaaaaa|
     * ---------
     */
    private void setGrid(GridLayoutManager manager, int orientation, int spanCount, @NonNull Rect outRect, int childPosition, int itemCount) {
        float totalSpace = mSpace * (spanCount - 1) + mEdgeSpace * 2;
        float eachSpace = totalSpace / spanCount;
        int column = childPosition % spanCount;
        int row = childPosition / spanCount;

        float left;
        float right;
        float top;
        float bottom;
        final boolean isAddEndSpace = itemCount % spanCount != 0 && itemCount / spanCount == row ||
                itemCount % spanCount == 0 && itemCount / spanCount == row + 1;
        if (orientation == GridLayoutManager.VERTICAL) {
            top = 0;
            bottom = mSpace;

            if (childPosition < spanCount) {
                top = mEdgeSpace;
            }
            if (isAddEndSpace) {
                bottom = mEdgeSpace;
            }

            if (spanCount == 1) {
                left = mEdgeSpace;
                right = left;
            } else {
                left = column * (eachSpace - mEdgeSpace - mEdgeSpace) / (spanCount - 1) + mEdgeSpace;
                right = eachSpace - left;
            }
        } else {
            left = 0;
            right = mSpace;

            if (childPosition < spanCount) {
                left = mEdgeSpace;
            }
            if (isAddEndSpace) {
                right = mEdgeSpace;
            }

            if (spanCount == 1) {
                top = mEdgeSpace;
                bottom = top;
            } else {
                top = column * (eachSpace - mEdgeSpace - mEdgeSpace) / (spanCount - 1) + mEdgeSpace;
                bottom = eachSpace - top;
            }
        }
        outRect.set((int) left, (int) top, (int) right, (int) bottom);
    }


    private void drawLinear(int orientation, @NonNull Canvas c, @NonNull RecyclerView parent) {
        if (orientation == LinearLayoutManager.VERTICAL) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin +
                        Math.round(ViewCompat.getTranslationY(child));
                final int bottom = top + mSpace;
                c.drawRect(left, top, right, bottom, mDividerPaint);
            }

        } else if (orientation == LinearLayoutManager.HORIZONTAL) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();
            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getRight() + params.rightMargin +
                        Math.round(ViewCompat.getTranslationX(child));
                final int right = left + mSpace;
                c.drawRect(left, top, right, bottom, mDividerPaint);
            }
        }
    }
}