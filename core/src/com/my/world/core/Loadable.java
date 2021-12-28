package com.my.world.core;

import java.util.Map;

/**
 * Config Example:
 * <pre>
 *     intField: 1
 *     floatField: 2.0
 *     stringField: "string"
 *     customField1:
 *          type: com.my.com.customObject1
 *          config: ...
 *     customField2:
 *          type: com.my.com.customObject2
 *          config: ...
 *     listField:
 *        - type: com.my.com.customObject3
 *          config: ...
 *        - type: com.my.com.customObject4
 *          config: ...
 * </pre>
 */
public interface Loadable {

    interface OnLoad extends Loadable {
        void load(Map<String, Object> config, Context context);
    }

    interface OnInit extends Loadable {
        void init();
    }

    interface OnDump extends Loadable {
        Map<String, Object> dump(Context context);
    }
}
