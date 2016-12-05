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

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.bitfire.utils.ShaderLoader;

/** Scattering Light effect.
 * @see <a href="https://medium.com/community-play-3d/god-rays-whats-that-5a67f26aeac2">https://medium.com/community-play-3d/god-
 *      rays-whats-that-5a67f26aeac2</a>
 * @author Toni Sagrista **/
public final class Glow extends Filter<Glow> {
	// Number of light supported
	public static int N = 30;
	private Vector2 viewport;

	private float[] lightPositions;
	private float[] lightViewAngles;
	private float[] lightColors;
	private int nLights;
	private int nSamples = 30;
	private float textureScale = 1f;

	private Texture lightGlowTexture;

	public enum Param implements Parameter {
		// @formatter:off
		Texture("u_texture0", 0), LightGlowTexture("u_texture1", 0), LightPositions("u_lightPositions", 2), LightViewAngles(
			"u_lightViewAngles", 1), LightColors("u_lightColors",
				3), Viewport("u_viewport", 2), NLights("u_nLights", 0), NSamples("u_nSamples", 0), TextureScale("u_textureScale", 0);
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

	public Glow (int width, int height) {
		super(ShaderLoader.fromFile("screenspace", "lightglow"));
		lightPositions = new float[N * 2];
		lightViewAngles = new float[N];
		lightColors = new float[N * 3];
		viewport = new Vector2(width, height);
		rebind();
	}

	public void setViewportSize (float width, float height) {
		this.viewport.set(width, height);
		setParam(Param.Viewport, this.viewport);
	}

	public void setLightPositions (int nLights, float[] vec) {
		this.nLights = nLights;
		this.lightPositions = vec;
		setParam(Param.NLights, this.nLights);
		setParamv(Param.LightPositions, this.lightPositions, 0, N * 2);
	}

	public void setLightViewAngles (float[] ang) {
		this.lightViewAngles = ang;
		setParamv(Param.LightViewAngles, this.lightViewAngles, 0, N);
	}

	public void setLightColors (float[] colors) {
		this.lightColors = colors;
		setParamv(Param.LightColors, this.lightColors, 0, N * 3);
	}

	public void setNSamples (int nSamples) {
		this.nSamples = nSamples;
		setParam(Param.NSamples, nSamples);
	}

	public void setTextureScale (float scl) {
		this.textureScale = scl;
		setParam(Param.TextureScale, textureScale);
	}

	public void setLightGlowTexture (Texture tex) {
		lightGlowTexture = tex;
		setParam(Param.LightGlowTexture, u_texture1);
	}

	public Texture getLightGlowTexture () {
		return lightGlowTexture;
	}

	@Override
	public void rebind () {
		// Re-implement super to batch every parameter
		setParams(Param.Texture, u_texture0);
		setParams(Param.LightGlowTexture, u_texture1);
		setParams(Param.NLights, nLights);
		setParams(Param.NSamples, nSamples);
		setParams(Param.TextureScale, textureScale);
		setParams(Param.Viewport, viewport);
		setParamsv(Param.LightPositions, lightPositions, 0, N * 2);
		setParamsv(Param.LightViewAngles, lightViewAngles, 0, N);
		setParamsv(Param.LightColors, lightColors, 0, N * 3);
		endParams();
	}

	@Override
	protected void onBeforeRender () {
		inputTexture.bind(u_texture0);
		if (lightGlowTexture != null) lightGlowTexture.bind(u_texture1);
	}
}
