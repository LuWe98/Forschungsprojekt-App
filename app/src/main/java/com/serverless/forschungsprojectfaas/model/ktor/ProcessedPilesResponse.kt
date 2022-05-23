package com.serverless.forschungsprojectfaas.model.ktor

import android.graphics.PointF
import com.serverless.forschungsprojectfaas.extensions.averageRectDimension
import com.serverless.forschungsprojectfaas.extensions.log
import com.serverless.forschungsprojectfaas.model.BoxDimension
import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue

@Serializable
data class ProcessedPilesResponse(
    val processedBoxes: List<ProcessedBox>
) {
    val averageBoxDimension get(): BoxDimension = processedBoxes.map(ProcessedBox::rect).averageRectDimension






    //TODO -> Das geht noch nicht richtig
    //Finds Enclosing Points inside the rectangles
    fun findEnclosedEmptySpaces(): List<PointF> {
        // Um zu schauen ob in dem Abstand keine andere Box über einer anderen liegt
        val averageBoxDimension = averageBoxDimension
        val points = mutableListOf<PointF>()

        processedBoxes.forEach { box ->
            findClosestBoxAboveInLine(box, averageBoxDimension)?.let { boxToTopOf ->
                // Check how big the height difference ist and add points according to the different size:
                val diff = box.top - boxToTopOf.bottom
                // The amount of boxes to insert
                val boxesToInsert: Int = (diff / averageBoxDimension.height * 0.85).toInt()
                val steps = diff / boxesToInsert
                log("To Insert: $boxesToInsert")
                val middleXCoordinate = box.getMiddleXCoordinate(boxToTopOf)
                for (index in 0..boxesToInsert) {
                    points.add(
                        PointF(
                            middleXCoordinate,
                            box.top + steps * (index + 1)
                        )
                    )
                }
            }
        }

        log("POINTS")
        points.forEach {
            //log("TEST: $it")
        }
        return points
    }

    fun findClosestBoxAboveInLine(box: ProcessedBox, averageBoxDimension: BoxDimension): ProcessedBox? = processedBoxes.filter {
        box.top >= it.bottom
                && box.centerX <= it.right
                && box.centerX >= it.left
    }.minByOrNull {
        (box.centerY - it.rect.bottom).absoluteValue
    }?.let {
        // Checks if the distance of the found box is greater than the average height of a box
        // * 0.85 is taken as a means of error
        val spaceBetween = box.top - it.bottom
        if (spaceBetween >= averageBoxDimension.height * 0.85f) {
            it
        } else {
            null
        }
    }

//    fun isBarToLeftPresent(box: ProcessedBox): Boolean {
//
//    }
//
//    fun isBarToRightPresent(box: ProcessedBox): Boolean {
//
//    }

    fun findClosestLeftBar(box: ProcessedBox): ProcessedBox? = processedBoxes.filter {
        box.rect.centerY() <= it.rect.bottom
                && box.rect.centerY() >= it.rect.top
                && box.rect.right > it.rect.right
    }.minByOrNull {
        (box.rect.centerX() - it.rect.right).absoluteValue
    }

    fun findClosestRightBar(box: ProcessedBox): ProcessedBox? = processedBoxes.filter {
        box.rect.centerY() <= it.rect.bottom
                && box.rect.centerY() >= it.rect.top
                && box.rect.left < it.rect.left
    }.minByOrNull {
        (box.rect.centerX() - it.rect.left).absoluteValue
    }
}