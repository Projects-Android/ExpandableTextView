package com.ev.library;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;



public class ExpandableTextView extends View implements View.OnClickListener {
    private static final int DEFAULT_TEXT_SIZE_PX = 20;
    private static final int DRAWABLE_POSITION_LEFT = 1;
    private static final int DRAWABLE_POSITION_RIGHT = 2;

    private TextPaint mTextPaint;
    private String mCurrentText;
    private ColorStateList mTextColor;
    private @ColorInt
    int mCurTextColor;
    private int mTextSize;
    private int mMaxLines;
    private Drawable mUnFoldDrawable;
    private Drawable mFoldDrawable;
    private int mDrawablePadding = 0;
    private int mDrawableWitdh = 0;
    private int mDrawableHeight = 0;
    private int mDrawablePosition;

    private StaticLayout mLayout;
    private boolean expandable = false;
    private boolean expanded = false;

    public ExpandableTextView(Context context) {
        super(context);

        init(context, null);
    }

    public ExpandableTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public ExpandableTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ExpandableTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context, attrs);
    }

    public void init(Context context, AttributeSet attributeSet) {
        if (null != attributeSet) {
            TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.ExpandableTextView);
            this.mCurrentText = a.getString(R.styleable.ExpandableTextView_text);
            this.mTextColor = a.getColorStateList(R.styleable.ExpandableTextView_textColor);
            updateTextColors();
            this.mMaxLines = a.getInteger(R.styleable.ExpandableTextView_maxLines, 1);
            this.mUnFoldDrawable = a.getDrawable(R.styleable.ExpandableTextView_unFoldDrawable);
            this.mFoldDrawable = a.getDrawable(R.styleable.ExpandableTextView_foldDrawable);
            this.mDrawablePadding = a.getDimensionPixelSize(R.styleable.ExpandableTextView_drawablePadding, 0);
            this.mDrawableWitdh = a.getDimensionPixelSize(R.styleable.ExpandableTextView_drawableSize, 0);
            this.mDrawableHeight = mDrawableWitdh;
            if (mDrawableWitdh <= 0) {
                int width = 0;
                int height = 0;
                if (null != mUnFoldDrawable) {
                    width = mUnFoldDrawable.getIntrinsicWidth();
                    height = mUnFoldDrawable.getIntrinsicHeight();
                }

                if (null != mFoldDrawable) {
                    width = Math.max(width, mFoldDrawable.getIntrinsicWidth());
                    height = Math.max(height, mFoldDrawable.getIntrinsicHeight());
                }

                mDrawableWitdh = width;
                mDrawableHeight = height;
            }
            setCompoundDrawablesWithIntrinsicBounds(mUnFoldDrawable, mFoldDrawable);

            this.mTextSize = a.getDimensionPixelSize(R.styleable.ExpandableTextView_textSize, DEFAULT_TEXT_SIZE_PX);
            this.mDrawablePosition = a.getInt(R.styleable.ExpandableTextView_drawablePosition, DRAWABLE_POSITION_RIGHT);
        }

        this.mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        this.mTextPaint.setColor(this.mCurTextColor);
        this.mTextPaint.setTextSize(this.mTextSize);
    }

    public void setText(String text) {
        reset();

        this.mCurrentText = text;

        requestLayout();
    }

    public void setText(String text, boolean expanded) {
        this.expanded = expanded;

        setText(text);
    }

    /**
     * reset status
     */
    public void reset() {
        expandable = false;
        expanded = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = 0;

        int lineWidth = width - getPaddingLeft() - getPaddingRight();
        int ellipseWidth = width - mDrawablePadding - mDrawableWitdh - getPaddingLeft() - getPaddingRight();
        expandable = isExpandable(lineWidth);
        if (expandable) {
            lineWidth = ellipseWidth;
        }
        if (!expanded && expandable) {
            if (!TextUtils.isEmpty(mCurrentText)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mLayout = StaticLayout.Builder.obtain(mCurrentText, 0, mCurrentText.length(), mTextPaint, lineWidth)
                            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                            .setEllipsize(TextUtils.TruncateAt.END)
                            .setMaxLines(mMaxLines)
                            .setEllipsizedWidth(ellipseWidth)
                            .setIncludePad(false)
                            .build();
                } else {
                    StaticLayout layout = new StaticLayout(
                            mCurrentText,
                            mTextPaint,
                            lineWidth,
                            Layout.Alignment.ALIGN_NORMAL,
                            1.0f,
                            0f,
                            false);

                    StringBuilder sb = new StringBuilder();
                    int start = layout.getLineStart(mMaxLines - 1);
                    sb.append(mCurrentText.subSequence(0, start));
                    CharSequence lastLine = TextUtils.ellipsize(
                            mCurrentText.subSequence(start, mCurrentText.length()),
                            mTextPaint,
                            lineWidth,
                            TextUtils.TruncateAt.END,
                            false,
                            null);
                    sb.append(lastLine);
                    String newText = sb.toString();

                    mLayout = new StaticLayout(
                            newText,
                            0,
                            newText.length(),
                            mTextPaint,
                            lineWidth,
                            Layout.Alignment.ALIGN_NORMAL,
                            1.0f,
                            0f,
                            false,
                            TextUtils.TruncateAt.END,
                            ellipseWidth);
                }

                height += mLayout.getHeight();
            } else {
                mLayout = null;
            }
        } else { // expand all lines
            if (!TextUtils.isEmpty(mCurrentText)) {
                mLayout = new StaticLayout(
                        mCurrentText,
                        mTextPaint,
                        lineWidth,
                        Layout.Alignment.ALIGN_NORMAL,
                        1.0f,
                        0f,
                        false);

                height += mLayout.getHeight();
            } else {
                mLayout = null;
            }
        }

        setMeasuredDimension(width, height + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (null == mLayout) {
            setVisibility(GONE);
            return;
        }

        int drawableX = 0;
        int textX = 0;
        switch (mDrawablePosition) {
            case DRAWABLE_POSITION_LEFT:
                drawableX = getPaddingLeft();
                textX = getPaddingLeft() + mDrawablePadding + mDrawableWitdh;
                break;
            case DRAWABLE_POSITION_RIGHT:
                drawableX = canvas.getWidth() - mDrawableWitdh - getPaddingRight();
                textX = getPaddingLeft();
                break;
            default:
                break;
        }

        if (expandable) {
            canvas.save();
            canvas.translate(drawableX, getPaddingTop());
            Drawable drawable = null;
            if (expanded && null != mFoldDrawable) {
                drawable = mFoldDrawable.getCurrent();
            } else if (null != mUnFoldDrawable) {
                drawable = mUnFoldDrawable.getCurrent();
            }
            if (null != drawable) {
                drawable.setBounds(0, 0, mDrawableWitdh, mDrawableHeight);
                drawable.draw(canvas);
            }
            canvas.restore();

            setOnClickListener(this);
        } else {
            setOnClickListener(null);
        }

        if (null != mLayout) {
            canvas.save();
            canvas.translate(textX, getPaddingTop());
            mLayout.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (updateTextColors()) {
            invalidate();
        }
        updateDrawable();
    }

    /**
     * update text color
     *
     * @return should redraw if true
     */
    private boolean updateTextColors() {
        if (null != mTextColor && mTextColor.isStateful()) {
            final int[] drawableState = getDrawableState();
            int color = mTextColor.getColorForState(drawableState, 0);
            if (color != mCurTextColor) {
                mCurTextColor = color;
                if (null != mTextPaint) {
                    mTextPaint.setColor(this.mCurTextColor);
                }

                return true;
            }
        }

        return false;
    }

    public void setCompoundDrawablesWithIntrinsicBounds(@Nullable Drawable unfold, @Nullable Drawable fold) {

        if (unfold != null) {
            unfold.setBounds(0, 0, unfold.getIntrinsicWidth(), unfold.getIntrinsicHeight());
        }
        if (fold != null) {
            fold.setBounds(0, 0, fold.getIntrinsicWidth(), fold.getIntrinsicHeight());
        }
        setCompoundDrawables(unfold, fold);
    }

    public void setCompoundDrawables(@Nullable Drawable unfold, @Nullable Drawable fold) {
        int[] state = getDrawableState();
        if (null != unfold) {
            unfold.setState(state);
            unfold.setCallback(this);
        }

        if (null != fold) {
            fold.setState(state);
            fold.setCallback(this);
        }
    }

    private void updateDrawable() {
        if (null != mUnFoldDrawable && mUnFoldDrawable.isStateful() && mUnFoldDrawable.setState(getDrawableState())) {
            invalidateDrawable(mUnFoldDrawable.getCurrent());
        }

        if (null != mFoldDrawable && mFoldDrawable.isStateful() && mFoldDrawable.setState(getDrawableState())) {
            invalidateDrawable(mFoldDrawable.getCurrent());
        }
    }

    /**
     * expandable or not
     *
     * @param width
     * @return
     */
    public boolean isExpandable(int width) {
        if (!TextUtils.isEmpty(mCurrentText)) {
            StaticLayout staticLayout = new StaticLayout(
                    mCurrentText,
                    mTextPaint,
                    width,
                    Layout.Alignment.ALIGN_NORMAL,
                    1.0f,
                    0f,
                    false);

            return staticLayout.getLineCount() > mMaxLines;
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        expanded = !expanded;
        requestLayout();
    }
}