package com.github.ericytsang.lib.simulation

interface Looper
{
    fun start()
    fun stop()

    interface Factory
    {
        fun make(loopee:Loopee):Looper

        companion object
        {
            fun new(_make:(Loopee)->Looper):Looper.Factory
            {
                return object:Looper.Factory
                {
                    override fun make(loopee:Loopee):Looper
                    {
                        return _make(loopee)
                    }
                }
            }
        }
    }
}
