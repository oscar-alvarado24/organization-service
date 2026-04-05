package co.com.organization.r2dbc.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductConstants {
    public static final String MSG_PRODUCT_SAVE_SUCCESSFULLY = "Producto creado exitosamente con id %s";
    public static final String MSG_ERROR_SAVED_PRODUCT = "Error al crear el producto";

    public static final String MSG_NAME_UPDATED_SUCCESSFULLY = "El nombre del producto se actualizó exitosamente";
    public static final String MSG_PRODUCT_NOT_FOUND = "Producto no encontrado";
    public static final String LOG_ERROR_CREATING_PRODUCT = "Error al crear el producto: ";
    public static final String MSG_ERROR_VALIDATING_PRODUCT_EXISTENCE = "Error al validar la existencia del producto";
    public static final String MSG_ERROR_CHANGE_PRODUCT_NAME = "Error al cambiar el nombre del producto";
    public static final String LOG_ERROR_CHANGE_PRODUCT_NAME = "Error al cambiar el nombre del producto: ";
    public static final String LOG_ERROR_VALIDATING_PRODUCT_EXISTENCE = "Error al validar la existencia del producto: ";
    public static final String MSG_ERROR_GET_PRODUCTS_BY_ID_LIST = "Error al obtener los productos de la lista de ids";
    public static final String LOG_ERROR_GET_PRODUCTS_BY_ID_LIST = "Error al obtener los productos de la lista de ids: ";
}