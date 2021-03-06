package com.github.ericytsang.lib.simulation

import javafx.application.Platform
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.transform.Affine
import javafx.scene.transform.Transform
import java.util.concurrent.CountDownLatch

class CanvasRenderer(val canvas:Canvas,_cellLength:Double):Renderer
{
    /**
     * last transform applied to the [GraphicsContext] of [canvas] in while
     * rendering. this controls where in the simulation the user gets to see.
     */
    val viewTransform:Affine = Transform.affine(1.0,0.0,0.0,1.0,0.0,0.0)
    //.apply {appendTranslation(128.0,128.0)}
    //.apply {appendRotation(-10.0)}

    /**
     * length of a cell used when rendering [Entity] objects.
     */
    var cellLength = _cellLength

        set(value)
        {
            if (value <= 0.0)
            {
                throw IllegalArgumentException("value ($value) cannot be <= 0")
            }
            else
            {
                field = value
            }
        }

    init
    {
        this.cellLength = _cellLength
    }

    override fun render(renderees:Iterable<*>)
    {
        val releasedOnRenderFinished = CountDownLatch(1)
        Platform.runLater()
        {
            val context = canvas.graphicsContext2D

            // apply all transformations that are common to all entities
            //context.transform = viewTransform.clone()

            // render every entity
            renderees
                .filter {it is CanvasRenderer.Renderee}
                .map {it as CanvasRenderer.Renderee}
                .sortedBy {it.renderLayer}
                .forEach()
                {
                    context.save()

                    // apply entity specific transformations, then render the entity
                    context.transform = context.transform.apply()
                    {
                        appendTranslation(canvas.width/2,canvas.height/2)
                        append(viewTransform)
                        appendTranslation(cellLength*it.position.x,cellLength*it.position.y)
                        appendRotation(it.direction)
                    }
                    it.render(context,viewTransform.clone(),cellLength)

                    context.restore()
                }

            // release latch indicating that rendering has finished
            releasedOnRenderFinished.countDown()
        }
        releasedOnRenderFinished.await()
    }

    interface Renderee
    {
        val direction:Double
        val position:Position
        val renderLayer:Int
        fun render(graphicsContext:GraphicsContext,viewTransform:Affine,cellLength:Double)
    }

    /**
     * where the center of the entity is. The boundary between cells lies on
     * integers.
     */
    data class Position(val x:Double,val y:Double)
}