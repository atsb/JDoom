/*
 * Copyright (C) 2017 Good Sign
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package v.renderers;

import doom.CommandVariable;
import doom.DoomMain;
import java.util.function.Function;
import jdoom.Engine;
import rr.SceneRenderer;
import rr.parallel.ParallelRenderer;

/**
 * This class helps to choose between scene renderers
 */
public enum SceneRendererMode {
    Parallel(SceneRendererMode::Parallel_8, SceneRendererMode::Parallel_16, SceneRendererMode::Parallel_32);
    
    private static final boolean cVarParallel = Engine.getCVM().present(CommandVariable.PARALLELRENDERER);
    private static final int[] threads = cVarParallel
        ? parseSwitchConfig()
        : new int[]{2, 2, 2};
            
    final SG<byte[], byte[]> indexedGen;
    final SG<byte[], short[]> hicolorGen;
    final SG<byte[], int[]> truecolorGen;

    SceneRendererMode(SG<byte[], byte[]> indexed, SG<byte[], short[]> hi, SG<byte[], int[]> truecolor) {
        this.indexedGen = indexed;
        this.hicolorGen = hi;
        this.truecolorGen = truecolor;
    }
    
    static int[] parseSwitchConfig() {
        // Try parsing walls, or default to 1
        final int walls = Engine.getCVM().get(CommandVariable.PARALLELRENDERER, Integer.class, 0).orElse(1);
        // Try parsing floors. If wall succeeded, but floors not, it will default to 1.
        final int floors = Engine.getCVM().get(CommandVariable.PARALLELRENDERER, Integer.class, 1).orElse(1);
        // In the worst case, we will use the defaults.
        final int masked = Engine.getCVM().get(CommandVariable.PARALLELRENDERER, Integer.class, 2).orElse(2);
        return new int[]{walls, floors, masked};
    }

    private static SceneRenderer<byte[], byte[]> Parallel_8(DoomMain<byte[], byte[]> DOOM) {
        return new ParallelRenderer.Indexed(DOOM, threads[0], threads[1], threads[2]);
    }
    
    private static SceneRenderer<byte[], short[]> Parallel_16(DoomMain<byte[], short[]> DOOM) {
        return new ParallelRenderer.HiColor(DOOM, threads[0], threads[1], threads[2]);
    }
    
    private static SceneRenderer<byte[], int[]> Parallel_32(DoomMain<byte[], int[]> DOOM) {
        return new ParallelRenderer.TrueColor(DOOM, threads[0], threads[1], threads[2]);
    }
    
    interface SG<T, V> extends Function<DoomMain<T, V>, SceneRenderer<T, V>> {}
}
