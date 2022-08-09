package com.my.world.enhanced.depthmask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

import static com.badlogic.gdx.graphics.GL20.*;

public class ShaderUtil {

    public static final int FLAG_DISABLE = 0;
    public static final int FLAG_USE_VALUE = 1;
    public static final int FLAG_USE_TEXTURE = 2;

    public static final Mesh mesh;
    public static final ShaderProgram clearScreenShader;

    static {
        // 初始化覆盖屏幕的矩形网格
        VertexAttributes attributes = new VertexAttributes(
                VertexAttribute.Position(),
                VertexAttribute.TexCoords(0)
        );
        float[] vertices = {
                -1f, 1f, 0.0f,   0f, 1f,// 左上角
                1f, 1f, 0.0f,    1f, 1f,// 右上角
                -1f, -1f, 0.0f,  0f, 0f,// 左下角
                1f, -1f, 0.0f,   1f, 0f,// 右下角
        };
        short[] indices = {
                0, 3, 1, // 第一个三角形
                0, 2, 3  // 第二个三角形
        };
        mesh = new Mesh(true, 4, 6, attributes);
        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        // 初始化渲染器
        clearScreenShader = new ShaderProgram(
                Gdx.files.classpath("com/my/world/enhanced/portal/clear-screen.vertex.glsl").readString(),
                Gdx.files.classpath("com/my/world/enhanced/portal/clear-screen.fragment.glsl").readString()
        );
        if (!clearScreenShader.isCompiled()) {
            throw new GdxRuntimeException(clearScreenShader.getLog());
        }
    }

    public static void clearScreen(Color color, float depth) {
        clearScreen(
                color, null,
                depth, null
        );
    }

    public static void clearScreen(int colorMapHandler, int depthMapHandler) {
        clearScreen(
                null, colorMapHandler,
                null, depthMapHandler
        );
    }

    public static void clearScreen(Color colorValue, Integer colorMapHandler, Float depthValue, Integer depthMapHandler) {
        clearScreenShader.bind();

        if (colorMapHandler != null) {
            Gdx.gl.glActiveTexture(GL_TEXTURE0); //激活纹理单元, 接下来的glBindTexture函数调用会绑定这个纹理到当前激活的纹理单元
            Gdx.gl.glBindTexture(GL_TEXTURE_2D, colorMapHandler);
            clearScreenShader.setUniformi("u_colorMap", 0);
            clearScreenShader.setUniformi("u_colorFlag", FLAG_USE_TEXTURE);
        } else if (colorValue != null) {
            clearScreenShader.setUniformf("u_colorValue", colorValue);
            clearScreenShader.setUniformi("u_colorFlag", FLAG_USE_VALUE);
        } else {
            clearScreenShader.setUniformi("u_colorFlag", FLAG_DISABLE);
        }

        if (depthMapHandler != null) {
            Gdx.gl.glActiveTexture(GL_TEXTURE1);
            Gdx.gl.glBindTexture(GL_TEXTURE_2D, depthMapHandler);
            clearScreenShader.setUniformi("u_depthMap", 1);
            clearScreenShader.setUniformi("u_depthFlag", FLAG_USE_TEXTURE);
        } else if (depthValue != null) {
            clearScreenShader.setUniformf("u_depthValue", depthValue);
            clearScreenShader.setUniformi("u_depthFlag", FLAG_USE_VALUE);
        } else {
            clearScreenShader.setUniformi("u_depthFlag", FLAG_DISABLE);
        }

        mesh.render(clearScreenShader, GL_TRIANGLES);
    }
}
