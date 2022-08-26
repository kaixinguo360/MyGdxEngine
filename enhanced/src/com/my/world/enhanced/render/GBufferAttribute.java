package com.my.world.enhanced.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;

public class GBufferAttribute extends TextureAttribute
{
	public final static String GBuffer0Alias = "gBuffer0";
	public final static long GBuffer0 = register(GBuffer0Alias);

	public final static String GBuffer1Alias = "gBuffer1";
	public final static long GBuffer1 = register(GBuffer1Alias);

	public final static String GBuffer2Alias = "gBuffer2";
	public final static long GBuffer2 = register(GBuffer2Alias);

	public final static String GBuffer3Alias = "gBuffer3";
	public final static long GBuffer3 = register(GBuffer3Alias);

	static{
		Mask |= GBuffer0 | GBuffer1 | GBuffer2 | GBuffer3;
	}

	public GBufferAttribute(long type, Texture texture) {
		super(type, texture);
	}
	public GBufferAttribute(GBufferAttribute attribute) {
		super(attribute);
	}
	public static GBufferAttribute createGBuffer0(Texture texture) {
		return new GBufferAttribute(GBuffer0, texture);
	}
	public static GBufferAttribute createGBuffer1(Texture texture) {
		return new GBufferAttribute(GBuffer1, texture);
	}
	public static GBufferAttribute createGBuffer2(Texture texture) {
		return new GBufferAttribute(GBuffer2, texture);
	}
	public static GBufferAttribute createGBuffer3(Texture texture) {
		return new GBufferAttribute(GBuffer3, texture);
	}

	@Override
	public Attribute copy() {
		return new GBufferAttribute(this);
	}

	public static BaseShader.LocalSetter setter(final long type) {
		return new BaseShader.LocalSetter() {
			@Override
			public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				GBufferAttribute attribute = combinedAttributes.get(GBufferAttribute.class, type);
				if(attribute != null){
					final int unit = shader.context.textureBinder.bind(attribute.textureDescription);
					shader.set(inputID, unit);
				}
			}
		};
	}
}
