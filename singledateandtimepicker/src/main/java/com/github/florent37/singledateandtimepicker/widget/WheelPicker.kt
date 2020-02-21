package com.github.florent37.singledateandtimepicker.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.Paint.*
import android.os.Build
import android.os.Handler
import android.support.annotation.StringRes
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import com.github.florent37.singledateandtimepicker.DateHelper.timeZone
import com.github.florent37.singledateandtimepicker.LocaleHelper.getString
import com.github.florent37.singledateandtimepicker.R
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


abstract class WheelPicker<V>(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val mainHandler: Handler by lazy {
        Handler()
    }
    protected var defaultValue: V
    protected var lastScrollPosition = 0
    protected var listener: Listener<WheelPicker<*>, V>? = null
    var adapter: Adapter<V> = Adapter()
        set(value) {
            field = value
            updateItemTextAlign()
            computeTextSize()
            notifyDatasetChanged()
        }
    private var customLocale: Locale? = null
    private var paint: Paint
    private lateinit var scroller: Scroller
    private var tracker: VelocityTracker? = null
    private var onItemSelectedListener: OnItemSelectedListener? =
        null
    private var onWheelChangeListener: OnWheelChangeListener? = null
    private val rectDrawn = Rect()
    private val rectIndicatorHead = Rect()
    private val rectIndicatorFoot = Rect()
    private val rectCurrentItem = Rect()
    private val camera = Camera()
    private val matrixRotate = Matrix()
    private val matrixDepth = Matrix()
    private var maxWidthText: String? = null
    private var mVisibleItemCount = 0
    private var mDrawnItemCount = 0
    private var mHalfDrawnItemCount = 0
    private var mTextMaxWidth = 0
    private var mTextMaxHeight = 0
    private var mItemTextColor = 0
    private var mSelectedItemTextColor = 0
    private var mItemTextSize = 0
    private var mIndicatorSize = 0
    private var mIndicatorColor = 0
    private var mCurtainColor = 0
    private var mItemSpace = 0
    private var mItemAlign = 0
    private var mItemHeight = 0
    private var mHalfItemHeight = 0
    private var mHalfWheelHeight = 0
    private var selectedItemPosition = 0
    var currentItemPosition = 0
        private set
    private var minFlingY = 0
    private var maxFlingY = 0
    private var minimumVelocity = 50
    private var maximumVelocity = 8000
    private var wheelCenterX = 0
    private var wheelCenterY = 0
    private var drawnCenterX = 0
    private var drawnCenterY = 0
    private var scrollOffsetY = 0
    private var textMaxWidthPosition = 0
    private var lastPointY = 0
    private var downPointY = 0
    private var touchSlop = 8
    private var hasSameWidth = false
    private var hasIndicator = false
    private var hasCurtain = false
    private var hasAtmospheric = false
    open var isCyclic = false
        set(value) {
            field = value
            computeFlingLimitY()
            invalidate()
        }
    var isCurved = false
        set(value) {
            field = value
            requestLayout()
            postInvalidate()
        }
    private var isClick = false
    private var isForceFinishScroll = false
    protected abstract fun initClass()
    protected abstract fun initDefault(): V

    private val runnable: Runnable by lazy {
        object : Runnable {
            override fun run() {
                val itemCount = adapter.itemCount
                if (itemCount == 0) return
                if (scroller.isFinished && !isForceFinishScroll) {
                    if (mItemHeight == 0) return
                    var position =
                        (-scrollOffsetY / mItemHeight + selectedItemPosition) % itemCount
                    position = if (position < 0) position + itemCount else position
                    currentItemPosition = position
                    onItemSelected()
                    onWheelChangeListener?.run {
                        onWheelSelected(position)
                        onWheelScrollStateChanged(SCROLL_STATE_IDLE)
                    }
                }
                if (scroller.computeScrollOffset()) {
                    onWheelChangeListener?.onWheelScrollStateChanged(SCROLL_STATE_SCROLLING)
                    scrollOffsetY = scroller.currY
                    val position =
                        (-scrollOffsetY / mItemHeight + selectedItemPosition) % itemCount
                    onItemSelectedListener?.onCurrentItemOfScroll(this@WheelPicker, position)

                    val item = adapter.getItem(position) ?: return
                    onItemCurrentScroll(position, item)
                    postInvalidate()
                    mainHandler.postDelayed(this, 16)
                }
            }

        }
    }

    constructor(context: Context) : this(context, null)

    init {
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.WheelPicker)
        mItemTextSize = a.getDimensionPixelSize(
            R.styleable.WheelPicker_wheel_item_text_size,
            resources.getDimensionPixelSize(R.dimen.WheelItemTextSize)
        )
        mVisibleItemCount = a.getInt(R.styleable.WheelPicker_wheel_visible_item_count, 7)
        selectedItemPosition = a.getInt(R.styleable.WheelPicker_wheel_selected_item_position, 0)
        hasSameWidth = a.getBoolean(R.styleable.WheelPicker_wheel_same_width, false)
        textMaxWidthPosition =
            a.getInt(R.styleable.WheelPicker_wheel_maximum_width_text_position, -1)
        maxWidthText = a.getString(R.styleable.WheelPicker_wheel_maximum_width_text)
        mSelectedItemTextColor =
            a.getColor(R.styleable.WheelPicker_wheel_selected_item_text_color, -1)
        mItemTextColor = a.getColor(R.styleable.WheelPicker_wheel_item_text_color, -0x777778)
        mItemSpace = a.getDimensionPixelSize(
            R.styleable.WheelPicker_wheel_item_space,
            resources.getDimensionPixelSize(R.dimen.WheelItemSpace)
        )
        isCyclic = a.getBoolean(R.styleable.WheelPicker_wheel_cyclic, false)
        hasIndicator = a.getBoolean(R.styleable.WheelPicker_wheel_indicator, false)
        mIndicatorColor = a.getColor(R.styleable.WheelPicker_wheel_indicator_color, -0x11cccd)
        mIndicatorSize = a.getDimensionPixelSize(
            R.styleable.WheelPicker_wheel_indicator_size,
            resources.getDimensionPixelSize(R.dimen.WheelIndicatorSize)
        )
        hasCurtain = a.getBoolean(R.styleable.WheelPicker_wheel_curtain, false)
        mCurtainColor = a.getColor(R.styleable.WheelPicker_wheel_curtain_color, -0x77000001)
        hasAtmospheric = a.getBoolean(R.styleable.WheelPicker_wheel_atmospheric, false)
        isCurved = a.getBoolean(R.styleable.WheelPicker_wheel_curved, false)
        mItemAlign = a.getInt(R.styleable.WheelPicker_wheel_item_align, ALIGN_CENTER)
        a.recycle()
        updateVisibleItemCount()
        paint = Paint(ANTI_ALIAS_FLAG or DITHER_FLAG or LINEAR_TEXT_FLAG)
        paint.setTextSize(mItemTextSize.toFloat())
        scroller = Scroller(getContext())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
            val conf = ViewConfiguration.get(getContext())
            minimumVelocity = conf.scaledMinimumFlingVelocity
            maximumVelocity = conf.scaledMaximumFlingVelocity
            touchSlop = conf.scaledTouchSlop
        }
        initClass()
        defaultValue = initDefault()
        generateAdapterValues()?.let { adapter.data = it.toMutableList() }
        currentItemPosition = adapter.getItemPosition(defaultValue)
        selectedItemPosition = currentItemPosition
    }

    fun updateAdapter() {
        adapter.data = generateAdapterValues()?.toMutableList() ?: mutableListOf()
        notifyDatasetChanged()
    }

    protected abstract fun generateAdapterValues(): List<V>?
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.adapter = adapter
        setDefault(defaultValue)
    }

    private fun updateVisibleItemCount() {
        if (mVisibleItemCount < 2) {
            throw ArithmeticException("Wheel's visible item count can not be less than 2!")
        }
        if (mVisibleItemCount % 2 == 0) mVisibleItemCount += 1
        mDrawnItemCount = mVisibleItemCount + 2
        mHalfDrawnItemCount = mDrawnItemCount / 2
    }

    private fun computeTextSize() {
        mTextMaxHeight = 0
        mTextMaxWidth = mTextMaxHeight
        if (hasSameWidth) {
            mTextMaxWidth = paint.measureText(adapter.getItemText(0)).toInt()
        } else if (isPosInRang(textMaxWidthPosition)) {
            mTextMaxWidth = paint.measureText(adapter.getItemText(textMaxWidthPosition)).toInt()
        } else if (!TextUtils.isEmpty(maxWidthText)) {
            mTextMaxWidth = paint.measureText(maxWidthText).toInt()
        } else {
            val itemCount = adapter.itemCount
            for (i in 0 until itemCount) {
                val text = adapter.getItemText(i)
                val width = paint.measureText(text).toInt()
                mTextMaxWidth = Math.max(mTextMaxWidth, width)
            }
        }
        val metrics = paint.fontMetrics
        mTextMaxHeight = (metrics.bottom - metrics.top).toInt()
    }

    private fun updateItemTextAlign() {
        when (mItemAlign) {
            ALIGN_LEFT -> paint.textAlign = Paint.Align.LEFT
            ALIGN_RIGHT -> paint.textAlign = Paint.Align.RIGHT
            else -> paint.textAlign = Paint.Align.CENTER
        }
    }

    protected fun updateDefault() {
        setSelectedItemPosition(defaultItemPosition)
    }

    open fun setDefault(defaultValue: V) {
        this.defaultValue = defaultValue
        updateDefault()
    }

    fun setDefaultDate(date: Date) {
        if (adapter.itemCount > 0) {
            val indexOfDate = findIndexOfDate(date)
            defaultValue = adapter.data[indexOfDate]
            setSelectedItemPosition(indexOfDate)
        }
    }

    fun selectDate(date: Date) {
        setSelectedItemPosition(findIndexOfDate(date))
    }

    open fun setCustomLocale(customLocale: Locale) {
        this.customLocale = customLocale
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        // Correct sizes of original content
        var resultWidth = mTextMaxWidth
        var resultHeight =
            mTextMaxHeight * mVisibleItemCount + mItemSpace * (mVisibleItemCount - 1)
        // Correct view sizes again if curved is enable
        if (isCurved) {
            resultHeight = (2 * resultHeight / Math.PI).toInt()
        }
        // Consideration padding influence the view sizes
        resultWidth += paddingLeft + paddingRight
        resultHeight += paddingTop + paddingBottom
        // Consideration sizes of parent can influence the view sizes
        resultWidth = measureSize(modeWidth, sizeWidth, resultWidth)
        resultHeight = measureSize(modeHeight, sizeHeight, resultHeight)
        setMeasuredDimension(resultWidth, resultHeight)
    }

    private fun measureSize(mode: Int, sizeExpect: Int, sizeActual: Int): Int {
        var realSize: Int
        if (mode == MeasureSpec.EXACTLY) {
            realSize = sizeExpect
        } else {
            realSize = sizeActual
            if (mode == MeasureSpec.AT_MOST) realSize = Math.min(realSize, sizeExpect)
        }
        return realSize
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldW: Int,
        oldH: Int
    ) { // Set content region
        rectDrawn[paddingLeft, paddingTop, width - paddingRight] = height - paddingBottom
        // Get the center coordinates of content region
        wheelCenterX = rectDrawn.centerX()
        wheelCenterY = rectDrawn.centerY()
        // Correct item drawn center
        computeDrawnCenter()
        mHalfWheelHeight = rectDrawn.height() / 2
        mItemHeight = rectDrawn.height() / mVisibleItemCount
        mHalfItemHeight = mItemHeight / 2
        // Initialize fling max Y-coordinates
        computeFlingLimitY()
        // Correct region of indicator
        computeIndicatorRect()
        // Correct region of current select item
        computeCurrentItemRect()
    }

    private fun computeDrawnCenter() {
        drawnCenterX = when (mItemAlign) {
            ALIGN_LEFT -> rectDrawn.left
            ALIGN_RIGHT -> rectDrawn.right
            else -> wheelCenterX
        }
        drawnCenterY = (wheelCenterY - (paint.ascent() + paint.descent()) / 2).toInt()
    }

    private fun computeFlingLimitY() {
        val currentItemOffset = selectedItemPosition * mItemHeight
        minFlingY =
            if (isCyclic) Int.MIN_VALUE else -mItemHeight * (adapter.itemCount - 1) + currentItemOffset
        maxFlingY = if (isCyclic) Int.MAX_VALUE else currentItemOffset
    }

    private fun computeIndicatorRect() {
        if (!hasIndicator) return
        val halfIndicatorSize = mIndicatorSize / 2
        val indicatorHeadCenterY = wheelCenterY + mHalfItemHeight
        val indicatorFootCenterY = wheelCenterY - mHalfItemHeight
        rectIndicatorHead[rectDrawn.left, indicatorHeadCenterY - halfIndicatorSize, rectDrawn.right] =
            indicatorHeadCenterY + halfIndicatorSize
        rectIndicatorFoot[rectDrawn.left, indicatorFootCenterY - halfIndicatorSize, rectDrawn.right] =
            indicatorFootCenterY + halfIndicatorSize
    }

    private fun computeCurrentItemRect() {
        if (!hasCurtain && mSelectedItemTextColor == -1) return
        rectCurrentItem[rectDrawn.left, wheelCenterY - mHalfItemHeight, rectDrawn.right] =
            wheelCenterY + mHalfItemHeight
    }

    override fun onDraw(canvas: Canvas) {
        onWheelChangeListener?.onWheelScrolled(scrollOffsetY)
        if (mItemHeight - mHalfDrawnItemCount <= 0) return
        val drawnDataStartPos = -scrollOffsetY / mItemHeight - mHalfDrawnItemCount
        var drawnDataPos = drawnDataStartPos + selectedItemPosition
        var drawnOffsetPos = -mHalfDrawnItemCount
        while (drawnDataPos < drawnDataStartPos + selectedItemPosition + mDrawnItemCount) {
            var data = ""
            if (isCyclic) {
                val itemCount = adapter.itemCount
                var actualPos = drawnDataPos % itemCount
                actualPos = if (actualPos < 0) actualPos + itemCount else actualPos
                data = adapter.getItemText(actualPos)
            } else {
                if (isPosInRang(drawnDataPos)) {
                    data = adapter.getItemText(drawnDataPos)
                }
            }
            paint.color = mItemTextColor
            paint.style = Paint.Style.FILL
            val mDrawnItemCenterY =
                drawnCenterY + drawnOffsetPos * mItemHeight + scrollOffsetY % mItemHeight
            var distanceToCenter = 0
            if (isCurved) { // Correct ratio of item's drawn center to wheel center
                val ratio =
                    (drawnCenterY - Math.abs(drawnCenterY - mDrawnItemCenterY) -
                        rectDrawn.top) * 1.0f / (drawnCenterY - rectDrawn.top)
                // Correct unit
                var unit = 0
                if (mDrawnItemCenterY > drawnCenterY) {
                    unit = 1
                } else if (mDrawnItemCenterY < drawnCenterY) unit = -1
                var degree = -(1 - ratio) * 90 * unit
                if (degree < -90) degree = -90f
                if (degree > 90) degree = 90f
                distanceToCenter = computeSpace(degree.toInt())
                var transX = wheelCenterX
                when (mItemAlign) {
                    ALIGN_LEFT -> transX = rectDrawn.left
                    ALIGN_RIGHT -> transX = rectDrawn.right
                }
                val transY = wheelCenterY - distanceToCenter
                camera.save()
                camera.rotateX(degree)
                camera.getMatrix(matrixRotate)
                camera.restore()
                matrixRotate.preTranslate(-transX.toFloat(), -transY.toFloat())
                matrixRotate.postTranslate(transX.toFloat(), transY.toFloat())
                camera.save()
                camera.translate(0f, 0f, computeDepth(degree.toInt()).toFloat())
                camera.getMatrix(matrixDepth)
                camera.restore()
                matrixDepth.preTranslate(-transX.toFloat(), -transY.toFloat())
                matrixDepth.postTranslate(transX.toFloat(), transY.toFloat())
                matrixRotate.postConcat(matrixDepth)
            }
            if (hasAtmospheric) {
                var alpha =
                    ((drawnCenterY - Math.abs(drawnCenterY - mDrawnItemCenterY)) * 1.0f / drawnCenterY
                        * 255).toInt()
                alpha = if (alpha < 0) 0 else alpha
                paint.alpha = alpha
            }
            // Correct item's drawn centerY base on curved state
            val drawnCenterY =
                if (isCurved) drawnCenterY - distanceToCenter else mDrawnItemCenterY
            // Judges need to draw different color for current item or not
            if (mSelectedItemTextColor != -1) {
                canvas.save()
                if (isCurved) canvas.concat(matrixRotate)
                canvas.clipRect(rectCurrentItem, Region.Op.DIFFERENCE)
                canvas.drawText(data, drawnCenterX.toFloat(), drawnCenterY.toFloat(), paint)
                canvas.restore()
                paint.color = mSelectedItemTextColor
                canvas.save()
                if (isCurved) canvas.concat(matrixRotate)
                canvas.clipRect(rectCurrentItem)
                canvas.drawText(data, drawnCenterX.toFloat(), drawnCenterY.toFloat(), paint)
                canvas.restore()
            } else {
                canvas.save()
                canvas.clipRect(rectDrawn)
                if (isCurved) canvas.concat(matrixRotate)
                canvas.drawText(data, drawnCenterX.toFloat(), drawnCenterY.toFloat(), paint)
                canvas.restore()
            }
            drawnDataPos++
            drawnOffsetPos++
        }
        // Need to draw curtain or not
        if (hasCurtain) {
            paint.color = mCurtainColor
            paint.style = Paint.Style.FILL
            canvas.drawRect(rectCurrentItem, paint)
        }
        // Need to draw indicator or not
        if (hasIndicator) {
            paint.color = mIndicatorColor
            paint.style = Paint.Style.FILL
            canvas.drawRect(rectIndicatorHead, paint)
            canvas.drawRect(rectIndicatorFoot, paint)
        }
    }

    private fun isPosInRang(position: Int): Boolean {
        return position >= 0 && position < adapter.itemCount
    }

    private fun computeSpace(degree: Int): Int {
        return (Math.sin(Math.toRadians(degree.toDouble())) * mHalfWheelHeight).toInt()
    }

    private fun computeDepth(degree: Int): Int {
        return (mHalfWheelHeight - Math.cos(Math.toRadians(degree.toDouble())) * mHalfWheelHeight).toInt()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (null != parent) parent.requestDisallowInterceptTouchEvent(true)
                    if (null == tracker) {
                        tracker = VelocityTracker.obtain()
                    } else {
                        tracker?.clear()
                    }
                    tracker?.addMovement(event)
                    if (!scroller.isFinished) {
                        scroller.abortAnimation()
                        isForceFinishScroll = true
                    }
                    run {
                        lastPointY = event.y.toInt()
                        downPointY = lastPointY
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (abs(downPointY - event.y) < touchSlop
                        && computeDistanceToEndPoint(scroller.finalY % mItemHeight) > 0
                    ) {
                        isClick = true
                        return true
                    }
                    isClick = false
                    tracker?.addMovement(event)
                    onWheelChangeListener?.onWheelScrollStateChanged(SCROLL_STATE_DRAGGING)
                    // Scroll WheelPicker's content
                    val move = event.y - lastPointY
                    if (Math.abs(move) < 1) return true
                    scrollOffsetY += move.toInt()
                    lastPointY = event.y.toInt()
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    if (null != parent) parent.requestDisallowInterceptTouchEvent(false)
                    if (isClick) return true
                    tracker?.addMovement(event)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
                        tracker?.computeCurrentVelocity(1000, maximumVelocity.toFloat())
                    } else {
                        tracker?.computeCurrentVelocity(1000)
                    }
                    // Judges the WheelPicker is scroll or fling base on current velocity
                    isForceFinishScroll = false
                    val velocity = tracker?.yVelocity?.toInt() ?: 0
                    if (Math.abs(velocity) > minimumVelocity) {
                        scroller.fling(0, scrollOffsetY, 0, velocity, 0, 0, minFlingY, maxFlingY)
                        scroller.finalY =
                            scroller.finalY + computeDistanceToEndPoint(scroller.finalY % mItemHeight)
                    } else {
                        scroller.startScroll(
                            0, scrollOffsetY, 0,
                            computeDistanceToEndPoint(scrollOffsetY % mItemHeight)
                        )
                    }
                    // Correct coordinates
                    if (!isCyclic) {
                        if (scroller.finalY > maxFlingY) {
                            scroller.finalY = maxFlingY
                        } else if (scroller.finalY < minFlingY) scroller.finalY = minFlingY
                    }
                    mainHandler.post(runnable)
                    if (null != tracker) {
                        tracker?.recycle()
                        tracker = null
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (null != parent) parent.requestDisallowInterceptTouchEvent(false)
                    if (null != tracker) {
                        tracker?.recycle()
                        tracker = null
                    }
                }
            }
        }
        return true
    }

    private fun computeDistanceToEndPoint(remainder: Int): Int {
        return if (abs(remainder) > mHalfItemHeight) {
            if (scrollOffsetY < 0) {
                -mItemHeight - remainder
            } else {
                mItemHeight - remainder
            }
        } else {
            -remainder
        }
    }

    fun scrollTo(itemPosition: Int) {
        if (itemPosition != currentItemPosition) {
            val differencesLines = currentItemPosition - itemPosition
            val newScrollOffsetY =
                scrollOffsetY + differencesLines * mItemHeight // % adapter.getItemCount();
            val va = ValueAnimator.ofInt(scrollOffsetY, newScrollOffsetY)
            va.duration = 300
            va.addUpdateListener { animation ->
                scrollOffsetY = animation.animatedValue as Int
                invalidate()
            }
            va.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentItemPosition = itemPosition
                    onItemSelected()
                }
            })
            va.start()
        }
    }

    private fun onItemSelected() {
        val position = currentItemPosition
        val item: V = adapter.getItem(position) ?: return
        onItemSelectedListener?.onItemSelected(this, item, position)
        onItemSelected(position, item)
    }

    protected open fun onItemSelected(position: Int, item: V) {
        listener?.onSelected(this, position, item)
    }

    protected open fun onItemCurrentScroll(position: Int, item: V) {
        if (lastScrollPosition != position) {
            if (listener != null) {
                listener?.onCurrentScrolled(this, position, item)
                if (lastScrollPosition == adapter.itemCount - 1 && position == 0) {
                    onFinishedLoop()
                }
            }
            lastScrollPosition = position
        }
    }

    protected open fun onFinishedLoop() {}
    protected open fun getFormattedValue(value: Any): String {
        return value.toString()
    }

    var visibleItemCount: Int
        get() = mVisibleItemCount
        set(count) {
            mVisibleItemCount = count
            updateVisibleItemCount()
            requestLayout()
        }

    fun setOnItemSelectedListener(listener: OnItemSelectedListener?) {
        onItemSelectedListener = listener
    }

    fun getSelectedItemPosition(): Int = selectedItemPosition

    fun setSelectedItemPosition(position: Int) {
        var pos = position
        pos = min(pos, adapter.itemCount - 1)
        pos = max(pos, 0)
        selectedItemPosition = pos
        currentItemPosition = pos
        scrollOffsetY = 0
        computeFlingLimitY()
        requestLayout()
        invalidate()
    }

    val defaultItemPosition: Int
        get() = adapter.data.indexOf(defaultValue)

    val todayItemPosition: Int
        get() = (adapter.data as? List<*>)?.indexOf(getLocalizedString(R.string.picker_today)) ?: 0

    fun notifyDatasetChanged() {
        if (selectedItemPosition > adapter.itemCount - 1
            || currentItemPosition > adapter.itemCount - 1
        ) {
            currentItemPosition = adapter.itemCount - 1
            selectedItemPosition = currentItemPosition
        } else {
            selectedItemPosition = currentItemPosition
        }
        scrollOffsetY = 0
        computeTextSize()
        computeFlingLimitY()
        requestLayout()
        postInvalidate()
    }

    fun setSameWidth(hasSameWidth: Boolean) {
        this.hasSameWidth = hasSameWidth
        computeTextSize()
        requestLayout()
        invalidate()
    }

    fun hasSameWidth(): Boolean = hasSameWidth

    fun setOnWheelChangeListener(listener: OnWheelChangeListener?) {
        onWheelChangeListener = listener
    }

    var maximumWidthText: String?
        get() = maxWidthText
        set(text) {
            if (null == text) throw NullPointerException("Maximum width text can not be null!")
            maxWidthText = text
            computeTextSize()
            requestLayout()
            postInvalidate()
        }

    var maximumWidthTextPosition: Int
        get() = textMaxWidthPosition
        set(position) {
            if (!isPosInRang(position)) {
                throw ArrayIndexOutOfBoundsException(
                    "Maximum width text Position must in [0, " +
                        adapter.itemCount + "), but current is " + position
                )
            }
            textMaxWidthPosition = position
            computeTextSize()
            requestLayout()
            postInvalidate()
        }

    var selectedItemTextColor: Int
        get() = mSelectedItemTextColor
        set(color) {
            mSelectedItemTextColor = color
            computeCurrentItemRect()
            postInvalidate()
        }

    var itemTextColor: Int
        get() = mItemTextColor
        set(color) {
            mItemTextColor = color
            postInvalidate()
        }

    var itemTextSize: Int
        get() = mItemTextSize
        set(size) {
            if (mItemTextSize != size) {
                mItemTextSize = size
                paint.textSize = mItemTextSize.toFloat()
                computeTextSize()
                requestLayout()
                postInvalidate()
            }
        }

    var itemSpace: Int
        get() = mItemSpace
        set(space) {
            mItemSpace = space
            requestLayout()
            postInvalidate()
        }

    fun setIndicator(hasIndicator: Boolean) {
        this.hasIndicator = hasIndicator
        computeIndicatorRect()
        postInvalidate()
    }

    fun hasIndicator(): Boolean = hasIndicator

    var indicatorSize: Int
        get() = mIndicatorSize
        set(size) {
            mIndicatorSize = size
            computeIndicatorRect()
            postInvalidate()
        }

    var indicatorColor: Int
        get() = mIndicatorColor
        set(color) {
            mIndicatorColor = color
            postInvalidate()
        }

    fun setCurtain(hasCurtain: Boolean) {
        this.hasCurtain = hasCurtain
        computeCurrentItemRect()
        postInvalidate()
    }

    fun hasCurtain(): Boolean = hasCurtain

    var curtainColor: Int
        get() = mCurtainColor
        set(color) {
            mCurtainColor = color
            postInvalidate()
        }

    fun setAtmospheric(hasAtmospheric: Boolean) {
        this.hasAtmospheric = hasAtmospheric
        postInvalidate()
    }

    fun hasAtmospheric(): Boolean {
        return hasAtmospheric
    }

    var itemAlign: Int
        get() = mItemAlign
        set(align) {
            mItemAlign = align
            updateItemTextAlign()
            computeDrawnCenter()
            postInvalidate()
        }

    var typeface: Typeface?
        get() = paint.typeface
        set(tf) {
            paint.typeface = tf
            computeTextSize()
            requestLayout()
            postInvalidate()
        }

    /**
     * TODO: [Adapter.data] could contain 'Data' class objects. 'Data' could be composed of
     * a String: displayedValue (the value to be displayed in the wheel) and
     * a Date/Calendar: comparisonDate (a reference date/calendar that will help to find the index).
     * This could clean this method and [.getFormattedValue].
     *
     *
     * Finds the index in the wheel for a date
     *
     * @param date the targeted date
     * @return the index closed to `date`. Returns 0 if not found.
     */
    open fun findIndexOfDate(date: Date): Int {
        val formatItem = getFormattedValue(date)
        if (this is WheelDayOfMonthPicker) {
            val calendar = Calendar.getInstance()
            calendar.timeZone = timeZone
            calendar.time = date
            return calendar[Calendar.DAY_OF_MONTH] - 1
        }
        if (this is WheelDayPicker) {
            val today = getFormattedValue(Date())
            if (today == formatItem) {
                return todayItemPosition
            }
        }
        if (this is WheelMonthPicker) {
            val calendar = Calendar.getInstance()
            calendar.timeZone = timeZone
            calendar.time = date
            return calendar[Calendar.MONTH]
        }
        if (this is WheelYearPicker) {
            val yearPick = this as WheelYearPicker
            val calendar = Calendar.getInstance()
            calendar.timeZone = timeZone
            calendar.time = date
            return calendar[Calendar.YEAR] - yearPick.minYear
        }
        var formatItemInt = Int.MIN_VALUE
        try {
            formatItemInt = formatItem.toInt()
        } catch (e: NumberFormatException) {
        }
        val itemCount = adapter.itemCount
        var index = 0
        for (i in 0 until itemCount) {
            val `object` = adapter.getItemText(i)
            if (formatItemInt != Int.MIN_VALUE) { // displayed values are Integers
                var objectInt = `object`.toInt()
                if (this is WheelHourPicker && (this as WheelHourPicker).isAmPm) { // In case of hours and AM/PM mode, apply modulo 12
                    objectInt %= 12
                }
                if (objectInt <= formatItemInt) {
                    index = i
                }
            } else if (formatItem == `object`) {
                return i
            }
        }
        return index
    }

    fun getLocalizedString(@StringRes stringRes: Int): String = getString(
        context,
        currentLocale,
        stringRes
    )

    @get:TargetApi(Build.VERSION_CODES.N)
    val currentLocale: Locale
        get() {
            customLocale?.let { return it }
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                resources.configuration.locales[0]
            } else {
                resources.configuration.locale
            }
        }

    interface BaseAdapter<V> {
        val itemCount: Int
        fun getItem(position: Int): V
        fun getItemText(position: Int): String
    }

    interface OnItemSelectedListener {
        fun onItemSelected(
            picker: WheelPicker<*>?,
            data: Any?,
            position: Int
        )

        fun onCurrentItemOfScroll(picker: WheelPicker<*>?, position: Int)
    }

    interface OnWheelChangeListener {
        /**
         *
         *
         * Invoke when WheelPicker scroll stopped
         * WheelPicker will return a distance offset which between current scroll position and
         * initial position, this offset is a positive or a negative, positive means WheelPicker is
         * scrolling from bottom to top, negative means WheelPicker is scrolling from top to bottom
         *
         * @param offset
         *
         *
         * Distance offset which between current scroll position and initial position
         */
        fun onWheelScrolled(offset: Int)

        /**
         *
         *
         * Invoke when WheelPicker scroll stopped
         * This method will be called when WheelPicker stop and return current selected item data's
         * position in list
         *
         * @param position
         *
         *
         * Current selected item data's position in list
         */
        fun onWheelSelected(position: Int)

        /**
         *
         *
         * Invoke when WheelPicker's scroll state changed
         * The state of WheelPicker always between idle, dragging, and scrolling, this method will
         * be called when they switch
         *
         * @param state [WheelPicker.SCROLL_STATE_IDLE]
         * [WheelPicker.SCROLL_STATE_DRAGGING]
         * [WheelPicker.SCROLL_STATE_SCROLLING]
         *
         *
         * State of WheelPicker, only one of the following
         * [WheelPicker.SCROLL_STATE_IDLE]
         * Express WheelPicker in state of idle
         * [WheelPicker.SCROLL_STATE_DRAGGING]
         * Express WheelPicker in state of dragging
         * [WheelPicker.SCROLL_STATE_SCROLLING]
         * Express WheelPicker in state of scrolling
         */
        fun onWheelScrollStateChanged(state: Int)
    }

    protected interface Listener<PICKER : WheelPicker<*>?, V> {
        fun onSelected(picker: PICKER, position: Int, value: V)
        fun onCurrentScrolled(picker: PICKER, position: Int, value: V)
    }

    class Adapter<V> @JvmOverloads constructor(data: List<V> = ArrayList()) :
        BaseAdapter<Any?> {
        var data: MutableList<V> = mutableListOf()
            set(value) {
                field.clear()
                field.addAll(value)
            }
        override val itemCount: Int
            get() = data.size

        override fun getItem(position: Int): V? =
            if (itemCount == 0) null else data[(position + itemCount) % itemCount]

        override fun getItemText(position: Int): String = data[position].toString()

        fun addData(data: List<V>?) {
            data?.let { this.data.addAll(it) }
        }

        fun getItemPosition(value: V): Int = data.indexOf(value) ?: -1

        init {
            this.data.addAll(data)
        }
    }

    companion object {
        const val SCROLL_STATE_IDLE = 0
        const val SCROLL_STATE_DRAGGING = 1
        const val SCROLL_STATE_SCROLLING = 2
        const val ALIGN_CENTER = 0
        const val ALIGN_LEFT = 1
        const val ALIGN_RIGHT = 2
        const val FORMAT = "%1$02d" // two digits
    }
}