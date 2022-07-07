package me.xanium.gemseconomy.cheque;

import com.google.gson.Gson;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

public class ChequeStorageType implements PersistentDataType<String, ChequeStorage> {
    private static final Gson gson = new Gson();

    public static final ChequeStorageType INSTANCE = new ChequeStorageType();

    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public Class<ChequeStorage> getComplexType() {
        return ChequeStorage.class;
    }

    @Override
    public String toPrimitive(ChequeStorage complex, PersistentDataAdapterContext context) {
        return gson.toJson(complex);
    }

    @Override
    public ChequeStorage fromPrimitive(String primitive, PersistentDataAdapterContext context) {
      return gson.fromJson(primitive,ChequeStorage.class);
    }
}
