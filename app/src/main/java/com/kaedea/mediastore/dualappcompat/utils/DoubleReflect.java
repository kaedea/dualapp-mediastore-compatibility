package com.kaedea.mediastore.dualappcompat.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Kaede
 * @since 2/4/2022
 */
public class DoubleReflect {
    public static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Method getDeclaredFieldMethod = null;
        try {
            getDeclaredFieldMethod = Class.class.getDeclaredMethod(decStr("\253\250\272\213\255\252\246\252\266\240\242\201\251\244\256\247"), String.class);
        } catch (NoSuchMethodException e) {
            throw new NoSuchFieldException("E1: " + clazz.getName() + "," + fieldName);
        }
        Class<?> currClazz = clazz;
        while (true) {
            try {
                final Field field = (Field) getDeclaredFieldMethod.invoke(currClazz, fieldName);
                field.setAccessible(true);
                return field;
            } catch (IllegalAccessException e) {
                throw new NoSuchFieldException("E2: " + clazz.getName() + "," + fieldName);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof NoSuchFieldException) {
                    if (currClazz == Object.class || currClazz == null) {
                        // msg: No field xxx in class yyy or its super classes.
                        throw new NoSuchFieldException(decStr("\205\253\345\240\256\245\255\246\343") + fieldName
                                + decStr("\352\242\252\345\245\253\241\262\261\343") + clazz.getName()
                                + decStr("\366\270\242\361\273\247\277\355\275\272\270\254\270\353\247\251\247\264\263\244\261\355"));
                    } else {
                        currClazz = currClazz.getSuperclass();
                    }
                } else {
                    throw new NoSuchFieldException("E3: " + clazz.getName() + "," + fieldName);
                }
            }
        }
    }

    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... argTypes) throws NoSuchMethodException {
        Method getDeclaredMethodMethod = null;
        try {
            getDeclaredMethodMethod = Class.class.getDeclaredMethod(decStr("\264\251\271\212\252\253\245\253\271\241\241\213\242\264\251\255\247"), String.class, Class[].class);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("E1: " + clazz.getName() + "," + methodName + "," + Arrays.toString(argTypes));
        }
        Class<?> currClazz = clazz;
        while (true) {
            try {
                final Method method = (Method) getDeclaredMethodMethod.invoke(currClazz, methodName, (Object) argTypes);
                method.setAccessible(true);
                return method;
            } catch (IllegalAccessException e) {
                throw new NoSuchMethodException("E2: " + clazz.getName() + "," + methodName + "," + Arrays.toString(argTypes));
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof NoSuchMethodException) {
                    if (currClazz == Object.class || currClazz == null) {
                        // msg: No method xxx [aaa] in class yyy or its super classes.
                        throw new NoSuchMethodException(decStr("\204\244\344\250\243\263\250\256\246\343") + methodName + " " + Arrays.toString(argTypes)
                                + decStr("\352\242\252\345\245\253\241\262\261\343") + clazz.getName()
                                + decStr("\366\270\242\361\273\247\277\355\275\272\270\254\270\353\247\251\247\264\263\244\261\355"));
                    } else {
                        currClazz = currClazz.getSuperclass();
                    }
                } else {
                    throw new NoSuchMethodException("E3: " + clazz.getName() + "," + methodName + "," + Arrays.toString(argTypes));
                }
            }
        }
    }

    public static Constructor<?> findConstructor(Class<?> clazz, Class<?>... argTypes) throws NoSuchMethodException {
        Method getDeclaredCtorMethod = null;
        try {
            getDeclaredCtorMethod = Class.class.getDeclaredMethod(decStr("\261\262\244\225\267\260\240\254\274\252\254\212\245\245\267\261\264\262\243\265\255\261"), Class[].class);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("E1: " + clazz.getName() + ",ctor," + Arrays.toString(argTypes));
        }
        Class<?> currClazz = clazz;
        while (true) {
            try {
                final Constructor<?> ctor = (Constructor<?>) getDeclaredCtorMethod.invoke(currClazz, (Object) argTypes);
                ctor.setAccessible(true);
                return ctor;
            } catch (IllegalAccessException e) {
                throw new NoSuchMethodException("E2: " + clazz.getName() + ",ctor," + Arrays.toString(argTypes));
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof NoSuchMethodException) {
                    if (currClazz == Object.class || currClazz == null) {
                        // msg: No method <init> [aaa] in class yyy or its super classes.
                        throw new NoSuchMethodException(decStr("\235\243\355\243\252\274\241\245\257\344\371\257\251\251\265\374\343") + Arrays.toString(argTypes)
                                + decStr("\352\242\252\345\245\253\241\262\261\343") + clazz.getName()
                                + decStr("\366\270\242\361\273\247\277\355\275\272\270\254\270\353\247\251\247\264\263\244\261\355"));
                    } else {
                        currClazz = currClazz.getSuperclass();
                    }
                } else {
                    throw new NoSuchMethodException("E3: " + clazz.getName() + ",ctor," + Arrays.toString(argTypes));
                }
            }
        }
    }

    private static String decStr(String encStr) {
        int strLen = 0;
        try {
            strLen = encStr.length();
        } catch (Throwable thr) {
            return encStr;
        }
        StringBuilder sb = null;
        int step = 9;
        int i = 0;
        byte res = 0;
        while (true) {
            switch (step) {
                case 1: {
                    return sb.toString();
                }
                case 3: {
                    step = i >= strLen ? step - 2 : step + 2;
                    break;
                }
                case 5: {
                    res = (byte) encStr.charAt(i);
                    step = step + 10;
                    break;
                }
                case 7: {
                    ++i;
                    step = step - 4;
                    break;
                }
                case 9: {
                    sb = new StringBuilder();
                    step = step - 6;
                    break;
                }
                case 11: {
                    sb.append((char) res);
                    step = step - 4;
                    break;
                }
                case 13: {
                    res = (byte) (res ^ 0x3C);
                    step = step + 4;
                    break;
                }
                case 15: {
                    res = (byte) (res ^ (i - strLen));
                    step = step - 2;
                    break;
                }
                case 17: {
                    res = (byte) (res & 0xFF);
                    step = step - 6;
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }
}
