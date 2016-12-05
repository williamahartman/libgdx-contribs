/**
 * Fisheye effect
 * 
 * @author tsagrista
 */

package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.FisheyeDistortion;

public final class Fisheye extends PostProcessorEffect {
    private FisheyeDistortion distort;

    public Fisheye() {
        distort = new FisheyeDistortion();
    }

    @Override
    public void dispose() {
        distort.dispose();
    }

    @Override
    public void rebind() {
        distort.rebind();
    }

    @Override
    public void render(FrameBuffer src, FrameBuffer dest) {
        restoreViewport(dest);
        distort.setInput(src).setOutput(dest).render();
    };

}
