package com.malcom.sm4rtstock.model;

public enum TipoMovimiento {
    ENTRADA,    // stock aumenta (compra, ajuste +)
    SALIDA,     // stock disminuye (venta, ajuste -)
    AJUSTE      // corrección manual
}