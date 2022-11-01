package com.my.world.enhanced.bool.util;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class VertexMixer {

    private final Array<VertexAttribute> targetAttributes = new Array<>();
    private int targetAttrNum = 0;
    private int targetVertexSize = 0;
    private final Array<VertexAttributes> inputAttributes = new Array<>();
    private final Array<Integer> inputVertexSize = new Array<>();
    private final Array<Integer> inputPositionOffset = new Array<>();
    private final Array<Array<Integer>> inputMaps = new Array<>();
    private final Array<Array<Integer>> inputOffsets = new Array<>();
    private AttrProvider attrProvider;
    private boolean allowChange = true;


    //-----------------------------------Configure-------------------------------------//
    private float[] vertices = null;
    private int point = 0;

    public int addAttributes(VertexAttributes vertexAttributes) {
        int id = inputAttributes.size;
        Array<Integer> map = new Array<>();

        // 遍历目标数组, 找出每个元素与输入数组中元素的映射关系
        for (int i = 0; i < targetAttributes.size; i++) {
            VertexAttribute ta = targetAttributes.get(i);

            //  遍历输入数组, 找出对应属性,
            // 找到后把它的位置保存在Map数组对应位置
            // 如果没找到, 设置为-1
            boolean isFound = false;
            for (int j = 0; j < vertexAttributes.size(); j++) {
                VertexAttribute va = vertexAttributes.get(j);
                if (ta.usage == va.usage && !map.contains(j, false)) {
                    isFound = true;
                    map.add(j);
                    break;
                }
            }
            if (!isFound) {
                map.add(-1);
            }
        }

        assert (map.size == targetAttrNum);

        if (allowChange) {
            // 遍历输入数组, 找出没有被映射的元素
            for (int j = 0; j < vertexAttributes.size(); j++) {
                if (!map.contains(j, false)) {
                    VertexAttribute va = vertexAttributes.get(j);
                    addNewAttrToTarget(va);
                    map.add(j);
                }
            }

            assert (map.size == targetAttrNum);
        }

        // 计算输入数组每个属性的偏移, 简化后面操作
        Array<Integer> offset = new Array<Integer>();
        int tmp = 0;
        for (int i = 0; i < vertexAttributes.size(); i++) {
            offset.add(tmp);
            tmp += vertexAttributes.get(i).numComponents;
        }

        assert (offset.size == vertexAttributes.size());

        // 将得到的各种信息保存
        inputPositionOffset.add(getPositionOffset(vertexAttributes));
        inputOffsets.add(offset);
        inputAttributes.add(vertexAttributes);
        inputMaps.add(map);
        inputVertexSize.add(vertexAttributes.vertexSize / 4);

        assert (inputAttributes.size == inputMaps.size &&
                inputAttributes.size == inputVertexSize.size &&
                inputAttributes.size == inputOffsets.size &&
                inputAttributes.size == inputPositionOffset.size);

        return id;
    }

    public void setAttrProvider(AttrProvider attrProvider) {
        this.attrProvider = attrProvider;
    }

    //-----------------------------------Getter-------------------------------------//

    public void allowChange() {
        this.allowChange = true;
    }

    public void disallowChange() {
        this.allowChange = false;
    }

    public int getTargetVertexSize() {
        return targetVertexSize;
    }

    //-----------------------------------Work-------------------------------------//

    public VertexAttributes getTargetAttr() {
        VertexAttribute[] vs = new VertexAttribute[targetAttributes.size];
        for (int i = 0; i < vs.length; i++) {
            vs[i] = targetAttributes.get(i);
        }
        return new VertexAttributes(vs);
    }

    public boolean isAllowChange() {
        return allowChange;
    }

    public void begin(int num) {
        vertices = new float[targetVertexSize * num];
        point = 0;
    }

    public void addVertex(Vector3 pos) {
        float[] inVertices = new float[targetVertexSize];
        int inputPoint = 0;
        for (int i = 0; i < this.targetAttributes.size; i++) {
            VertexAttribute v = this.targetAttributes.get(i);
            int attrSize = v.numComponents;
            float[] newAttr = new float[attrSize];
            if (v.usage == VertexAttributes.Usage.Position) {
                newAttr[0] = pos.x;
                newAttr[1] = pos.y;
                newAttr[2] = pos.z;
            } else {
                attrProvider.setAttr(targetAttributes.get(i), pos.x, pos.y, pos.z, newAttr);
            }
            System.arraycopy(newAttr, 0, inVertices, inputPoint, attrSize);
            inputPoint += attrSize;
        }

        assert (point + targetVertexSize - 1 < vertices.length) : "Vertices Array Is Full!!!";

        System.arraycopy(inVertices, 0, vertices, point, targetVertexSize);
        point += targetVertexSize;
    }

    public void addVertex(int id, float[] in, Vector3 pos) {
        assert (id < inputVertexSize.size) : "Id Is Not Allowed!!!";
        assert (in.length > 3) : "Input Array Is Too Small!!!";
        int x = this.inputPositionOffset.get(id),
                y = x + 1,
                z = x + 2;
        in[x] = pos.x;
        in[y] = pos.y;
        in[z] = pos.z;
        addVertex(id, in);
    }

    public void addVertex(int id, float[] in) {
        assert (id < inputVertexSize.size) : "Id Is Not Allowed!!!";
        int inputVertexSize = this.inputVertexSize.get(id);
        assert (in.length == inputVertexSize) : "Input Length Is Not Allowed!!!";

        VertexAttributes inputAttribute = this.inputAttributes.get(id);
        Array<Integer> inputMap = this.inputMaps.get(id);
        Array<Integer> inputOffset = this.inputOffsets.get(id);

        float[] inVertices = new float[targetVertexSize];
        int inputPoint = 0;
        for (int i = 0; i < inputMap.size; i++) {
            int index = inputMap.get(i);
            int attrSize = targetAttributes.get(i).numComponents;
            assert (index < inputAttribute.size());
            if (index != -1) {
                assert (targetAttributes.get(i).numComponents == inputAttribute.get(index).numComponents);
                System.arraycopy(in, inputOffset.get(index), inVertices, inputPoint, attrSize);
            } else {
                // 如果输入顶点没有目标顶点所需属性
                // 则新建属性
                if (attrProvider != null) {
                    int x = this.inputPositionOffset.get(id),
                            y = x + 1,
                            z = x + 2;
                    float[] newAttr = new float[attrSize];
                    attrProvider.setAttr(targetAttributes.get(i), in[x], in[y], in[z], newAttr);
                    System.arraycopy(newAttr, 0, inVertices, inputPoint, attrSize);
                }
            }
            inputPoint += attrSize;
        }

        assert (point + targetVertexSize - 1 < vertices.length) : "Vertices Array Is Full!!!";

        System.arraycopy(inVertices, 0, vertices, point, targetVertexSize);
        point += targetVertexSize;
    }

    public float[] build() {
        assert (point == vertices.length);
        float[] vertices = this.vertices;
        this.vertices = null;
        this.point = 0;
        return vertices;
    }

    public void buildToArray(float[] vs, int vsOffset) {
        assert (point == vertices.length);
        assert (vsOffset * targetVertexSize + vertices.length <= vs.length);

        System.arraycopy(vertices, 0, vs, vsOffset * targetVertexSize + 0, vertices.length);

        this.vertices = null;
        int point = 0;
    }


    //-----------------------------------Private-------------------------------------//

    private void addNewAttrToTarget(VertexAttribute vertexAttribute) {
        targetAttributes.add(vertexAttribute);
        for (Array<Integer> map : inputMaps) {
            map.add(-1);
            assert (map.size == targetAttrNum);
        }
        targetAttrNum++;
        targetVertexSize += vertexAttribute.numComponents;
    }

    private int getPositionOffset(VertexAttributes vertexAttributes) {
        int offsetPosition = 0;
        boolean isFound = false;
        for (VertexAttribute attribute : vertexAttributes) {
            int offset = attribute.offset / 4;
            if (attribute.usage == VertexAttributes.Usage.Position) {
                offsetPosition = offset;
                isFound = true;
            }
        }
        assert isFound : "Position Offset Not Found!!!";
        return offsetPosition;
    }


    //-----------------------------------Inner Class-------------------------------------//

    public abstract static class AttrProvider {
        protected abstract void setAttr(VertexAttribute v, float x, float y, float z, float[] attrs);

        protected void set(float[] attrs, float... as) {
            if (attrs == null || as == null || attrs.length <= 0 || as.length <= 0) return;
            int length = (as.length < attrs.length) ? as.length : attrs.length;
            System.arraycopy(as, 0, attrs, 0, length);
        }

        protected void setPosition(float[] attrs, float a0, float a1, float a2) {
            attrs[0] = a0;
            attrs[1] = a1;
            attrs[2] = a2;
        }

        protected void setColorUnpacked(float[] attrs, float a0, float a1, float a2, float a3) {
            attrs[0] = a0;
            attrs[1] = a1;
            attrs[2] = a2;
            attrs[3] = a3;
        }

        protected void setColorPacked(float[] attrs, float a0, float a1, float a2, float a3) {
            attrs[0] = a0;
            attrs[1] = a1;
            attrs[2] = a2;
            attrs[3] = a3;
        }

        protected void setNormal(float[] attrs, float a0, float a1, float a2) {
            attrs[0] = a0;
            attrs[1] = a1;
            attrs[2] = a2;
        }

        protected void setTextureCoordinates(float[] attrs, float a0, float a1) {
            attrs[0] = a0;
            attrs[1] = a1;
        }

        protected void setGeneric() {
            // No Thing In Here
        }

        protected void setBoneWeight(float[] attrs, float a0, float a1) {
            attrs[0] = a0;
            attrs[1] = a1;
        }

        protected void setBiNormal(float[] attrs, float a0, float a1, float a2) {
            attrs[0] = a0;
            attrs[1] = a1;
            attrs[2] = a2;
        }
    }
}
