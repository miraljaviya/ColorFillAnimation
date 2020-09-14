package com.example.myapplication

import android.animation.ValueAnimator
import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.Choreographer
import android.view.animation.DecelerateInterpolator
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

class WaveDrawable(var context: Context, var drawable: Drawable,var colorId:Int): Drawable(),
    Animatable, ValueAnimator.AnimatorUpdateListener {

    companion object{
        private val sXfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        private const val UNDEFINED_VALUE = Int.MIN_VALUE
        private val WAVE_HEIGHT_FACTOR = 0.2f
        private val WAVE_SPEED_FACTOR = 0.02f
    }

    private var mAnimator: ValueAnimator? = null
    private var mIndeterminate: Boolean = false
    private var mRunning: Boolean = false
    private var mCurFilter: ColorFilter? = null
    private var mWaveOffset: Int = 0
    private var mWaveLevel: Int = 0
    private var mWaveStep: Int = UNDEFINED_VALUE
    private  var mWaveHeight: Int = UNDEFINED_VALUE
    private var mWaveLength: Int = UNDEFINED_VALUE
    private var mHeight: Int? = 0
    private var mWidth:Int? = 0
    private var mMatrix: Matrix = Matrix()
    private lateinit var mDrawable : Drawable
    private var mProgress = 0.3f
    private var mPaint: Paint? = null
    private var mMask: Bitmap? = null

    private val mFrameCallback = object: Choreographer.FrameCallback {
        override fun doFrame(l:Long) {
            invalidateSelf()
            if (isRunning) {
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
    }

    init {
        init(drawable)
    }

    private fun init(drawable: Drawable) {
        mDrawable = drawable.constantState!!.newDrawable().mutate()
       // if (selectedColor.equals("pink")) {
            val filter: ColorFilter = PorterDuffColorFilter(
                context.resources.getColor(colorId),
                PorterDuff.Mode.SRC_IN
            )
            mDrawable.colorFilter = filter
      //  }
       /* if (selectedColor.equals("yellow")) {
            val filter: ColorFilter = PorterDuffColorFilter(
                context.resources.getColor(R.color.color_yellow),
                PorterDuff.Mode.SRC_IN
            )
            mDrawable.colorFilter = filter
        }
        if (selectedColor.equals("orange")) {
            val filter: ColorFilter = PorterDuffColorFilter(
                context.resources.getColor(R.color.color_orange),
                PorterDuff.Mode.SRC_IN
            )
            mDrawable.colorFilter = filter
        }
        if (selectedColor.equals("purple")) {
            val filter: ColorFilter = PorterDuffColorFilter(
                context.resources.getColor(R.color.color_purple),
                PorterDuff.Mode.SRC_IN
            )
            mDrawable.colorFilter = filter
        }
        if (selectedColor.equals("red")) {
            val filter: ColorFilter = PorterDuffColorFilter(
                context.resources.getColor(R.color.color_red),
                PorterDuff.Mode.SRC_IN
            )
            mDrawable.colorFilter = filter
        }
        if (selectedColor.equals("green")) {
            val filter: ColorFilter = PorterDuffColorFilter(
                context.resources.getColor(R.color.color_green),
                PorterDuff.Mode.SRC_IN
            )
            mDrawable.colorFilter = filter
        }
        if (selectedColor.equals("blue")) {
            val filter: ColorFilter = PorterDuffColorFilter(
                context.resources.getColor(R.color.color_blue),
                PorterDuff.Mode.SRC_IN
            )
            mDrawable.colorFilter = filter
        }
        if (selectedColor.equals("black")) {
            val filter: ColorFilter = PorterDuffColorFilter(
                context.resources.getColor(R.color.color_black),
                PorterDuff.Mode.SRC_IN
            )
            mDrawable.colorFilter = filter
        }
        if (selectedColor.equals("brown")) {
            val filter: ColorFilter = PorterDuffColorFilter(
                context.resources.getColor(R.color.color_brown),
                PorterDuff.Mode.SRC_IN
            )
            mDrawable.colorFilter = filter
        }
        if (selectedColor.equals("gray")) {
            val filter: ColorFilter = PorterDuffColorFilter(
                context.resources.getColor(R.color.color_gray),
                PorterDuff.Mode.SRC_IN
            )
            mDrawable.colorFilter = filter
        }
*/
        mMatrix.reset()
        mPaint = Paint()
        mPaint?.xfermode = sXfermode

        mWidth = mDrawable.intrinsicWidth
        mHeight = mDrawable.intrinsicHeight

        if (mWidth!! > 0 && mHeight!! > 0) {
            mWaveLength = mWidth!!
            mWaveHeight = max(8.0f, (mHeight!! * WAVE_HEIGHT_FACTOR)).roundToInt()
            mWaveStep = max(1.0f, (mWidth!! * WAVE_SPEED_FACTOR)).roundToInt()
            updateMask(mWidth!!, mWaveLength, mWaveHeight)
        }

        setProgress(0f)
        start()
    }
    fun setWaveSpeed(step: Int) {
        mWaveStep = Math.min(step, mWidth!! / 2)
    }

    fun setWaveAmplitude( amplitude: Int) {
        var amplitude = amplitude
        amplitude = Math.max(1, Math.min(amplitude, mHeight!! / 2))
        val height = amplitude * 2
        if (mWaveHeight != height) {
            mWaveHeight = height
            updateMask(mWidth!!, mWaveLength, mWaveHeight)
            invalidateSelf()
        }
    }

    fun setWaveLength(length: Int) {
        var length = length
        length = Math.max(8, Math.min(mWidth!! * 2, length))
        if (length != mWaveLength) {
            mWaveLength = length
            updateMask(mWidth!!, mWaveLength, mWaveHeight)
            invalidateSelf()
        }
    }

    fun setIndeterminateAnimator(animator: ValueAnimator) {
        if (mAnimator === animator) {
            return
        }
        if (mAnimator != null) {
            mAnimator!!.removeUpdateListener(this)
            mAnimator!!.cancel()
        }
        mAnimator = animator
        if (mAnimator != null) {
            mAnimator!!.addUpdateListener(this)
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        mDrawable.setBounds(left, top, right, bottom)
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        updateBounds(bounds!!)
    }

    private fun updateBounds(bounds: Rect) {
        if (bounds.width() <= 0 || bounds.height() <= 0) {
            return
        }
        if (mWidth!! < 0 || mHeight!! < 0) {
            mWidth = bounds.width()
            mHeight = bounds.height()
            if (mWaveHeight == UNDEFINED_VALUE) {
                mWaveHeight = max(8f, (mHeight!! * WAVE_HEIGHT_FACTOR)).roundToInt()
            }
            if (mWaveLength == UNDEFINED_VALUE) {
                mWaveLength = mWidth!!
            }
            if (mWaveStep == UNDEFINED_VALUE) {
                mWaveStep = max(1f, (mWidth!! * WAVE_SPEED_FACTOR)).roundToInt()
            }
            updateMask(mWidth!!, mWaveLength, mWaveHeight)
        }
    }

    override fun onLevelChange(level: Int): Boolean {
        setProgress(level / 500f)
        return true
    }

    override fun getIntrinsicHeight(): Int {
        return mHeight!!
    }

    override fun getIntrinsicWidth(): Int {
        return mWidth!!
    }

    fun setIndeterminate(indeterminate: Boolean) {
        mIndeterminate = indeterminate
        if (mIndeterminate) {
            if (mAnimator == null) {
                mAnimator = getDefaultAnimator()
            }
            mAnimator?.addUpdateListener(this)
            mAnimator?.start()
        } else {
            if (mAnimator != null) {
                mAnimator?.removeUpdateListener(this)
                mAnimator?.cancel()
            }
            level = calculateLevel()
        }
    }
    fun isIndeterminate(): Boolean {
        return mIndeterminate
    }

    private fun calculateLevel(): Int {
        return (mHeight!! - mWaveLevel) * 2000 / (mHeight!! + mWaveHeight)
    }

    private fun getDefaultAnimator(): ValueAnimator? {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.interpolator = DecelerateInterpolator()
        animator.duration = 500
        return animator
    }

    override fun draw(canvas: Canvas) {
        if (mProgress <= 0.001f) {
            return
        }


        val sc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.saveLayer(0f, 0f, mWidth!!.toFloat(), mHeight!!.toFloat(), null)
        } else {
            canvas.saveLayer(0f, 0f, mWidth!!.toFloat(), mHeight!!.toFloat(), null,
                Canvas.ALL_SAVE_FLAG
            )

        }

        if (mWaveLevel > 0) {
            canvas.clipRect(0, mWaveLevel, mWidth!!, mHeight!!)
        }

        mDrawable.draw(canvas)

        if (mProgress >= 0.999f) {
            return
        }

        mWaveOffset += mWaveStep
        if (mWaveOffset > mWaveLength) {
            mWaveOffset -= mWaveLength
        }

        if (mMask != null) {
            mMatrix.setTranslate(-(mWaveOffset.toFloat()), mWaveLevel.toFloat())
            canvas.drawBitmap(mMask!!, mMatrix, mPaint)
        }
        canvas.restoreToCount(sc)
    }

    override fun setAlpha(alpha: Int) {
        mDrawable.alpha = alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mCurFilter = colorFilter
        // invalidateSelf()
    }

    override fun isRunning(): Boolean {
        return mRunning
    }

    override fun start() {
        mRunning = true
        Choreographer.getInstance().postFrameCallback(mFrameCallback)
    }

    override fun stop() {
        mRunning = false
        Choreographer.getInstance().removeFrameCallback(mFrameCallback)
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        if (mIndeterminate) {
            setProgress(animation!!.animatedFraction)
            if (!mRunning) {
                //  invalidateSelf()
            }
        }
    }

    private fun updateMask(width: Int, length: Int, height: Int) {
        if (width <= 0 || length <= 0 || height <= 0) {
            Log.w(ContentValues.TAG, "updateMask: size must > 0")
            mMask = null
            return
        }

        val count:Int = ceil((width.toFloat() + length) / length).roundToInt()
        val bm = Bitmap.createBitmap((length * count), height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bm)
        val p = Paint()
        val amplitude = height / 2
        val path = Path()
        path.moveTo(0.0f, amplitude.toFloat())
        val stepX = length / 4f
        var x = 0f
        var y = -amplitude.toFloat()
        for (i in 0 until count * 2) {
            x += stepX
            path.quadTo(x, y, x + stepX, amplitude.toFloat())
            x += stepX
            y = bm.height - y
        }
        path.lineTo(bm.width.toFloat(), height.toFloat())
        path.lineTo(0.0f, height.toFloat())
        path.close()
        c.drawPath(path, p)
        mMask = bm
    }

    private fun setProgress(progress: Float) {
        mProgress = progress
        mWaveLevel = mHeight!! - ((mHeight!! + mWaveHeight) * mProgress).toInt()
        invalidateSelf()
    }

}