/*******************************************************************************
 * Copyright 2012 tsagrista
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

package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.Bias;
import com.bitfire.postprocessing.filters.Blur;
import com.bitfire.postprocessing.filters.Blur.BlurType;
import com.bitfire.postprocessing.filters.Combine;
import com.bitfire.postprocessing.filters.Scattering;
import com.bitfire.postprocessing.utils.PingPongBuffer;

/** Light scattering implementation.
 * @author Toni Sagrista */
public final class LightScattering extends PostProcessorEffect {
	public static class Settings {
		public final String name;

		public final BlurType blurType;
		public final int blurPasses; // simple blur
		public final float blurAmount; // normal blur (1 pass)
		public final float bias;

		public final float scatteringIntensity;
		public final float scatteringSaturation;
		public final float baseIntensity;
		public final float baseSaturation;

		public Settings (String name, BlurType blurType, int blurPasses, float blurAmount, float bias, float baseIntensity,
			float baseSaturation, float scatteringIntensity, float scatteringSaturation) {
			this.name = name;
			this.blurType = blurType;
			this.blurPasses = blurPasses;
			this.blurAmount = blurAmount;

			this.bias = bias;
			this.baseIntensity = baseIntensity;
			this.baseSaturation = baseSaturation;
			this.scatteringIntensity = scatteringIntensity;
			this.scatteringSaturation = scatteringSaturation;

		}

		// simple blur
		public Settings (String name, int blurPasses, float bias, float baseIntensity, float baseSaturation,
			float scatteringIntensity, float scatteringSaturation) {
			this(name, BlurType.Gaussian5x5b, blurPasses, 0, bias, baseIntensity, baseSaturation, scatteringIntensity,
				scatteringSaturation);
		}

		public Settings (Settings other) {
			this.name = other.name;
			this.blurType = other.blurType;
			this.blurPasses = other.blurPasses;
			this.blurAmount = other.blurAmount;

			this.bias = other.bias;
			this.baseIntensity = other.baseIntensity;
			this.baseSaturation = other.baseSaturation;
			this.scatteringIntensity = other.scatteringIntensity;
			this.scatteringSaturation = other.scatteringSaturation;

		}
	}

	private PingPongBuffer pingPongBuffer;

	private Scattering scattering;
	private Blur blur;
	private Bias bias;
	private Combine combine;

	private Settings settings;

	private boolean blending = false;
	private int sfactor, dfactor;

	public LightScattering (int fboWidth, int fboHeight) {
		pingPongBuffer = PostProcessor.newPingPongBuffer(fboWidth, fboHeight, PostProcessor.getFramebufferFormat(), false);

		scattering = new Scattering(fboWidth, fboHeight);
		blur = new Blur(fboWidth, fboHeight);
		bias = new Bias();
		combine = new Combine();

		setSettings(new Settings("default", 2, -0.9f, 1f, 1f, 0.7f, 1f));
	}

	@Override
	public void dispose () {
		combine.dispose();
		bias.dispose();
		blur.dispose();
		pingPongBuffer.dispose();
	}

	/** Sets the positions of the 10 lights in [0..1] in both coordinates **/
	public void setLightPositions (int nLights, float[] vec) {
		scattering.setLightPositions(nLights, vec);
	}

	public void setLightViewAngles (float[] vec) {
		scattering.setLightViewAngles(vec);
	}

	public void setBaseIntesity (float intensity) {
		combine.setSource1Intensity(intensity);
	}

	public void setBaseSaturation (float saturation) {
		combine.setSource1Saturation(saturation);
	}

	public void setScatteringIntesity (float intensity) {
		combine.setSource2Intensity(intensity);
	}

	public void setScatteringSaturation (float saturation) {
		combine.setSource2Saturation(saturation);
	}

	public void setBias (float b) {
		bias.setBias(b);
	}

	public void enableBlending (int sfactor, int dfactor) {
		this.blending = true;
		this.sfactor = sfactor;
		this.dfactor = dfactor;
	}

	public void disableBlending () {
		this.blending = false;
	}

	public void setBlurType (BlurType type) {
		blur.setType(type);
	}

	public void setSettings (Settings settings) {
		this.settings = settings;

		// setup threshold filter
		setBias(settings.bias);

		// setup combine filter
		setBaseIntesity(settings.baseIntensity);
		setBaseSaturation(settings.baseSaturation);
		setScatteringIntesity(settings.scatteringIntensity);
		setScatteringSaturation(settings.scatteringSaturation);

		// setup blur filter
		setBlurPasses(settings.blurPasses);
		setBlurAmount(settings.blurAmount);
		setBlurType(settings.blurType);

	}

	public void setDecay (float decay) {
		scattering.setDecay(decay);
	}

	public void setDensity (float density) {
		scattering.setDensity(density);
	}

	public void setWeight (float weight) {
		scattering.setWeight(weight);
	}

	public void setNumSamples (int numSamples) {
		scattering.setNumSamples(numSamples);
	}

	public void setBlurPasses (int passes) {
		blur.setPasses(passes);
	}

	public void setBlurAmount (float amount) {
		blur.setAmount(amount);
	}

	public float getBias () {
		return bias.getBias();
	}

	public float getBaseIntensity () {
		return combine.getSource1Intensity();
	}

	public float getBaseSaturation () {
		return combine.getSource1Saturation();
	}

	public float getScatteringIntensity () {
		return combine.getSource2Intensity();
	}

	public float getScatteringSaturation () {
		return combine.getSource2Saturation();
	}

	public boolean isBlendingEnabled () {
		return blending;
	}

	public int getBlendingSourceFactor () {
		return sfactor;
	}

	public int getBlendingDestFactor () {
		return dfactor;
	}

	public BlurType getBlurType () {
		return blur.getType();
	}

	public Settings getSettings () {
		return settings;
	}

	public int getBlurPasses () {
		return blur.getPasses();
	}

	public float getBlurAmount () {
		return blur.getAmount();
	}

	@Override
	public void render (final FrameBuffer src, final FrameBuffer dest) {
		Texture texsrc = src.getColorBufferTexture();

		boolean blendingWasEnabled = PostProcessor.isStateEnabled(GL20.GL_BLEND);
		Gdx.gl.glDisable(GL20.GL_BLEND);

		pingPongBuffer.begin();
		{
			// apply bias
			bias.setInput(texsrc).setOutput(pingPongBuffer.getSourceBuffer()).render();

			scattering.setInput(pingPongBuffer.getSourceBuffer()).setOutput(pingPongBuffer.getResultBuffer()).render();

			pingPongBuffer.set(pingPongBuffer.getResultBuffer(), pingPongBuffer.getSourceBuffer());

			// blur pass
			blur.render(pingPongBuffer);
		}
		pingPongBuffer.end();

		if (blending || blendingWasEnabled) {
			Gdx.gl.glEnable(GL20.GL_BLEND);
		}

		if (blending) {
			Gdx.gl.glBlendFunc(sfactor, dfactor);
		}

		restoreViewport(dest);

		// mix original scene and blurred threshold, modulate via
		combine.setOutput(dest).setInput(texsrc, pingPongBuffer.getResultTexture()).render();
	}

	@Override
	public void rebind () {
		blur.rebind();
		bias.rebind();
		combine.rebind();
		pingPongBuffer.rebind();
	}
}
