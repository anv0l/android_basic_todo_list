package com.example.todolist.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

object UserAvatar {
    fun generateIdenticon(
        email: String,
        size: Int = 256,
        gridSize: Int = 5,
        @ColorInt foregroundColor: Int,
        @ColorInt backgroundColor: Int
    ): Bitmap {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val bgPaint = Paint().apply {
            color = backgroundColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint)

        val patternBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val patternCanvas = Canvas(patternBitmap)

        val fgPaint = Paint().apply {
            color = foregroundColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val hash = email.hashCode()
        val cellSize = size / gridSize
        val center = gridSize / 2

        for (x in 0..center) {
            for (y in 0 until gridSize) {
                if ((hash shr (x * gridSize + y)) and 1 == 1) {
                    patternCanvas.drawRect(
                        x * cellSize.toFloat(),
                        y * cellSize.toFloat(),
                        (x + 1) * cellSize.toFloat(),
                        (y + 1) * cellSize.toFloat(),
                        fgPaint
                    )
                    if (x < center) {
                        patternCanvas.drawRect(
                            (gridSize - x - 1) * cellSize.toFloat(),
                            y * cellSize.toFloat(),
                            (gridSize - x) * cellSize.toFloat(),
                            (y + 1) * cellSize.toFloat(),
                            fgPaint
                        )
                    }
                }
            }
        }

        val mask = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(mask)
        val maskPaint = Paint().apply {
            color = backgroundColor
            style = Paint.Style.FILL
        }
        maskCanvas.drawCircle(size / 2f, size / 2f, size / 2f, maskPaint)

        val paint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        canvas.drawBitmap(patternBitmap, 0f, 0f, null)
        canvas.drawBitmap(mask, 0f, 0f, paint)

        return output
    }

    fun bitmapToDrawable(context: Context, bitmap: Bitmap): Drawable {
        return BitmapDrawable(context.resources, bitmap)
    }
}