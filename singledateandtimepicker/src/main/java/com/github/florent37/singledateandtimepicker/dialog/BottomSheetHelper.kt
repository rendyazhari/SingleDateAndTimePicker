package com.github.florent37.singledateandtimepicker.dialog

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.FrameLayout
import com.github.florent37.singledateandtimepicker.R


class BottomSheetHelper {
    private var context: Context? = null
    private var layoutId = 0
    private var view: View? = null
    private var listener: Listener? = null
    private var handler: Handler? = null
    private var windowManager: WindowManager? = null

    constructor(context: Context?, layoutId: Int) {
        this.context = context
        this.layoutId = layoutId
        handler = Handler(Looper.getMainLooper())
    }

    private fun initClass() {
        handler?.postDelayed({
            if (context is Activity) {
                windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager

                view = LayoutInflater.from(context).inflate(layoutId, null, true);

                var layoutParams = WindowManager.LayoutParams(
                        // Shrink the window to wrap the content rather than filling the screen
                        WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                        // Don't let it grab the input focus
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        // Make the underlying application window visible through any transparent parts
                        PixelFormat.TRANSLUCENT)

                if ((layoutParams.softInputMode and WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION) == 0) {
                    val nl = WindowManager.LayoutParams()
                    nl.copyFrom(layoutParams)
                    nl.softInputMode = nl.softInputMode.or(WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION)
                    layoutParams = nl
                }

                windowManager?.addView(view, layoutParams);

                view?.findViewById<FrameLayout>(R.id.bottom_sheet_background)?.setOnClickListener {
                    hide()
                }

                view?.viewTreeObserver?.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        view?.viewTreeObserver?.removeOnPreDrawListener(this)
                        listener?.onLoaded(view)
                        animateBottomSheet()
                        return false
                    }
                })

            }
        }, 100)
    }

    fun setListener(listener: Listener?): BottomSheetHelper? {
        this.listener = listener
        return this
    }

    fun display() {
        initClass()
    }

    fun hide() {
        handler?.postDelayed({
            val objectAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, view?.height?.toFloat() ?: 0f)
            objectAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view?.visibility = View.GONE
                    listener?.onClose()
                    remove()
                }
            })
            objectAnimator.start()
        }, 200)
    }

    fun dismiss() {
        remove()
    }

    private fun remove() {
        windowManager?.removeView(view)
    }

    private fun animateBottomSheet() {
        val objectAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view!!.height.toFloat(), 0f)
        objectAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                listener?.onOpen()
            }
        })
        objectAnimator.start()
    }

    interface Listener {
        fun onOpen()
        fun onLoaded(view: View?)
        fun onClose()
    }
}