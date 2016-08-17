package com.github.ericytsang.lib.simulation

interface Renderer<in Renderee>
{
    fun render(renderees:Iterable<Renderee>)
}