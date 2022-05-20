package com.my.demo.builder.test;

import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BaseShapeBuilder;
import com.badlogic.gdx.math.MathUtils;

/** Helper class with static methods to build cylinders shapes using {@link MeshPartBuilder}.
 * @author xoppa */
public class HollowCylinderShapeBuilder extends BaseShapeBuilder {
	/** Build a cylinder */
	public static void build (MeshPartBuilder builder, float width, float height, float depth, int divisions) {
		build(builder, width, height, depth, divisions, 0, 360);
	}
	
	/** Build a cylinder */
	public static void build (MeshPartBuilder builder, float width, float height, float depth, int divisions, float angleFrom, float angleTo) {
		// FIXME create better cylinder method (- axis on which to create the cylinder (matrix?))
		final float hw = width * 0.5f;
		final float hh = height * 0.5f;
		final float hd = depth * 0.5f;
		final float ao = MathUtils.degreesToRadians * angleFrom;
		final float step = (MathUtils.degreesToRadians * (angleTo - angleFrom)) / divisions;
		final float us = 1f / divisions;
		float u = 0f;
		float angle = 0f;
		VertexInfo curr1 = vertTmp3.set(null, null, null, null);
		curr1.hasUV = curr1.hasPosition = curr1.hasNormal = true;
		VertexInfo curr2 = vertTmp4.set(null, null, null, null);
		curr2.hasUV = curr2.hasPosition = curr2.hasNormal = true;
		short i1, i2, i3 = 0, i4 = 0;

		builder.ensureVertices(2 * (divisions + 1));
		builder.ensureRectangleIndices(divisions);
		for (int i = 0; i <= divisions; i++) {
			angle = ao + step * i;
			u = 1f - us * i;
			curr1.position.set(MathUtils.cos(angle) * hw, 0f, MathUtils.sin(angle) * hd);
			curr1.normal.set(curr1.position).nor();
			curr1.position.y = -hh;
			curr1.uv.set(u, 1);
			curr2.position.set(curr1.position);
			curr2.normal.set(curr1.normal);
			curr2.position.y = hh;
			curr2.uv.set(u, 0);
			i2 = builder.vertex(curr1);
			i1 = builder.vertex(curr2);
			if (i != 0) builder.rect(i3, i1, i2, i4); // FIXME don't duplicate lines and points
			if (i != 0) builder.rect(i3, i4, i2, i1); // FIXME don't duplicate lines and points
			i4 = i2;
			i3 = i1;
		}
	}
}
