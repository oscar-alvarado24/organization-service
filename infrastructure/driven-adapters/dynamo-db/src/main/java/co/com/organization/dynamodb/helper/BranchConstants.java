package co.com.organization.dynamodb.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BranchConstants {
    public static final String BRANCH_METADATA_LABEL = "branch#metadata";
    public static final String TOP_PRODUCT_LABEL = "branch#top_product";
    public static final String BRANCH_PRODUCT_LABEL = "branch#product#";
    public static final String TABLE_NAME = "sucursales";
    public static final String MSG_ERROR_CREATING_BRANCH = "Error al crear la sucursal";
    public static final String LOG_ERROR_CREATING_BRANCH = "Error al crear la sucursal: ";
    public static final String MSG_ERROR_GETTING_TOP_PRODUCTS = "Error al obtener los productos con mayor stock de la sucursales de una franquicia";
    public static final String LOG_ERROR_GETTING_TOP_PRODUCTS = "Error al obtener los productos con mayor stock de la sucursales de una franquicia: ";
    public static final String LOG_ERROR_ADDING_PRODUCT_TO_BRANCH = "Error al agregar el producto con id {} a la sucursal: ";
    public static final String MSG_ERROR_ADDING_PRODUCT_TO_BRANCH = "Error al agregar el producto a la sucursal";
    public static final String MSG_PRODUCT_DELETED_SUCCESSFULLY = "Producto eliminado correctamente";
    public static final String MSG_BRANCH_WITH_OUT_PRODUCTS = "La sucursal no tiene productos asociados";
    public static final String MSG_ERROR_DELETE_PRODUCT_OF_BRANCH = "Error al emininar el producto de la sucursal";
    public static final String LOG_ERROR_DELETE_PRODUCT_OF_BRANCH = "Error al eliminar el producto con id {} de la sucursal con id {}: ";
    public static final String MSG_CHANGE_PRODUCT_STOCK_SUCCESSFULLY = "Stock del producto actualizado correctamente";
    public static final String LOG_ERROR_CHANGE_STOCK_PRODUCT = "Error al actualizar el stock del producto con id {} en la sucursal con id {}: ";
    public static final String MSG_ERROR_CHANGE_STOCK_PRODUCT = "Error al actualizar el stock del producto en la sucursal";
    public static final String MSG_CHANGE_BRANCH_NAME_SUCCESSFULLY = "Nombre de la sucursal actualizado correctamente";
    public static final String LOG_ERROR_CHANGE_BRANCH_NAME = "Error al actualizar el nombre de la sucursal con id {}: ";
    public static final String MSG_ERROR_CHANGE_BRANCH_NAME = "Error al actualizar el nombre de la sucursal";
    public static final String MSG_ERROR_DELETE_BRANCH = "Error al eliminar la sucursal";
    public static final String LOG_ERROR_DELETE_BRANCH = "Error al eliminar la sucursal con id {}: ";
    public static final String MSG_ERROR_PRODUCT_NOT_FOUND = "Error, producto no encontrado en la sucursal";
    public static final String MSG_ERROR_BRANCH_NOT_FOUND = "Error, sucursal no encontrada";
    public static final String MSG_ERROR_SAVING_BRANCH = "Error al guardar la sucursal";
}