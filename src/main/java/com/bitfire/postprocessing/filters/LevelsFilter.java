/*******************************************************************************
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.bitfire.postprocessing.filters;

import com.bitfire.utils.ShaderLoader;

/** Controls levels of brightness and contrast
 * @author tsagrista */
public final class LevelsFilter extends Filter<LevelsFilter> {
	private float brightness = 0.0f;
	private float contrast = 1.0f;

	public enum Param implements Parameter {
		// @formatter:off
		Texture("u_texture0", 0), Brightness("u_brightness", 0), Contrast("u_contrast", 0);
		// @formatter:on

		private String mnemonic;
		private int elementSize;

		private Param (String mnemonic, int arrayElementSize) {
			this.mnemonic = mnemonic;
			this.elementSize = arrayElementSize;
		}

		@Override
		public String mnemonic () {
			return this.mnemonic;
		}

		@Override
		public int arrayElementSize () {
			return this.elementSize;
		}
	}

	public LevelsFilter () {
		super(ShaderLoader.fromFile("screenspace", "levels"));
		rebind();
	}

	/** Set the contrast
	 * @param contrast The contrast value in [0..2] */
	public void setContrast (float contrast) {
		this.contrast = contrast;
		setParam(Param.Contrast, this.contrast);
	}

	/** Set the brightness
	 * @param brightness The brightness value in [-1..1] */
	public void setBrightness (float brightness) {
		this.brightness = brightness;
		setParam(Param.Brightness, this.brightness);
	}

	@Override
	public void rebind () {
		// reimplement super to batch every parameter
		setParams(Param.Texture, u_texture0);
		setParams(Param.Brightness, brightness);
		setParams(Param.Contrast, contrast);
		endParams();
	}

	@Override
	protected void onBeforeRender () {
		inputTexture.bind(u_texture0);
	}
}
