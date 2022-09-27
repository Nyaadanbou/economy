package me.xanium.gemseconomy.cheque;

import com.google.gson.Gson;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ChequeStorageType implements PersistentDataType<String, ChequeStorage> {
    private static final Gson gson = new Gson();

    public static final ChequeStorageType INSTANCE = new ChequeStorageType();

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<ChequeStorage> getComplexType() {
        return ChequeStorage.class;
    }

    @Override
    public @NotNull String toPrimitive(@NotNull ChequeStorage complex, @NotNull PersistentDataAdapterContext context) {
        return gson.toJson(complex);
    }

    @Override
    public @NotNull ChequeStorage fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        return gson.fromJson(primitive, ChequeStorage.class);
    }
}
