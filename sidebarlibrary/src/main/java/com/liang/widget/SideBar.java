package com.liang.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SideBar extends View {

    public static final String TAG = "SideBar";


    // 默认26个字母
    private String[] arrayTag = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"
    };
    //选中
    private int choose = -1;

    private Paint mPaint;

    private int mTextColor;
    private int mTextSelectedColor;
    private int mTextBackground;
    private int mTextSelectedBackground;
    private int mTextSize;
    private int mInterval;

    private List<Rect> itemRect = new ArrayList<>();
    private int mBackgroundWidth;

    private OnTouchingChangedListener mOnTouchingChangedListener;
    private TextDialog mTextDialog;

    public interface OnTouchingChangedListener {
        void onTouchingChanged(String s);
    }

    public interface TextDialog {
        void dismiss();

        void show(String s, int y);
    }

    public SideBar(Context context) {
        this(context, null);
    }

    public SideBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SideBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SideBar,
                defStyleAttr, 0);
        mTextColor = typedArray.getColor(R.styleable.SideBar_sideBarTextColor, Color.GRAY);
        mTextSelectedColor = typedArray.getColor(R.styleable.SideBar_sideBarTextSelectedColor, Color.BLACK);
        mTextBackground = typedArray.getColor(R.styleable.SideBar_sideBarTextBackground, Color.TRANSPARENT);
        mTextSelectedBackground = typedArray.getColor(R.styleable.SideBar_sideBarTextSelectedBackground, Color.TRANSPARENT);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.SideBar_sideBarTextSize, sp2px(getContext(), 14));
        typedArray.recycle();
        mInterval = mTextSize / 2;
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(mTextSize);
    }

    public void setTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
        invalidate();
    }

    public void setTextSelectedColor(int textSelectedColor) {
        this.mTextSelectedColor = textSelectedColor;
        invalidate();
    }

    public void setTextBackground(int textBackground) {
        this.mTextBackground = textBackground;
        invalidate();
    }

    public void setTextSelectedBackground(int textSelectedBackground) {
        this.mTextSelectedBackground = textSelectedBackground;
        invalidate();
    }

    public void setTextSize(int textSize) {
        this.mTextSize = textSize;
        mPaint.setTextSize(mTextSize);
        invalidate();
    }

    public void setOnTouchingChangedListener(OnTouchingChangedListener mOnTouchingChangedListener) {
        this.mOnTouchingChangedListener = mOnTouchingChangedListener;
    }

    public void setTextDialog(TextDialog mTextDialog) {
        this.mTextDialog = mTextDialog;
    }

    public void setArrayTag(int arrayResId) {
        setArrayTag(getContext().getResources().getStringArray(arrayResId));
    }

    public void setArrayTag(String[] arrayTag) {
        if (arrayTag == null) {
            throw new NullPointerException("arrayTag is null");
        }
        this.arrayTag = arrayTag;
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;

        int size = onMeasureChild();

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = mBackgroundWidth + getPaddingLeft() + getPaddingRight();
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
            onMeasureHeight(width, height);
        } else {
            height = mBackgroundWidth * size + getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(width, height);
    }


    private void onMeasureHeight(int width, int height) {
        itemRect.clear();
        final int length = arrayTag.length;
        int itemWidth = width - getPaddingLeft() - getPaddingRight();
        int itemHeight = (height - getPaddingTop() - getPaddingBottom()) / length;
        int itemTop = getPaddingTop();
        for (int i = 0; i < length; i++) {
            Rect bound = new Rect();
            bound.left = getPaddingLeft();
            bound.top = itemTop;
            bound.right = bound.left + itemWidth;
            bound.bottom = bound.top + itemHeight;
            itemRect.add(bound);
            itemTop += itemHeight;
        }
    }

    private int onMeasureChild() {
        itemRect.clear();
        List<Integer> itemWidths = new ArrayList<>();
        List<Integer> itemHeights = new ArrayList<>();
        for (String a : arrayTag) {
            Rect bound = new Rect();
            mPaint.getTextBounds(a, 0, a.length(), bound);
            itemRect.add(bound);
            int itemWidth = bound.right - bound.left;
            itemWidths.add(itemWidth);
            int itemHeight = bound.bottom - bound.top;
            itemHeights.add(itemHeight);
        }
        mBackgroundWidth = Math.max(Collections.max(itemWidths), Collections.max(itemHeights)) + mInterval;
        refreshBounds();
        return itemRect.size();
    }

    private void refreshBounds() {
        int itemTop = getPaddingTop();
        for (Rect bound : itemRect) {
            bound.left = getPaddingLeft();
            bound.top = itemTop;
            bound.right = bound.left + mBackgroundWidth;
            bound.bottom = bound.top + mBackgroundWidth;
            itemTop += mBackgroundWidth;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        int itemY = getPaddingTop();

        for (int i = 0; i < arrayTag.length; i++) {
            Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
            // 转载请注明出处：http://blog.csdn.net/hursing
            Rect bound = itemRect.get(i);
            int baseline = (bound.bottom + bound.top - fontMetrics.bottom - fontMetrics.top) / 2;
            mPaint.setAntiAlias(true);
            mPaint.setTextAlign(Paint.Align.CENTER);
            if (choose == i) {
                mPaint.setColor(mTextSelectedBackground);
                canvas.drawCircle(width / 2, bound.top + (bound.bottom - bound.top) / 2, mBackgroundWidth / 2, mPaint);
                mPaint.setColor(mTextSelectedColor);
                mPaint.setFakeBoldText(true);//设置是否为粗体文字
            } else {
                mPaint.setColor(mTextBackground);
                canvas.drawCircle(width / 2, bound.top + (bound.bottom - bound.top) / 2, mBackgroundWidth / 2, mPaint);
                mPaint.setColor(mTextColor);
                mPaint.setFakeBoldText(false);
            }

            Log.e(TAG, "itemY: " + itemY);
            canvas.drawText(arrayTag[i], width / 2, baseline, mPaint);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();//点击y坐标
        final int oldChoose = choose;
        int c = -1;
        if (getPaddingTop() > 0 || getPaddingBottom() > 0) {
            for (int i = 0; i < itemRect.size(); i++) {
                Rect bound = itemRect.get(i);
                if (y >= bound.top && y <= bound.bottom) {
                    c = i;
                }
            }
        } else {
            c = (int) (y / getHeight() * arrayTag.length);//点击y坐标所占高度的比例*b数组的长度就等于点击b中的个数
        }

        switch (action) {
            case MotionEvent.ACTION_UP:
                setPressed(false);
                choose = -1;
                invalidate();
                if (mTextDialog != null) {
                    mTextDialog.dismiss();
                }
                break;

            default:
                setPressed(true);
                if (oldChoose != c) {
                    if (c >= 0 && c < arrayTag.length) {
                        if (mOnTouchingChangedListener != null) {
                            mOnTouchingChangedListener.onTouchingChanged(arrayTag[c]);
                        }
                        if (mTextDialog != null) {
                            mTextDialog.show(arrayTag[c], itemRect.get(c).centerY());
                        }
                        choose = c;
                        invalidate();
                    }
                }
                break;
        }
        return true;

    }

    private int sp2px(Context context, float sp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, sp,
                context.getResources().getDisplayMetrics());
    }
}
