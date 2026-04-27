package net.undeadriders.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.undeadriders.UndeadRiders;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class OpenPartiesAndClaimsCompat {

    private static final String SERVER_API_CLASS = "xaero.pac.common.server.api.OpenPACServerAPI";
    private static final String PLAYER_CONFIG_OPTIONS_CLASS = "xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions";
    private static final String HOSTILE_NATURAL_SPAWN_EXCEPTION_FIELD = "CLAIM_EXCEPTION_HOSTILE_NATURAL_SPAWN";
    private static boolean loggedFailure;

    private OpenPartiesAndClaimsCompat() {
    }

    public static boolean preventsHostileNaturalSpawn(ServerLevel world, BlockPos pos) {
        try {
            Class<?> serverApiClass = Class.forName(SERVER_API_CLASS);
            Object api = invokeStaticByNameAndArgCount(serverApiClass, "get", 1, world.getServer());
            Object claimsManager = api.getClass().getMethod("getServerClaimsManager").invoke(api);
            Object claim = invokeChunkClaimLookup(claimsManager, world.dimension().identifier(), pos);
            if (claim == null) {
                return false;
            }

            Object protection = api.getClass().getMethod("getChunkProtection").invoke(api);
            Object claimConfig = invokeByNameAndArgCount(protection, "getConfig", 1, claim);
            Object hostileNaturalSpawnOption = getStaticField(PLAYER_CONFIG_OPTIONS_CLASS, HOSTILE_NATURAL_SPAWN_EXCEPTION_FIELD);
            Object hostileNaturalSpawnException = invokeByNameAndArgCount(claimConfig, "getEffective", 1, hostileNaturalSpawnOption);
            return Boolean.FALSE.equals(hostileNaturalSpawnException);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            if (!loggedFailure) {
                loggedFailure = true;
                UndeadRiders.LOGGER.warn("[UndeadRiders] Open Parties and Claims spawn protection checks failed. Undead Riders will not use OPAC claim spawn rules until this is fixed.", e);
            }
            return false;
        }
    }

    private static Object invokeChunkClaimLookup(Object claimsManager, Object dimensionId, BlockPos pos) throws ReflectiveOperationException {
        for (Method method : claimsManager.getClass().getMethods()) {
            if (method.getName().equals("get") && canAccept(method, dimensionId, pos)) {
                return method.invoke(claimsManager, dimensionId, pos);
            }
        }
        throw new NoSuchMethodException("No OPAC claims manager get(dimension, pos) method found.");
    }

    private static Object invokeByNameAndArgCount(Object target, String name, int argCount, Object... args) throws ReflectiveOperationException {
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == argCount) {
                return method.invoke(target, args);
            }
        }
        throw new NoSuchMethodException("No OPAC method found: " + name);
    }

    private static Object invokeStaticByNameAndArgCount(Class<?> targetClass, String name, int argCount, Object... args) throws ReflectiveOperationException {
        for (Method method : targetClass.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == argCount) {
                return method.invoke(null, args);
            }
        }
        throw new NoSuchMethodException("No OPAC static method found: " + name);
    }

    private static boolean canAccept(Method method, Object... args) {
        if (method.getParameterCount() != args.length) {
            return false;
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (args[i] != null && !parameterTypes[i].isInstance(args[i])) {
                return false;
            }
        }
        return true;
    }

    private static Object getStaticField(String className, String fieldName) throws ReflectiveOperationException {
        Field field = Class.forName(className).getField(fieldName);
        return field.get(null);
    }
}
