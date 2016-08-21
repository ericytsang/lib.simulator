package com.github.ericytsang.lib.simulation

import com.github.ericytsang.lib.collections.ObservableMap
import java.util.WeakHashMap

/**
 * Created by surpl on 8/15/2016.
 */
class Simulation constructor(val renderer:Renderer,looperFactory:Looper.Factory)
{
    private val loopee = MyLoopee()

    val looper:Looper = looperFactory.make(loopee).apply {start()}

    val allEntities:Set<Entity> get()
    {
        return entityToCellsMap.keys
    }

    /**
     * [cellToEntitiesMap] maps cell objects to entity objects.
     *
     * modifying this map will automatically update the [entityToCellsMap] too.
     *
     * meant to be used for looking up entities by the cells.
     */
    val cellToEntitiesMap:MutableMap<Cell,Set<Entity>> = ObservableMap(mutableMapOf<Cell,Set<Entity>>()).apply()
    {
        listeners += {
            change ->

            entityToCellsMap as ObservableMap<Entity,Set<Cell>>

            val cell = change.key

            when
            {
                change.wasAdded ->
                {
                    change.valueAdded!!.forEach()
                    {
                        entity ->
                        entityToCellsMap.wrapee[entity] = entityToCellsMap[entity]?.plus(cell) ?: setOf(cell)
                    }
                }
                change.wasRemoved ->
                {
                    change.valueRemoved!!.forEach()
                    {
                        entity ->
                        entityToCellsMap.wrapee[entity] = entityToCellsMap[entity]?.minus(cell) ?: emptySet()
                    }
                }
            }
        }
    }

    /**
     * [entityToCellsMap] maps entity objects to cell objects.
     *
     * modifying this map will automatically update the [cellToEntitiesMap] too.
     *
     * meant to be used for registering entities with cells so they can be
     * looked up in [cellToEntitiesMap].
     *
     * all entities registered in this map will be processed in the [looper]
     */
    val entityToCellsMap:MutableMap<Entity,Set<Cell>> = ObservableMap(mutableMapOf<Entity,Set<Cell>>()).apply()
    {
        listeners += {
            change ->

            cellToEntitiesMap as ObservableMap<Cell,Set<Entity>>

            val entity = change.key

            when
            {
                change.wasAdded ->
                {
                    change.valueAdded!!.forEach()
                    {
                        cell ->
                        cellToEntitiesMap.wrapee[cell] = cellToEntitiesMap[cell]?.plus(entity) ?: setOf(entity)
                    }
                }
                change.wasRemoved ->
                {
                    change.valueRemoved!!.forEach()
                    {
                        cell ->
                        cellToEntitiesMap.wrapee[cell] = cellToEntitiesMap[cell]?.minus(entity) ?: emptySet()
                    }
                }
            }
        }
    }

    /**
     * to draw something on the simulation's canvas, implement this interface.
     */
    interface Entity
    {
        fun update(simulation:Simulation)
    }

    data class Cell private constructor(val x:Int,val y:Int)
    {
        companion object
        {
            private val instances = WeakHashMap<Pair<Int,Int>,Cell>()
            fun getElseMake(x:Int,y:Int):Cell
            {
                return instances.getOrPut(x to y,{Cell(x,y)})
            }
        }
    }

    private inner class MyLoopee:Loopee
    {
        override fun processInput()
        {
            // todo: implement
        }

        override fun update()
        {
            allEntities.forEach {it.update(this@Simulation)}
        }

        override fun render()
        {
            renderer.render(allEntities)
        }
    }
}
