package com.github.ericytsang.lib.simulation

import kotlin.concurrent.thread

class SimpleLooper(val loopee:Loopee,_updatesPerSecond:Int = 30):Looper
{
    var updatesPerSecond:Int = _updatesPerSecond

    val updateInterval = run()
    {
        if (updatesPerSecond <= 0)
        {
            throw IllegalArgumentException("updatesPerSecond ($updatesPerSecond) <= 0")
        }
        else
        {
            1000/updatesPerSecond
        }
    }

    private var simulationThread = thread(start = false) {}

    override fun start()
    {
        if (simulationThread.isAlive)
        {
            throw IllegalStateException("looper is already started")
        }
        simulationThread = thread(isDaemon = true)
        {
            var lastTimestamp = System.currentTimeMillis()
            var lag = 0L
            while (!Thread.interrupted())
            {
                val thisTimestamp = System.currentTimeMillis()
                var hasUpdated = false
                lag += thisTimestamp-lastTimestamp
                lastTimestamp = thisTimestamp

                loopee.processInput()

                while (lag > updateInterval)
                {
                    loopee.update()
                    hasUpdated = true
                    lag -= updateInterval
                }

                if (hasUpdated)
                {
                    loopee.render()
                }
            }
        }
    }

    override fun stop()
    {
        simulationThread.interrupt()
        simulationThread.join()
    }
}