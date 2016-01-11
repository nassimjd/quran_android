package com.quran.labs.androidquran.widgets;

import com.quran.labs.androidquran.R;
import com.quran.labs.androidquran.data.Constants;
import com.quran.labs.androidquran.ui.helpers.QuranDisplayHelper;
import com.quran.labs.androidquran.ui.util.PageController;
import com.quran.labs.androidquran.util.QuranSettings;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class QuranPageLayout extends FrameLayout
    implements ObservableScrollView.OnScrollListener {
  private static PaintDrawable sLeftGradient, sRightGradient = null;
  private static boolean sAreGradientsLandscape;

  protected Context mContext;
  protected PageController mPageController;
  protected int mPageNumber;

  private boolean mIsNightMode;
  private ObservableScrollView mScrollView;
  private ImageView mLeftBorder;
  private ImageView mRightBorder;
  private View mErrorLayout;
  private TextView mErrorText;
  private View mInnerView;
  private int mViewPaddingSmall;
  private int mViewPaddingLarge;

  public QuranPageLayout(Context context) {
    super(context);
    mContext = context;
    ViewCompat.setLayoutDirection(this, ViewCompat.LAYOUT_DIRECTION_LTR);
    Resources resources = context.getResources();
    final boolean isLandscape =
        resources.getConfiguration().orientation ==
        Configuration.ORIENTATION_LANDSCAPE;
    mInnerView = generateContentView(context);
    mViewPaddingSmall = resources.getDimensionPixelSize(R.dimen.page_margin_small);
    mViewPaddingLarge = resources.getDimensionPixelSize(R.dimen.page_margin_large);

    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    lp.gravity = Gravity.CENTER;
    if (isLandscape && shouldWrapWithScrollView()) {
      mScrollView = new ObservableScrollView(context);
      mScrollView.setFillViewport(true);
      addView(mScrollView, lp);
      mScrollView.addView(mInnerView, LayoutParams.MATCH_PARENT,
          LayoutParams.WRAP_CONTENT);
      mScrollView.setOnScrollListener(this);
    } else {
      addView(mInnerView, lp);
    }

    if (sAreGradientsLandscape != isLandscape) {
      sLeftGradient = null;
      sRightGradient = null;
      sAreGradientsLandscape = isLandscape;
    }
  }

  protected abstract View generateContentView(Context context);
  protected abstract void setContentNightMode(
      boolean nightMode, int textBrightness);

  protected boolean shouldWrapWithScrollView() {
    return true;
  }

  public void setPageController(PageController controller, int pageNumber) {
    mPageNumber = pageNumber;
    mPageController = controller;
  }

  public void updateView(boolean nightMode, boolean useNewBackground) {
    if (sRightGradient == null) {
      final WindowManager mgr =
          (WindowManager) mContext.getApplicationContext()
              .getSystemService(Context.WINDOW_SERVICE);
      Display display = mgr.getDefaultDisplay();
      int width = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ?
          QuranDisplayHelper.getWidthKitKat(display) : display.getWidth();
      sLeftGradient = QuranDisplayHelper.getPaintDrawable(width, 0);
      sRightGradient = QuranDisplayHelper.getPaintDrawable(0, width);
    }

    mIsNightMode = nightMode;
    final int lineImageId = nightMode ?
        R.drawable.light_line : R.drawable.dark_line;
    final int leftBorderImageId = nightMode ?
        R.drawable.night_left_border : R.drawable.border_left;
    final int rightBorderImageId = nightMode ?
        R.drawable.night_right_border : R.drawable.border_right;
    final int nightModeTextBrightness = nightMode ?
        QuranSettings.getInstance(mContext).getNightModeTextBrightness() :
        Constants.DEFAULT_NIGHT_MODE_TEXT_BRIGHTNESS;

    if (mLeftBorder == null) {
      mLeftBorder = new ImageView(mContext);
      final FrameLayout.LayoutParams params =
          new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
          ViewGroup.LayoutParams.MATCH_PARENT);
      params.gravity = GravityCompat.START;
      addView(mLeftBorder, params);
    }

    if (mPageNumber % 2 == 0) {
      mLeftBorder.setBackgroundResource(leftBorderImageId);
      if (mRightBorder != null) {
        mRightBorder.setVisibility(GONE);
      }
    } else {
      if (mRightBorder == null) {
        mRightBorder = new ImageView(mContext);
        final FrameLayout.LayoutParams params =
            new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = GravityCompat.END;
        addView(mRightBorder, params);
      }
      mRightBorder.setVisibility(VISIBLE);
      mRightBorder.setBackgroundResource(rightBorderImageId);
      mLeftBorder.setBackgroundResource(lineImageId);
    }
    setContentNightMode(nightMode, nightModeTextBrightness);

    if (nightMode) {
      setBackgroundColor(Color.BLACK);
    } else if (useNewBackground) {
      setBackgroundDrawable((mPageNumber % 2 == 0 ?
          sLeftGradient : sRightGradient));
    } else {
      setBackgroundColor(mContext.getResources().getColor(
          R.color.page_background));
    }

    if (mErrorText != null) {
      updateErrorTextColor();
    }

    // set a margin on the page itself so that it can never overlap the
    // left or right borders.
    final View innerView = mScrollView == null ? mInnerView : mScrollView;
    final LayoutParams params =
        (FrameLayout.LayoutParams) innerView.getLayoutParams();

    if (mPageNumber % 2 == 0) {
      params.leftMargin = mViewPaddingLarge;
      params.rightMargin = mViewPaddingSmall;
    } else {
      params.leftMargin = mViewPaddingSmall;
      params.rightMargin = mViewPaddingLarge;
    }

    // this calls requestLayout
    innerView.setLayoutParams(params);
  }

  public void showError(@StringRes int errorRes) {
    if (mErrorLayout == null) {
      inflateErrorLayout();
    }
    mErrorLayout.setVisibility(VISIBLE);
    mErrorText.setText(errorRes);
  }

  private void inflateErrorLayout() {
    final LayoutInflater inflater = LayoutInflater.from(mContext);
    mErrorLayout = inflater.inflate(R.layout.page_load_error, this, false);
    LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    lp.gravity = Gravity.CENTER;
    addView(mErrorLayout, lp);
    mErrorText = (TextView) mErrorLayout.findViewById(R.id.reason_text);
    final Button button =
        (Button) mErrorLayout.findViewById(R.id.retry_button);
    updateErrorTextColor();
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mErrorLayout.setVisibility(GONE);
        if (mPageController != null) {
          mPageController.handleRetryClicked();
        }
      }
    });
  }

  private void updateErrorTextColor() {
    mErrorText.setTextColor(mIsNightMode ? Color.WHITE : Color.BLACK);
  }

  public int getCurrentScrollY() {
    return mScrollView == null ? 0 : mScrollView.getScrollY();
  }

  public boolean canScroll() {
    return mScrollView != null;
  }

  public void smoothScrollLayoutTo(int y) {
    mScrollView.smoothScrollTo(mScrollView.getScrollX(), y);
  }

  @Override
  public void onScrollChanged(ObservableScrollView scrollView,
      int x, int y, int oldx, int oldy) {
    if (mPageController != null) {
      mPageController.onScrollChanged(x, y, oldx, oldy);
    }
  }
}
