import bpy
import re
from mathutils import Matrix

bl_info = {
 "name": "Export My World Prefab Config",
 "author": "Kaixinguo",
 "version": (1, 0),
 "blender": (2, 80, 0),
 "location": "View3D > Import-Export > My World Prefab Config",
 "description": "Create MyWorld Prefab Config File",
 "warning": "",
 "wiki_url": "",
 "tracker_url": "",
 "category": "Import-Export"}


def getList(obj) :
    "Get a List from give Matrix"
    objm = obj.matrix_local

    objsc = objm.to_scale()
    scale = Matrix.Scale(objsc.x, 4, (1,0,0)) * Matrix.Scale(objsc.z, 4, (0,1,0)) * Matrix.Scale(objsc.y, 4, (0,0,1))

    objro = objm.to_euler()
    rotation = Matrix.Rotation(objro.x, 4, 'X') * Matrix.Rotation(objro.z, 4, 'Y') * Matrix.Rotation(-objro.y, 4, 'Z')

    objtr = objm.to_translation()
    translation = Matrix.Translation((objtr.x, objtr.z, -objtr.y,))

    transform = translation @ rotation @ scale
    
    list = []
    for i in range(0,4):
        for j in range(0,4):
            list.append(transform[j][i])
    
    return list


def indent(tab_num, str):
    str = re.sub('^', ' ' * tab_num, str, flags=re.MULTILINE)
    str = re.sub('^ *$', '', str, flags=re.MULTILINE)
    return str


def indent_to_list(tab_num, str):
    str = indent(tab_num, str)
    if tab_num >= 2:
        str = re.sub('^' + ' ' * tab_num, ' ' * (tab_num - 2) + '- ', str)
    return str


def get_keys(object):
    keys = []
    
    for key in object.keys():
        if not key in [
            'cycles_visibility',
            'ant_landscape',
            'blenderkit',
            'bkit_ratings',
            'cycles',
            '_RNA_UI',
            'prefab',
            'mainObject'
        ]: keys.append(key)
    
    return keys


def get_entity_config(obj):
    ''' Get Config of Entity '''
    
    if obj.instance_type != 'NONE':
        return ''
    
    # Basic Params
    config = ''
    config += 'name: %s\n' % obj.name
    config += 'id: %s\n' % obj.name
    config += 'activeSelf: %s\n' % obj.get('activeSelf', 'true')
    
    # Parent
    if (obj.parent != None):
        config += 'parent: %s\n' % obj.parent.name
    
    
    # ----- Components Begin ----- #
    
    config += 'components:\n'
    
    
    # Components - Position
    if obj.get('position.disable') == None:
        config += '''\
- type: com.my.world.module.common.Position
  config:
    localTransform: %s
    disableInherit: %s
''' % (
        obj.get('position.localTransform', getList(obj)),
        obj.get('position.disableInherit', 'false')
    )
    
    
    # Components - PresetModelRender
    if obj.get('render.disable') == None and obj.data != None:
        config += '''\
- type: com.my.world.module.render.PresetModelRender
  config: {modelRender: %s, includeEnv: %s, active: %s}
''' % (
        obj.get('render.modelRender', obj.data.name),
        obj.get('render.includeEnv', 'true'),
        obj.get('render.active', 'true')
    )
    
    
    # Components - PresetTemplateRigidBody
    if obj.get('rigidbody.disable') == None and obj.data != None:
        config += '''\
- type: com.my.world.module.physics.PresetTemplateRigidBody
  config: {templateRigidBody: %s, group: %s, mask: %s, isTrigger: %s}
''' % (
        obj.get('rigidbody.templateRigidBody', obj.data.name),
        obj.get('rigidbody.group', '512'),
        obj.get('rigidbody.mask', '-1'),
        obj.get('rigidbody.isTrigger', 'false')
    )
    
    
    # Components - External
    if obj.get('components') != None:
        config += str(obj.get('components')).replace('\\n', '\n')
        config += '\n'
    
    # ----- Components End ----- #
    
    
    return config


def get_prefab_config(obj):
    ''' Get Config of Prefab '''
    
    if obj.instance_type != 'COLLECTION':
        return ''
    
    # Prefab Name & Main Object Name
    prefabName = obj.instance_collection.name
    mainObjectName = prefabName
    
    # Basic Params
    config =  '- type: com.my.world.core.Prefab\n'
    config += '  prefabName: %s\n' % prefabName
    config += '  config:\n'
    config += '    %s.name: %s\n' % (prefabName, obj.name)
    config += '    %s.globalId: %s\n' % (prefabName, obj.name)
    config += '    %s.config.components[0].config.localTransform: %s\n' \
        % (prefabName, getList(obj))
    
    # Parent
    if (obj.parent != None):
        config += '    %s.parent: %s\n' % (prefabName, obj.parent.name)
    
    # Configs
    keys = get_keys(obj)
    if len(keys) != 0:
        for key in keys:
            config += '    %s: %s\n' % (key, obj[key])
    
    return config


def get_scene_config():
    config = '''\
name: %s
entities:
''' % bpy.context.scene.name
    
    for obj in bpy.data.objects:
        instance_type = obj.instance_type
        if instance_type == 'NONE':
            config += '- type: com.my.world.core.Entity\n'
            config += '  config:\n'
            config += indent(4, get_entity_config(obj))
        elif instance_type == 'COLLECTION':
            config += get_prefab_config(obj)
        else:
            pass
    
    config +='''\
systems:
  - type: com.my.world.module.camera.CameraSystem
    config: {}
  - type: com.my.world.module.render.DefaultRenderSystem
    config: {}
  - type: com.my.world.module.physics.PhysicsSystem
    config: {maxSubSteps: 5, fixedTimeStep: 0.016666668}
  - type: com.my.world.module.script.ScriptSystem
    config: {}
  - type: com.my.world.module.render.EnvironmentSystem
    config: {}
  - type: com.my.world.module.input.InputSystem
    config: {}
  - type: com.my.world.module.physics.ConstraintSystem
    config: {}
'''
    
    return config

# ---------- ---------- ---------- ---------- #


def get_collection_config(collection):
    ''' Get Config of Collection '''
    
    # Basic Params
    config =  'type: com.my.world.core.Prefab\n'
    config += 'id: %s\n' % collection.name
    config += 'config:\n'
    config += '  entities:\n'
    
    # Entities
    for obj in collection.objects:
        instance_type = obj.instance_type
        if instance_type == 'NONE':
            config += indent_to_list(4, get_entity_config(obj))
        elif instance_type == 'COLLECTION':
            pass
        else:
            pass
    
    return config


def get_assets_config():
    config = ''
    
    for collection in bpy.data.collections:
        if collection.name in ['RigidBodyWorld']:
            continue
        if collection.hide_render:
            continue
        if config != '':
            config += '---\n'
        config += get_collection_config(collection)
    
    return config

# ---------- ---------- ---------- ---------- #

def write_assets_data(context, filepath, use_some_setting):
    print("running write_assets_data...")
    config = get_assets_config()
    f = open(filepath + '.assets.yml', 'w', encoding='utf-8')
    f.write(config)
    f.close()

def write_scene_data(context, filepath, use_some_setting):
    print("running write_scene_data...")
    config = get_scene_config()
    f = open(filepath, 'w', encoding='utf-8')
    f.write(config)
    f.close()


# ExportHelper is a helper class, defines filename and
# invoke() function which calls the file selector.
from bpy_extras.io_utils import ExportHelper
from bpy.props import StringProperty, BoolProperty, EnumProperty
from bpy.types import Operator


class ExportSomeData(Operator, ExportHelper):
    """This appears in the tooltip of the operator and in the generated docs"""
    bl_idname = "my.export_world_prefab_config_file"  # important since its how bpy.ops.import_test.some_data is constructed
    bl_label = "Export Prefab Config File"

    # ExportHelper mixin class uses this
    filename_ext = ".yml"

    filter_glob: StringProperty(
        default="*.yml",
        options={'HIDDEN'},
        maxlen=255,  # Max internal buffer length, longer would be clamped.
    )

    # List of operator properties, the attributes will be assigned
    # to the class instance from the operator settings before calling.
    use_setting: BoolProperty(
        name="Example Boolean",
        description="Example Tooltip",
        default=True,
    )

    type: EnumProperty(
        name="Example Enum",
        description="Choose between two items",
        items=(
            ('OPT_A', "First Option", "Description one"),
            ('OPT_B', "Second Option", "Description two"),
        ),
        default='OPT_A',
    )

    def execute(self, context):
        write_assets_data(context, self.filepath, self.use_setting)
        write_scene_data(context, self.filepath, self.use_setting)
        return {'FINISHED'}


# Only needed if you want to add into a dynamic menu
def menu_func_export(self, context):
    self.layout.operator(ExportSomeData.bl_idname, text="MyWorld Prefab Config File(.yml)")


def register():
    bpy.utils.register_class(ExportSomeData)
    bpy.types.TOPBAR_MT_file_export.append(menu_func_export)


def unregister():
    bpy.utils.unregister_class(ExportSomeData)
    bpy.types.TOPBAR_MT_file_export.remove(menu_func_export)


if __name__ == "__main__":
    register()

    # test call
    bpy.ops.my.export_world_prefab_config_file('INVOKE_DEFAULT')
