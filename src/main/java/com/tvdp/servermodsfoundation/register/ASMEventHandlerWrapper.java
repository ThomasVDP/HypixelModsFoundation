package com.tvdp.servermodsfoundation.register;

import com.google.common.collect.Maps;
import com.tvdp.servermodsfoundation.Reference;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.objectweb.asm.*;

import java.lang.reflect.Method;
import java.util.HashMap;

import static org.objectweb.asm.Opcodes.*;

public class ASMEventHandlerWrapper extends ASMEventHandler
{
    private static int IDs = 0;
    private static final String HANDLER_DESC = Type.getInternalName(IEventListener.class);
    private static final String HANDLER_FUNC_DESC = Type.getMethodDescriptor(IEventListener.class.getDeclaredMethods()[0]);
    private static final ASMClassLoader LOADER = new ASMClassLoader();
    private static final HashMap<Method, Class<?>> cache = Maps.newHashMap();

    public static String currentAddonId;

    public ASMEventHandlerWrapper(Object target, Method method) throws Exception {
        super(target, method, Loader.instance().getModList().stream().filter(modContainer -> modContainer.getModId().equals(Reference.MOD_ID)).findFirst().orElse(null));
    }

    @Override
    public Class<?> createWrapper(Method callback)
    {
        if (cache.containsKey(callback))
        {
            return cache.get(callback);
        }

        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        String name = this.getUniqueName(callback);
        String desc = name.replace('.',  '/');
        String instType = Type.getInternalName(callback.getDeclaringClass());
        String eventType = Type.getInternalName(callback.getParameterTypes()[0]);

        cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER, desc, null, "java/lang/Object", new String[]{ HANDLER_DESC });

        cw.visitSource(".dynamic", null);
        {
            cw.visitField(ACC_PUBLIC, "instance", "Ljava/lang/Object;", null, null).visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/Object;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, desc, "instance", "Ljava/lang/Object;");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "invoke", HANDLER_FUNC_DESC, null, null);
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC, "com/tvdp/hypixelmodsfoundation/HypixelModsFoundation", "instance", "Lcom/tvdp/hypixelmodsfoundation/HypixelModsFoundation;");
            mv.visitFieldInsn(GETFIELD, "com/tvdp/hypixelmodsfoundation/HypixelModsFoundation", "activeModContainers", "Ljava/util/Map;");
            mv.visitLdcInsn(currentAddonId);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "containsKey", "(Ljava/lang/Object;)Z", true);
            Label label = new Label();
            mv.visitJumpInsn(IFEQ, label);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, desc, "instance", "Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, instType);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, eventType);
            mv.visitMethodInsn(INVOKEVIRTUAL, instType, callback.getName(), Type.getMethodDescriptor(callback), false);
            mv.visitLabel(label);
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        cw.visitEnd();
        Class<?> ret = LOADER.define(name, cw.toByteArray());
        cache.put(callback, ret);
        return ret;
    }

    private String getUniqueName(Method callback)
    {
        return String.format("%s_%d_%s_%s_%s", getClass().getName(), IDs++,
                callback.getDeclaringClass().getSimpleName(),
                callback.getName(),
                callback.getParameterTypes()[0].getSimpleName());
    }

    private static class ASMClassLoader extends ClassLoader
    {
        private ASMClassLoader()
        {
            super(ASMClassLoader.class.getClassLoader());
        }

        public Class<?> define(String name, byte[] data)
        {
            return defineClass(name, data, 0, data.length);
        }
    }
}
