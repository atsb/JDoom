/**
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

package v.scale;

import doom.CVarManager;
import doom.CommandVariable;
import m.Settings;

public class VisualSettings {
    
    /** Parses the command line for resolution-specific commands, and creates
     *  an appropriate IVideoScale object.
     *  
     * @param CM
     * @return
     */

    /** Default video scale is (320 x 200) */
    public final static VideoScale renderer_resolution = new VideoScaleInfo(1.0f);
    
    public final static VideoScale parse(CVarManager CVM){
        
        { // check multiply
            // -multiply parameter defined from linux doom.
            // It gets priority over all others, if present.
            final int multiply = CVM.get(CommandVariable.MULTIPLY, Integer.class, 0).orElse(-1);

            // If -multiply was successful, trump any others.
            // Implied to be a solid multiple of the vanilla resolution.
            if (multiply > 0 && multiply <= 5) {
                return new VideoScaleInfo(multiply);
            }
        } // forget multiply
        
        // At least one of them is not a dud.
        final int mulx, muly, mulf;
        
        // check width & height
        final int width = CVM.get(CommandVariable.WIDTH, Integer.class, 0).orElse(-1);
        final int height = CVM.get(CommandVariable.HEIGHT, Integer.class, 0).orElse(-1);

        // Nothing to do?
        if (height == -1 && width == -1) {
                return renderer_resolution;
        }

        // Break them down to the nearest multiple of the base width or height.
        mulx = Math.round((float) width / VideoScale.BASE_WIDTH);
        muly = Math.round((float) height / VideoScale.BASE_HEIGHT);
        
        // Do not accept zero or sub-vanilla resolutions
        if (mulx > 0 || muly > 0) {
            // Use the maximum multiplier. We don't support skewed
            // aspect ratios yet.
            mulf = Math.max(mulx, muly);
            if (mulf >= 1 && mulf <= 5) {
                return new VideoScaleInfo(mulf);
            }
        }
        
        // In all other cases...
        return renderer_resolution;
    }
}
