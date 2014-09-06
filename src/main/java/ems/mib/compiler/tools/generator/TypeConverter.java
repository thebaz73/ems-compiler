package ems.mib.compiler.tools.generator;

import org.jsmiparser.smi.SmiObjectType;
import org.jsmiparser.smi.SmiPrimitiveType;

/**
 * Created by IntelliJ IDEA.
 * User: thebaz
 * Date: 2/25/12
 * Time: 9:10 PM
 */
public class TypeConverter {
    public static boolean isEnumerated(SmiObjectType smiObjectType) {
        return smiObjectType.getType().getPrimitiveType() == SmiPrimitiveType.ENUM;
    }

    public static String toJavaType(SmiObjectType smiObjectType) {
        String javaType;
        switch (smiObjectType.getType().getPrimitiveType()) {
            case INTEGER:
            case INTEGER_32:
            case COUNTER_32:
                javaType = "Integer";
                break;
            case GAUGE_32:
            case UNSIGNED_32:
            case COUNTER_64:
            case TIME_TICKS:
                javaType = "Long";
                break;
            case OBJECT_IDENTIFIER:
            case OPAQUE:
                javaType = "Object";
                break;
            case IP_ADDRESS:
                javaType = "java.net.InetAddress";
                break;
            case ENUM:
                javaType = "Integer";
                break;
            case BITS:
                javaType = "String";
                break;
            default:
                javaType = "String";
        }
        return javaType;
    }
}
