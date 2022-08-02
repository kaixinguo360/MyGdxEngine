package com.my.world.module.animation;

public class Screen {

    public static final float PIXEL_WIDTH = 0.3f;
    public static final float PIXEL_HEIGHT = 1f;

    public char[][] data;
    public final int width;
    public final int height;

    public float centerX;
    public float centerY;
    public final float scaleX;
    public final float scaleY;


    public Screen(int width, int height, float centerX, float centerY, float scaleX, float scaleY) {
        this.width = (int) (width / PIXEL_WIDTH);
        this.height = (int) (height / PIXEL_HEIGHT);
        this.centerX = centerX;
        this.centerY = centerY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        data = new char[this.width][this.height];
        clear();
    }

    public void clear() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                data[i][j] = ' ';
            }
        }
    }

    public void drawCo() {
        drawLineX(0, ':');
        drawLineY(0, '.');
        for (int i = -10; i < 10; i++) {
            draw(0, i * 10, '+');
            draw(i * 10, 0, '+');
            if (0 <= i && i < 10) {
                draw(-1 / scaleX, i * 10, (char) ('0' + i));
                draw(i * 10, -1 / scaleY, (char) ('0' + i));
            }
        }
    }

    public void drawBox() {
        setLineX(0, '|');
        setLineY(0, '-');
        for (int i = -10; i < 10; i++) {
            set(0, toIndexY(i * 10), '+');
            set(toIndexX(i * 10), 0, '+');
            if (0 <= i && i < 10) {
                set(2, toIndexY(i * 10), (char) ('0' + i));
                set(3, toIndexY(i * 10), '0');
                set(toIndexX(i * 10), 1, (char) ('0' + i));
                set(toIndexX(i * 10) + 1, 1, '0');
            }
        }
    }

    public int toIndexX(float x) {
        float realWidth = width * PIXEL_WIDTH / scaleX;
        float realX = x - centerX + realWidth / 2;
        return Math.round(realX * scaleX / PIXEL_WIDTH);
    }

    public int toIndexY(float y) {
        float realHeight = height * PIXEL_HEIGHT / scaleY;
        float realY = y - centerY + realHeight / 2;
        return Math.round(realY * scaleY / PIXEL_HEIGHT);
    }

    private void drawLineX(float x, char pixel) {
        setLineX(toIndexX(x), pixel);
    }

    private void setLineX(int indexX, char pixel) {
        if (0 <= indexX && indexX < width) {
            for (int i = 0; i < height; i++) {
                data[indexX][i] = pixel;
            }
        }
    }

    private void drawLineY(float y, char pixel) {
        setLineY(toIndexY(y), pixel);
    }

    private void setLineY(int indexY, char pixel) {
        if (0 <= indexY && indexY < height) {
            for (int i = 0; i < width; i++) {
                data[i][indexY] = pixel;
            }
        }
    }

    public void draw(float x, float y, char pixel) {
        set(toIndexX(x), toIndexY(y), pixel);
    }

    public void set(int indexX, int indexY, char pixel) {
        if (0 <= indexX && indexX < this.width && 0 <= indexY && indexY < this.height) {
            data[indexX][indexY] = pixel;
        }
    }

    public void print() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int j = height - 1; j >= 0; j--) {
            for (int i = 0; i < width; i++) {
                sb.append(data[i][j]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

}
