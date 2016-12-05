/**
 * Fisheye effect
 * 
 * @author tsagrista
 */

package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.CubemapEquirectangularFilter;

public final class CubemapEquirectangular extends PostProcessorEffect {
	private CubemapEquirectangularFilter filter;

	public CubemapEquirectangular () {
		filter = new CubemapEquirectangularFilter();
	}

	@Override
	public void dispose () {
		filter.dispose();
	}

	@Override
	public void rebind () {
		filter.rebind();
	}

	public void setSides (FrameBuffer xpositive, FrameBuffer xnegative, FrameBuffer ypositive, FrameBuffer ynegative,
		FrameBuffer zpositive, FrameBuffer znegative) {
		filter.setSides(xpositive, xnegative, ypositive, ynegative, zpositive, znegative);
	}

	@Override
	public void render (FrameBuffer src, FrameBuffer dest) {
		restoreViewport(dest);
		filter.setInput(src).setOutput(dest).render();
	};

}
