package com.my.world.module.render.enhanced;

import com.badlogic.gdx.graphics.Color;
import com.my.world.core.Config;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedPointLight extends EnhancedLight {

	@Config
	@Builder.Default
	public float constant = 1.0f;

	@Config
	@Builder.Default
	public float linear = 0.09f;

	@Config
	@Builder.Default
	public float quadratic = 0.032f;

	@Config public Color ambient;
	@Config public Color diffuse;
	@Config public Color specular;

	public EnhancedPointLight(Color diffuse) {
		this.diffuse = diffuse;
		this.constant = 1.0f;
		this.linear = 0.09f;
		this.quadratic = 0.032f;
	}
}
