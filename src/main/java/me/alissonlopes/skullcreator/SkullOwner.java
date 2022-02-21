package me.alissonlopes.skullcreator;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Class used to abstract the owner setting of a head
 *
 * As this class was created with the intention of
 * implementing support for 1.8, there will be some
 * cost of resources from the extensive use of
 * reflection.
 */
final class SkullOwner {

    private static SetType setType;

    /**
     * Sets the owner of a Head item or block starting at 1.8+ through
     * Reflection
     *
     * @param metaOrState the item SkullMeta or Block Skull State
     * @param owner the heads owner object
     */
    static void setOwner(Object metaOrState, Object owner) {
        getSetType(metaOrState).set(metaOrState, owner);
    }

    private static SetType getSetType(Object metaOrState) {
        if (setType != null) {
            return setType;
        }

        for (SetType setType : SetType.values()) {
            try {
                metaOrState.getClass().getMethod(setType.getName(), setType.getParameterTypes());
                SkullOwner.setType = setType;
                break;
            } catch (NoSuchMethodException e) {
                // we'll handle it quietly later
            }
        }

        if (setType == null) {
            throw new SkullCreatorException("It was impossible to find any owner setting methods.");
        }

        return setType;
    }



    private static UUID extractUUID(Object owner) {
        UUID ownerUUID;
        if (owner instanceof UUID) {
            ownerUUID = (UUID) owner;
        } else if (owner instanceof String) {
            /*
             This method will be used on setOwningPlayer(UUID). But if
             the method of getting an OfflinePlayer by its name is removed
             we can replace this with a web request directly to the Mojang's API
             */
            ownerUUID = Bukkit.getOfflinePlayer((String)owner).getUniqueId();
        } else {
            throw new SkullCreatorException("Invalid owner variable type when extracting the owner UUID");
        }
        return ownerUUID;
    }

    private static OfflinePlayer extractOfflinePlayer(Object owner) {
        OfflinePlayer offlinePlayer;
        if (owner instanceof UUID) {
            offlinePlayer = Bukkit.getOfflinePlayer((UUID) owner);
        } else if (owner instanceof String) {
            /*
             This method will be used on setOwningPlayer(OfflinePlayer). But if
             the method of getting an OfflinePlayer by its name is removed
             we can replace this with a web request directly to the Mojang's API
             */
            offlinePlayer = Bukkit.getOfflinePlayer((String)owner);
        } else {
            throw new SkullCreatorException("Invalid owner variable type when extracting the owner OfflinePlayer reference");
        }
        return offlinePlayer;
    }

    private static String extractName(Object owner) {
        String name;
        if (owner instanceof String) {
            name = (String) owner;
        } else if (owner instanceof UUID) {
            /*
             This method will be used on setOwningPlayer(OfflinePlayer). But if
             the method of getting an OfflinePlayer by its name is removed
             we can replace this with a http request to Mojang's API
             */
            name = Bukkit.getOfflinePlayer((UUID) owner).getName();
        } else {
            throw new SkullCreatorException("Invalid owner variable type when extracting the owner name");
        }
        return name;
    }

    enum SetType {
        UUID("setOwningPlayer", java.util.UUID.class) {
            @Override
            void set(Object metaOrState, Object owner) {
                try {
                    Method setMethod = metaOrState.getClass().getMethod(getName(), getParameterTypes());
                    setMethod.setAccessible(true);
                    setMethod.invoke(metaOrState, SkullOwner.extractUUID(owner));
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        },
        OFFLINE_PLAYER("setOwningPlayer", OfflinePlayer.class) {
            @Override
            void set(Object metaOrState, Object owner) {
                try {
                    Method setMethod = metaOrState.getClass().getMethod(getName(), getParameterTypes());
                    setMethod.setAccessible(true);
                    setMethod.invoke(metaOrState, SkullOwner.extractOfflinePlayer(owner));
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        },
        NAME("setOwner", String.class) {
            @Override
            void set(Object metaOrState, Object owner) {
                try {
                    Method setMethod = metaOrState.getClass().getMethod(getName(), getParameterTypes());
                    setMethod.setAccessible(true);
                    setMethod.invoke(metaOrState, SkullOwner.extractName(owner));
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        };

        final private String name;
        final private Class<?>[] parameterTypes;

        SetType(String name, Class<?>... parameterTypes) {
            this.name = name;
            this.parameterTypes = parameterTypes;
        }

        public String getName() {
            return name;
        }

        public Class<?>[] getParameterTypes() {
            return parameterTypes;
        }

        void set(Object metaOrState, Object owner) {
        }
    }

}