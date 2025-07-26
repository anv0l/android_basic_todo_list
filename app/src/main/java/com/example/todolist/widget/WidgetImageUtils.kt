package com.example.todolist.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.Log
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.example.todolist.R

object WidgetImageUtils {
    private const val CHECKBOX_SIZE_DP = 24

    fun setVectorCheckbox(
        context: Context,
        views: RemoteViews,
        imageViewId: Int,
        isChecked: Boolean
    ) {
        views.setImageViewBitmap(
            imageViewId,
            createThemedVectorBitmap(
                context = context,
                isChecked = isChecked
            )
        )
    }

    private fun createThemedVectorBitmap(
        context: Context,
        isChecked: Boolean
    ): Bitmap {
        val vectorRes = if (isChecked) R.drawable.check_box_24dp
        else R.drawable.check_box_outline_blank_24

        val color = getThemeColor(
            context,
            if (isChecked)
                com.google.android.material.R.attr.colorSecondary
            else android.R.attr.textColorSecondary
        )

        return try {
            createColoredVectorBitmap(context, vectorRes, color)
        } catch (e: Exception) {
            Log.e("WidgetImage", "Vector rendering failed", e)
            createFallbackBitmap(context, isChecked, color)
        }
    }

    private fun createColoredVectorBitmap(
        context: Context,
        @DrawableRes vectorRes: Int,
        @ColorInt color: Int
    ): Bitmap {
        val sizePx = (CHECKBOX_SIZE_DP * context.resources.displayMetrics.density).toInt()
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val drawable = AppCompatResources.getDrawable(context, vectorRes)?.mutate()
            ?: throw IllegalStateException("Vector resource not found")

        val paint = Paint().apply {
            colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        }

        Log.d("WidgetDebug", "Tint: $color")

        canvas.apply {
            drawable.setBounds(0, 0, width, height)
            drawable.draw(this)
            drawBitmap(bitmap, 0f, 0f, paint)
        }

        return bitmap
    }

    private fun createFallbackBitmap(
        context: Context,
        isChecked: Boolean,
        @ColorInt color: Int
    ): Bitmap {
        val sizePx = (CHECKBOX_SIZE_DP * context.resources.displayMetrics.density).toInt()
        return Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).drawColor(color)
        }
    }

    @ColorInt
    private fun getThemeColor(context: Context, @AttrRes attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}