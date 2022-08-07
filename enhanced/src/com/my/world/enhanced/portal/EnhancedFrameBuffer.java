package com.my.world.enhanced.portal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.IntBuffer;

import static com.badlogic.gdx.graphics.GL20.GL_FRAMEBUFFER_BINDING;

public class EnhancedFrameBuffer extends FrameBuffer {

    protected final IntBuffer intBuffer = BufferUtils.newIntBuffer(16);
    protected int originalFrameBufferIndex;

    protected EnhancedFrameBuffer(GLFrameBufferBuilder<? extends GLFrameBuffer<Texture>> bufferBuilder) {
        super(bufferBuilder);
    }

    public EnhancedFrameBuffer(Pixmap.Format format, int width, int height, boolean hasDepth) {
        super(format, width, height, hasDepth, false);
    }

    public EnhancedFrameBuffer(Pixmap.Format format, int width, int height, boolean hasDepth, boolean hasStencil) {
        super(format, width, height, hasDepth, hasStencil);
    }

    @Override
    public void begin() {
        Gdx.gl20.glGetIntegerv(GL_FRAMEBUFFER_BINDING, intBuffer);
        originalFrameBufferIndex = intBuffer.get(0);
        super.begin();
    }

    @Override
    public void end(int x, int y, int width, int height) {
        super.end(x, y, width, height);
        Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, originalFrameBufferIndex);
    }
}
