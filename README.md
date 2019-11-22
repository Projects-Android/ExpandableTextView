# ExpandableTextView
A expanable view to show text and pictures.

![image](/screenshot/1.gif)

# How to use++
**1、add in gradle**
```groovy
implementation 'ev.android.expandabletextview:library:0.0.1'
```

**2、use in xml**
```xml
<com.ev.library.ExpandableTextView
        android:layout_width="300dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="30dp"
        android:background="@drawable/shape_bg"
        android:padding="5dp"
        app:text="ExpandableTextView: This is a test sentence which can be very long. Here is test text(ljot sljs slg slg slg lsg sjg ). To make sure that the TextView has many lines."
        app:textSize="14sp"
        app:textColor="@color/selector_text_color"
        app:foldDrawable="@drawable/selector_icon_fold"
        app:unFoldDrawable="@drawable/selector_icon_unfold"
        app:drawableSize="40px"
        app:drawablePadding="10px"
        app:drawablePosition="right"
        app:maxLines="2"/>
```